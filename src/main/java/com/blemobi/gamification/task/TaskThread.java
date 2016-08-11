package com.blemobi.gamification.task;

import com.blemobi.gamification.helper.BadgeHelper;
import com.blemobi.gamification.helper.LevelExperience;
import com.blemobi.gamification.helper.LevelHelper;
import com.blemobi.library.redis.RedisManager;
import com.blemobi.sep.probuf.GamificationProtos.PBadgeDetail;
import com.blemobi.sep.probuf.GamificationProtos.PTaskDetail;
import com.google.common.base.Strings;

import redis.clients.jedis.Jedis;

public class TaskThread extends Thread {
	private long nowProgress;// 任务当前进度
	private long sumProgress;// 任务历史总进度
	private String userInfoKey;// 用户游戏信息在redis中的key
	private String userBadgeKey;// 用户徽章信息在redis中的key
	private PTaskDetail taskDetail;// 任务明细

	public TaskThread(long nowProgress, long sumProgress, String userInfoKey, String userBadgeKey,
			PTaskDetail taskDetail) {
		this.nowProgress = nowProgress;
		this.sumProgress = sumProgress;
		this.userInfoKey = userInfoKey;
		this.userBadgeKey = userBadgeKey;
		this.taskDetail = taskDetail;
	}

	public void run() {
		checkExperience();
		checkBadge();
	}

	// 任务进度增加后检查是否任务完成
	public void checkExperience() {
		int target = taskDetail.getTarget();// 任务要求次数
		if (nowProgress == target) {// 任务完成 ，可赠送经验值
			int experience = taskDetail.getExperience();// 此任务可获得经验值

			Jedis jedis = RedisManager.getRedis();
			long nowExperience = jedis.hincrBy(userInfoKey, "experience", experience);// 给用户增加经验值
			LevelExperience le = LevelHelper.getLevel(nowExperience);// 当前经验值对应的等级
			long newLevel = le.getLevel();
			String oldLevelString = jedis.hget(userInfoKey, "level");// 用户以前的等级
			long oldLevel = -1;
			if (!Strings.isNullOrEmpty(oldLevelString)) {
				oldLevel = Long.parseLong(oldLevelString);
			}

			if (newLevel > oldLevel) {// 升级了
				jedis.hset(userInfoKey, "level", newLevel + "");// 等级
				jedis.hset(userInfoKey, "levelName", le.getName());// 等级名称
				if (newLevel == 4) {// 赠送VIP4徽章
					jedis.hset(userBadgeKey, "VIP4", System.currentTimeMillis() + "");
				} else if (newLevel == 5) {// 赠送VIP5徽章
					jedis.hset(userBadgeKey, "VIP5", System.currentTimeMillis() + "");
				} else if (newLevel == 6) {// 赠送VIP6徽章
					jedis.hset(userBadgeKey, "VIP6", System.currentTimeMillis() + "");
				}
			}
		}
	}

	// 任务进度增加后检查是否触发徽章赠送
	public void checkBadge() {
		String taskKey = taskDetail.getTaskKey().toString();
		PBadgeDetail badgeDetail = BadgeHelper.getBadgeDetail(taskKey);
		if (badgeDetail != null) {// 任务有对应的徽章
			int target = badgeDetail.getTarget();// 徽章要求的总数量
			if (sumProgress == target) {// 达成赠送徽章要求
				Jedis jedis = RedisManager.getRedis();
				jedis.hset(userBadgeKey, taskKey, System.currentTimeMillis() + "");
				RedisManager.returnResource(jedis);
			}
		}
	}
}
