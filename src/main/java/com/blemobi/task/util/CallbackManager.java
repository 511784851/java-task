package com.blemobi.task.util;

import java.util.List;

import com.blemobi.library.redis.RedisManager;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.ResultProtos.PInt64List;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.TaskProtos.PCallback;
import com.blemobi.sep.probuf.TaskProtos.PCallbackArray;
import com.blemobi.task.basic.TaskHelper;
import com.blemobi.task.basic.TaskTag;
import com.google.common.base.Strings;

import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;

/**
 * 消息回调
 * 
 * @author zhaoyong
 *
 */
@Log4j
public class CallbackManager {
	/**
	 * 日常任务的结束时间点
	 */
	private long dailyTime;

	private PCallbackArray callbackArray;
	private Jedis jedis;

	/**
	 * 构造方法
	 * 
	 * @param callbackArray
	 */
	public CallbackManager(PCallbackArray callbackArray) {
		this.callbackArray = callbackArray;
		this.dailyTime = TaskHelper.getDailyDate();
	}

	/**
	 * 消息回调
	 * 
	 * @return
	 */
	public PMessage callback() {
		jedis = RedisManager.getRedis();
		log.debug("接受到消息回调 数量-> " + callbackArray.getCallbackCount());
		PInt64List.Builder rtnInt64List = PInt64List.newBuilder();
		for (PCallback callback : callbackArray.getCallbackList()) {
			long rtntime = makeMsg(callback);
			rtnInt64List.addList(rtntime);
		}

		RedisManager.returnResource(jedis);
		return ReslutUtil.createReslutMessage(rtnInt64List.build());
	}

	/**
	 * 消息处理
	 * 
	 * @param jedis
	 * @param callback
	 * @return
	 */
	private long makeMsg(PCallback callback) {
		String uuid = callback.getUuid();
		int msgid = callback.getMsgid();
		long time = callback.getTime();

		String logStr = "uuid=[" + uuid + "],msgid=[" + msgid + "],time=[" + time + "]";
		log.debug("有消息回调 -> " + logStr);

		// 消息ID对应的任务
		List<Integer> taskIdList = TaskHelper.getTaskListByMsgid(msgid);
		log.debug("消息id[" + msgid + "]关联的任务有： " + taskIdList);
		boolean mainBool = true;// 是否有主线任务未完成
		boolean dailyBool = true;// 是否有日常任务未完成
		for (int taskId : taskIdList) {
			TaskTag tag = TaskHelper.getTaskTag(taskId);// 任务类型
			String userTaskKey = getUserTaskKey(taskId, uuid, tag);// 用户任务KEY
			int num = getNum(taskId, uuid, tag);// 任务要求次数
			String targetStr = jedis.hget(userTaskKey, taskId + "");
			if (!Strings.isNullOrEmpty(targetStr)) {// 任务已初始化
				int target = Integer.parseInt(targetStr);
				if (target >= 0) {// 任务未完成
					num = getNum(taskId, userTaskKey, num, tag);// 任务要求次数
					// 累加一次任务进度
					long rtn = jedis.hincrBy(userTaskKey, taskId + "", 1);
					if (rtn < num) {// 任务未完成
						if (TaskTag.MAIN == tag)
							mainBool = false;// 主线任务未完成
						else if (TaskTag.DAILY == tag)
							dailyBool = false;// 日常任务未完成
					}
				}
			}
		}
		// 要更新的uuid-msgid状态
		long rtntime = getRtntime(mainBool, dailyBool);
		updateTime(uuid, msgid, rtntime);
		log.debug("消息回调更新 -> " + logStr + " rtntime:" + rtntime);
		return rtntime;
	}

	/**
	 * 获取用户任务KEY
	 * 
	 * @param taskId
	 * @param uuid
	 * @return
	 */
	private String getUserTaskKey(int taskId, String uuid, TaskTag tag) {
		if (TaskTag.MAIN == tag) // 主线任务
			return Constant.GAME_TASK_MAIN + uuid;
		else if (TaskTag.DAILY == tag) // 日常任务
			return Constant.GAME_TASK_DAILY + uuid + ":" + dailyTime;
		return "";
	}

	/**
	 * 获取任务要求次数
	 * 
	 * @param taskId
	 * @param uuid
	 * @return
	 */
	private int getNum(int taskId, String uuid, TaskTag tag) {
		if (TaskTag.MAIN == tag) {// 主线任务
			return TaskHelper.getMainTask(taskId).getNum();
		} else if (TaskTag.DAILY == tag) {// 日常任务
			return 0;
		}
		return 0;
	}

	/**
	 * 如果是日常任务，获取相关难度任务要求次数
	 * 
	 * @param taskId
	 * @param userTaskKey
	 * @param num
	 * @return
	 */
	private int getNum(int taskId, String userTaskKey, int num, TaskTag tag) {
		if (TaskTag.DAILY == tag) {// 日常任务
			int did = Integer.parseInt(jedis.hget(userTaskKey, TaskUtil.diffculty + taskId));
			num = TaskHelper.getDailyTaskNum(taskId, did);// 任务要求次数
		}
		return num;
	}

	/**
	 * 要更新的uuid-msgid状态
	 * 
	 * @param mainBool
	 * @param dailyBool
	 * @return
	 */
	private long getRtntime(boolean mainBool, boolean dailyBool) {
		if (!mainBool) // 有主线任务未完成，订阅消息有效时间为永久
			return -1;
		else if (!dailyBool) // 有日常任务未完成，订阅消息有效时间为当晚x时
			return dailyTime;
		else // 任务都完成了，取消该消息订阅
			return 0;
	}

	/**
	 * 更新消息订阅记录
	 * 
	 * @param jedis
	 * @param uuid
	 * @param msgid
	 * @param rtntime
	 */
	private void updateTime(String uuid, int msgid, long rtntime) {
		if (rtntime == 0)
			jedis.hdel(Constant.GAME_MSGID + uuid, msgid + "");// 删除
		else
			jedis.hset(Constant.GAME_MSGID + uuid, msgid + "", rtntime + "");// 更新
	}
}