package com.blemobi.gamification.task;

import com.blemobi.gamification.init.TaskHelper;
import com.blemobi.library.exception.BaseException;
import com.blemobi.library.redis.RedisManager;
import com.blemobi.library.util.CommonUtil;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.GamificationProtos.PTaskDetail;
import com.blemobi.sep.probuf.GamificationProtos.PTaskKey;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.google.common.base.Strings;

import redis.clients.jedis.Jedis;

public class TaskManager {
	private String taskKey;// 任务key
	private String userTaskKey;// 用户任务信息在redis中的key
	private String userInfoKey;// 用户游戏信息在redis中的key
	private String userBadgeKey;// 用户徽章信息在redis中的key
	private PTaskDetail taskDetail;// 任务明细

	// 构造函数
	public TaskManager(String uuid, String taskKey) {
		this.taskKey = taskKey;
		this.userInfoKey = Constant.GAME_USER_INFO + uuid;
		this.userBadgeKey = Constant.GAME_USER_BADGE + uuid;
		this.taskDetail = TaskHelper.getTaskDetail(taskKey);

		int type = taskDetail.getType();
		if (type == 1) {// 日常任务
			String today = CommonUtil.getNowDate();
			this.userTaskKey = Constant.GAME_USER_TASK + uuid + ":" + today;
		} else {// 一次任务
			this.userTaskKey = Constant.GAME_USER_TASK + uuid;
		}
	}

	// 接受任务
	public PMessage onAccept() throws BaseException {
		// 只有日常任务和主线任务才可以手动接受
		if (taskDetail.getType() == 1 || taskDetail.getType() == 2) {
			Jedis jedis = RedisManager.getRedis();
			boolean bool = jedis.hexists(userTaskKey, taskKey);
			if (!bool) {// 可以接受任务
				jedis.hsetnx(userTaskKey, taskKey, "0");
			}

			RedisManager.returnResource(jedis);
			return ReslutUtil.createSucceedMessage();
		} else {
			return ReslutUtil.createErrorMessage(1901012, "taskKey error");
		}
	}

	// 任务通知
	public PMessage onFinish() throws BaseException {
		if (!PTaskKey.VIP.toString().equals(taskKey)) {// 达成VIP隐藏任务不可主动通知
			Jedis jedis = RedisManager.getRedis();
			String progress = jedis.hget(userTaskKey, taskKey);// 历史任务进度

			if (Strings.isNullOrEmpty(progress) && PTaskKey.REGISTER.toString().equals(taskKey)) {// 首次注册
				progress = "0"; // 首次注册默认接取注册任务
			}

			if (!Strings.isNullOrEmpty(progress)) {// 已接受任务，可以累计进度
				long oldProgress = Long.parseLong(progress);
				int target = taskDetail.getTarget();// 任务要求次数
				if (oldProgress < target) {// 任务还未完成
					// 增加一次任务进度
					long nowProgress = jedis.hincrBy(userTaskKey, taskKey, 1);
					long sumProgress = jedis.hincrBy(userInfoKey, taskKey, 1);

					// 启动线程，处理任务的后续业务
					TaskThread taskThread = new TaskThread(nowProgress, sumProgress, userInfoKey, userBadgeKey,
							taskDetail);
					taskThread.start();
					;
				}
			}
			RedisManager.returnResource(jedis);
			return ReslutUtil.createSucceedMessage();
		} else {
			return ReslutUtil.createErrorMessage(1901012, "taskKey error");
		}
	}
}
