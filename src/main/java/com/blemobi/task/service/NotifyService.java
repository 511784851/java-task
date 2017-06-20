package com.blemobi.task.service;

import java.util.List;
import java.util.stream.Collectors;

import com.blemobi.library.redis.dao.RedisDao;
import com.blemobi.sep.probuf.ResultProtos.PInt64List;
import com.blemobi.sep.probuf.TaskApiProtos.PTaskMsg;
import com.blemobi.sep.probuf.TaskApiProtos.PTaskMsgs;
import com.blemobi.task.basic.TaskData;
import com.blemobi.task.basic.TaskInfo;
import com.blemobi.task.msg.NotifyMsg;

/**
 * 消息回调
 * 
 * @author zhaoyong
 *
 */
public class NotifyService {

	private long dailyTime;
	private PTaskMsgs taskMsgs;
	private boolean bool;

	/**
	 * 构造方法
	 * 
	 * @param taskMsgs
	 * @param bool
	 *            是否处理日常任务
	 */
	public NotifyService(PTaskMsgs taskMsgs, boolean bool) {
		this.taskMsgs = taskMsgs;
		this.dailyTime = TaskData.getDailyDate();
		this.bool = bool;
	}

	/**
	 * 消息回调
	 * 
	 * @return
	 */
	public PInt64List exec() {
		List<Long> list = taskMsgs.getTaskMsgList().stream().map(c -> makeMsg(c)).collect(Collectors.toList());
		return PInt64List.newBuilder().addAllList(list).build();
	}

	/**
	 * 消息处理
	 * 
	 * @param callback
	 * @return
	 */
	private long makeMsg(PTaskMsg taskMsg) {
		String uuid = taskMsg.getUuid();
		short msgID = (short) taskMsg.getMsgID();
		int count = taskMsg.getCount();
		return TaskData.get().stream().filter(t -> t.getMsgIDs().contains(msgID))// 过滤msgID对应的任务
				.mapToLong(t -> msgMap(uuid, count, t))// msgID对应的任务进度处理
				.reduce((a, b) -> Math.min(a, b) == -1 ? Math.max(a, b) : Math.min(a, b)).getAsLong();// 归总优先级规则：0>dailyTime>-1
	}

	/**
	 * 任务进度处理
	 * 
	 * @param uuid
	 * @param c
	 *            增量
	 * @param t
	 * @return
	 */
	private long msgMap(String uuid, int c, TaskInfo t) {
		if (!bool && t.isDailyTask())
			return -1;

		short ID = t.getID();
		String tasKey = t.getRedisKey(uuid, dailyTime);
		byte count = UserGoldUtil.getTaskCount(tasKey, ID);// 已完成次数
		byte total = (byte) (t.getLoop() * t.getTarg());// 可完成次数
		if (count >= 0 && count < total) {// 任务还在进行中
			// 应该奖励次数
			int num = count + c > total ? total - count : c;
			// 累计完成次数
			count = new RedisDao<Byte>().exec(r -> r.hincrBy(tasKey, ID + "", c).byteValue());
			// 如果是日常任务完成默认领取金币
			if (t.isDailyTask()) {
				while (num-- > 0) {
					new UserGoldUtil(uuid, ID + "", (byte) 2, t.getGold(), t.getDesc_sc()).incrAndDetail();
					NotifyMsg.add(uuid, "完成 " + t.getDesc("") + " +" + t.getGold() + "金币");
				}
			}
		}
		if (count < 0 || count >= total) {// 任务已完成
			if (t.isDailyTask())
				return dailyTime;// 日常任务完成，取消消息订阅有效时间到今天
			if (t.isNocivTask())
				return -1;// 新手任务完成，永久取消消息订阅
		}
		return 0;// 消息订阅不做更新
	}
}