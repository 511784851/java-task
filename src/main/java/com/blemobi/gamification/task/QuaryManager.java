package com.blemobi.gamification.task;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.blemobi.gamification.init.BadgeHelper;
import com.blemobi.gamification.init.LevelHelper;
import com.blemobi.gamification.init.TaskHelper;
import com.blemobi.library.redis.RedisManager;
import com.blemobi.library.util.CommonUtil;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.GamificationProtos.PAchievement;
import com.blemobi.sep.probuf.GamificationProtos.PBadgeDetail;
import com.blemobi.sep.probuf.GamificationProtos.PGamification;
import com.blemobi.sep.probuf.GamificationProtos.PTaskDetail;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.google.common.base.Strings;

import redis.clients.jedis.Jedis;

public class QuaryManager {
	private String userInfoKey;// 用户任务信息在redis中的key
	private String userMainTaskKey;// 用户主线任务在redis中的key
	private String userTodayTaskKey;// 用户每日任务在redis中的key
	private String userBadgeKey;// 用户徽章信息在redis中的key
	
	// 构造函数
	public QuaryManager(String uuid) {
		String today = CommonUtil.getNowDate();
		this.userInfoKey = Constant.GAME_USER_INFO + uuid;
		this.userTodayTaskKey = Constant.GAME_USER_TASK + uuid + ":" + today;
		this.userMainTaskKey = Constant.GAME_USER_TASK + uuid;
		this.userBadgeKey = Constant.GAME_USER_BADGE + uuid;
	}

	// 获取任务明细
	public PMessage taskDetail() {
		Jedis jedis = RedisManager.getRedis();
		String levelStr = jedis.hget(userInfoKey, "level");// 读取用户的等级
		String experienceStr = jedis.hget(userInfoKey, "experience");// 读取用户的经验值
		Map<String, String> taskMap = jedis.hgetAll(userMainTaskKey);// 读取用户的全部任务进度
		taskMap.putAll(jedis.hgetAll(userTodayTaskKey));// 读取用户的全部任务进度
		RedisManager.returnResource(jedis);

		int level = 0;
		if(!Strings.isNullOrEmpty(levelStr)){
			level = Integer.parseInt(levelStr);
		}
		
		int experience = 0;
		if(!Strings.isNullOrEmpty(experienceStr)){
			experience = Integer.parseInt(experienceStr);
		}
		
		PGamification.Builder gamificationBuilder = PGamification.newBuilder()
				.setExperience(experience)
				.setLevel(level);

		Collection<PTaskDetail> taskKeyCollection = TaskHelper.getTaskList();
		Iterator<PTaskDetail> it = taskKeyCollection.iterator();
		while (it.hasNext()) {
			PTaskDetail taskDetail = it.next();
			PTaskDetail.Builder taskDetailBuild = taskDetail.toBuilder();

			String progressStr = taskMap.get(taskDetail.getTaskKey().toString());// 任务进度
			if (Strings.isNullOrEmpty(progressStr)) {// 未接任务
				taskDetailBuild.setProgress(0)// 任务进度0
						.setStatu(1);// 1-任务未接受
			} else {// 已接任务
				int progress = Integer.parseInt(progressStr);
				taskDetailBuild.setProgress(progress);// 任务进度0
				int target = taskDetail.getTarget();// 任务要求
				if (progress >= target) {// 任务完成
					taskDetailBuild.setStatu(3);// 1-任务完成
				} else {
					taskDetailBuild.setStatu(2);// 1-任务进行中
				}
			}

			gamificationBuilder.addTask(taskDetailBuild.build());
		}

		return ReslutUtil.createReslutMessage(gamificationBuilder.build());
	}

	// 获得徽章明细
	public PMessage achievement() {
		Jedis jedis = RedisManager.getRedis();
		String levelStr = jedis.hget(userInfoKey, "level");// 读取用户的等级
		String experienceStr = jedis.hget(userInfoKey, "experience");// 读取用户的经验值
		Map<String, String> badgeMap = jedis.hgetAll(userBadgeKey);// 读取用户的全部徽章
		
		int level = 0;
		if(!Strings.isNullOrEmpty(levelStr)){
			level = Integer.parseInt(levelStr);
		}
		
		int experience = 0;
		if(!Strings.isNullOrEmpty(experienceStr)){
			experience = Integer.parseInt(experienceStr);
		}
		
		long nextLevelExperience = LevelHelper.getNextLevelExperience(level+1);//下一个等级的最低经验值
		long upgradeExperience = nextLevelExperience - experience;//距离下一个等级的经验值
		PAchievement.Builder achievementBuilder = PAchievement.newBuilder()
				.setExperience(experience)
				.setLevel(level)
				.setNextLevelExperience((int)nextLevelExperience)
				.setUpgradeExperience((int)upgradeExperience);
				

		Set<String> keys = BadgeHelper.getBadgeKeys();
		int badgeSum = keys.size();// 徽章总算
		int badgeHave = 0;// 已获得徽章
		for(String key : keys){
			PBadgeDetail badgeDetail = BadgeHelper.getBadgeDetail(key);
			PBadgeDetail.Builder badgeDetailBuild = badgeDetail.toBuilder();

			String time = badgeMap.get(key);// 获得徽章时间
			if(Strings.isNullOrEmpty(time)){
				int target = badgeDetail.getTarget();// 徽章要求任务的次数
				String progressStr = jedis.hget(userInfoKey, key);// 任务历史总进度
				int progress = 0;
				if (!Strings.isNullOrEmpty(progressStr)) {// 未接任务
					progress = Integer.parseInt(progressStr);
				}
				String accuracy = CommonUtil.accuracy(progress, target, 2);
				badgeDetailBuild.setStatu(2)// 未获得
				.setProgress(accuracy)
				.setTime(0);
			} else {
				badgeDetailBuild.setStatu(1)// 已获得
				.setProgress("100%")
				.setTime(Long.parseLong(time));
				badgeHave++;
			}

			achievementBuilder.addBadge(badgeDetailBuild.build());
		}
		
		RedisManager.returnResource(jedis);

		achievementBuilder.setBadgeHave(badgeHave).setBadgeSum(badgeSum);
		
		return ReslutUtil.createReslutMessage(achievementBuilder.build());
	}
}
