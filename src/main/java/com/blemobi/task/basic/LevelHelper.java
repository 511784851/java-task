package com.blemobi.task.basic;

import java.util.Collection;
import java.util.Random;

/*
 * 经验等级管理类
 */
public class LevelHelper {
	/*
	 * 获取经验值对应等级
	 */
	public static int getLevelByExp(long exp) {
		for (LevelInfo levelInfo : BasicData.levelMap.values()) {
			if (Math.max(levelInfo.getExp_min(), exp) == Math.min(exp, levelInfo.getExp_max())) {
				return levelInfo.getLevel();
			}
		}
		return 1;
	}

	/*
	 * 获取经验等级信息
	 */
	public static LevelInfo getLevelInfoByLevel(int level) {
		return BasicData.levelMap.get(level);
	}

	/*
	 * 获取等级的下一个经验等级信息
	 */
	public static LevelInfo getNextLevelInfoByLevel(int level) {
		int nextLevel = level + 1;
		LevelInfo levelInfo = getLevelInfoByLevel(nextLevel);
		if (levelInfo != null) {
			return levelInfo;
		} else {
			return getLevelInfoByLevel(level);
		}
	}

	/*
	 * 获取经验等级的最小经验值
	 */
	public static long getMinExpByLevel(int level) {
		return getLevelInfoByLevel(level).getExp_min();
	}

	/*
	 * 获取等级对应的可接最大日常任务数量
	 */
	public static int getMaxCountByLevel(int level) {
		return getLevelInfoByLevel(level).getMax();
	}

	/*
	 * 获取最大经验等级
	 */
	public static int getMaxLevel() {
		int max_level = 0;
		for (int level : BasicData.levelMap.keySet()) {
			if (level > max_level) {
				max_level = level;
			}
		}
		return max_level;
	}

	/*
	 * 获取最大经验值
	 */
	public static long getMaxExp() {
		return getLevelInfoByLevel(getMaxLevel()).getExp_min();
	}

	/*
	 * 获取全部等级数据
	 */
	public static Collection<LevelInfo> getAllLevelList() {
		return BasicData.levelMap.values();
	}

	/*
	 * 获取等级对应的可接最大困难和史诗日常任务数量
	 */
	public static int getMaxHCountByLevel(int level) {
		return getLevelInfoByLevel(level).getMax_h();
	}

	/*
	 * 产生一个随机的任务难度
	 */
	public static int getRandomDifficultyAll(int level) {
		LevelInfo levelInfo = getLevelInfoByLevel(level);
		int max = levelInfo.getSimple_pro() + levelInfo.getNormal_pro() + levelInfo.getHard_pro()
				+ levelInfo.getEpic_pro();
		Random ra = new Random();
		int random = ra.nextInt(max) + 1;
		if (random <= levelInfo.getSimple_pro()) {
			return 1;
		} else if (random <= levelInfo.getSimple_pro() + levelInfo.getNormal_pro()) {
			return 2;
		} else if (random <= levelInfo.getSimple_pro() + levelInfo.getNormal_pro() + levelInfo.getHard_pro()) {
			return 3;
		} else {
			return 4;
		}
	}

	/*
	 * 产生一个随机的任务难度（限制在简单和一般难度之间）
	 */
	public static int getRandomDifficulty(int level) {
		LevelInfo levelInfo = getLevelInfoByLevel(level);
		int max = levelInfo.getSimple_pro() + levelInfo.getNormal_pro();
		Random ra = new Random();
		int random = ra.nextInt(max) + 1;
		if (random <= levelInfo.getSimple_pro()) {
			return 1;
		} else {
			return 2;
		}
	}
}