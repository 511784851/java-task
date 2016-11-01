package com.blemobi.task.basic;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import com.blemobi.task.util.Constant;

import lombok.extern.log4j.Log4j;

/*
 * 经验等级管理类
 */
@Log4j
public class LevelHelper {
	// 最大经验等级（从1开始）
	private static final int maxLevel = 9;
	// 经验等级对应经验值（levelArray第0行：最小经验值；第1行：最大经验值；第2行：可接最大日常任务数量）
	private static long[][] levelArray = new long[2][maxLevel + 1];
	// 经验等级对应可接最大日常任务数量
	private static int[] maxArray = new int[maxLevel + 1];

	// 从配置文件中缓存数据
	static {
		try {
			Configuration config = new XMLConfiguration(TaskHelper.class.getResource(Constant.BASIC_LEVEL_DATA_CONFIG));
			List<?> ids = config.getList("levels.level.id");
			List<?> minexps = config.getList("levels.level.minexp");
			List<?> maxexps = config.getList("levels.level.maxexp");
			List<?> nums = config.getList("levels.level.num");
			for (int i = 0; i < ids.size(); i++) {
				int id = Integer.parseInt(ids.get(i).toString());
				levelArray[0][id] = Long.parseLong(minexps.get(i).toString());
				levelArray[1][id] = Long.parseLong(maxexps.get(i).toString());
				maxArray[id] = Integer.parseInt(nums.get(i).toString());
			}
		} catch (ConfigurationException e) {
			log.error("读取经验等级配置信息异常：" + e.getMessage());
			e.printStackTrace();
		}

		for (int row = 0; row < 2; row++) {
			for (int level = 1; level <= maxLevel; level++) {
				System.out.print(levelArray[row][level] + ",");
			}
			System.out.println();
		}

		for (int level = 1; level <= maxLevel; level++) {
			System.out.print(maxArray[level] + ",");
		}
		System.out.println();
	}

	/*
	 * 获取经验值对应等级
	 */
	public static int getLevelByExp(long exp) {
		for (int level = 1; level <= maxLevel; level++) {
			if (Math.max(levelArray[0][level], exp) == Math.min(exp, levelArray[1][level])) {
				return level;
			}
		}
		return 1;
	}

	/*
	 * 获取等级的下一个经验等级
	 */
	public static int getNextLevelByLevel(int level) {
		return ++level >= maxLevel ? maxLevel : level;
	}

	/*
	 * 获取经验等级的最小经验值
	 */
	public static long getMinExpByLevel(int level) {
		return levelArray[0][level];
	}

	/*
	 * 获取等级对应的可接最大日常任务数量
	 */
	public static int getMaxCountByLevel(int level) {
		return maxArray[level];
	}
}