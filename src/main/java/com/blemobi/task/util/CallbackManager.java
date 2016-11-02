package com.blemobi.task.util;

import java.util.List;
import java.util.Set;

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

/*
 * 消息回调
 */
@Log4j
public class CallbackManager {
	// 日常任务的结束时间点
	private long dailyTime;

	private PCallbackArray callbackArray;

	// 构造方法
	public CallbackManager(PCallbackArray callbackArray) {
		this.callbackArray = callbackArray;
		this.dailyTime = TaskHelper.getDailyDate();
	}

	// 消息回调
	public PMessage callback() {
		PInt64List.Builder rtnInt64List = PInt64List.newBuilder();
		Jedis jedis = RedisManager.getRedis();
		for (PCallback callback : callbackArray.getCallbackList()) {
			String uuid = callback.getUuid();
			int msgid = callback.getMsgid();
			long time = callback.getTime();

			String logStr = "uuid=[" + uuid + "],msgid=[" + msgid + "],time=[" + time + "]";
			log.error("有消息回调 -> " + logStr);
			long rtntime = -1;// 要更新的uuid-msgid状态
			// 消息ID对应的任务
			List<Integer> taskIdList = TaskHelper.getTaskListByMsgid(msgid);
			log.error("消息id[" + msgid + "]关联的任务有： " + taskIdList);
			boolean mainBool = true;// 是否有主线任务未完成
			boolean dailyBool = true;// 是否有日常任务未完成
			for (int taskId : taskIdList) {
				TaskTag tag = TaskHelper.getTaskTag(taskId);// 任务类型
				String userTaskKey = "";// 用户任务KEY
				int num = 0;// 任务要求次数
				if (TaskTag.MAIN == tag) {// 主线任务
					userTaskKey = Constant.GAME_TASK_MAIN + uuid;
					num = TaskHelper.getMainTask(taskId).getNum();
				} else if (TaskTag.DAILY == tag) {// 日常任务
					userTaskKey = Constant.GAME_TASK_DAILY + uuid + ":" + dailyTime;
					num = TaskHelper.getDailyTask(taskId).getNum();
				}
				String targetStr = jedis.hget(userTaskKey, taskId + "");
				if (!Strings.isNullOrEmpty(targetStr)) {// 任务已接取
					int target = Integer.parseInt(targetStr);
					if (target >= 0) {// 任务未完成
						// 累加一次任务进度
						long rtn = jedis.hincrBy(userTaskKey, taskId + "", 1);
						if (rtn < num) {// 任务未完成
							if (TaskTag.MAIN == tag) {// 主线任务未完成
								log.debug("主线任务可领取奖励了[taskId=" + taskId + "] -> " + logStr);
								mainBool = false;
							} else if (TaskTag.DAILY == tag) {// 日常任务未完成
								log.debug("日常任务可领取奖励了[taskId=" + taskId + "] -> " + logStr);
								dailyBool = false;
							}
						}
					}
				}
			}
			if (!mainBool) {// 有主线任务未完成，订阅消息有效时间为永久
				rtntime = -1;
			} else if (!dailyBool) {// 有日常任务未完成，订阅消息有效时间为当晚x时
				rtntime = dailyTime;
			} else {// 任务都完成了，取消该消息订阅
				rtntime = 0;
			}

			log.error("消息回调更新 -> " + logStr + " rtntime:" + rtntime);
			if (rtntime == 0) {
				jedis.hdel(Constant.GAME_MSGID + uuid, msgid + "");
			} else {
				jedis.hset(Constant.GAME_MSGID + uuid, msgid + "", rtntime + "");
			}
			rtnInt64List.addList(rtntime);
		}

		RedisManager.returnResource(jedis);
		return ReslutUtil.createReslutMessage(rtnInt64List.build());
	}
}