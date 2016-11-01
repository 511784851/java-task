package com.blemobi.task.basic;

import java.util.Collection;

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
	 * 获取等级的下一个经验等级信息
	 */
	public static LevelInfo getNextLevelByLevel(int level) {
		int nextLevel = level + 1;
		LevelInfo levelInfo = BasicData.levelMap.get(nextLevel);
		if (levelInfo != null) {
			return levelInfo;
		} else {
			return BasicData.levelMap.get(level);
		}
	}

	/*
	 * 获取经验等级的最小经验值
	 */
	public static long getMinExpByLevel(int level) {
		return BasicData.levelMap.get(level).getExp_min();
	}

	/*
	 * 获取等级对应的可接最大日常任务数量
	 */
	public static int getMaxCountByLevel(int level) {
		return BasicData.levelMap.get(level).getMax();
	}

	/*
	 * 获取全部等级数据
	 */
	public static Collection<LevelInfo> getAllLevelList() {
		return BasicData.levelMap.values();
	}
}