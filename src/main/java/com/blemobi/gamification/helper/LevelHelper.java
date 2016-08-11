package com.blemobi.gamification.helper;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j;

/*
 * 等级访问类
 */
public class LevelHelper {
	// 获取经验值对应的等级信息
	public static LevelExperience getLevel(long experience) {
		LevelSingleton levelSingleton = LevelSingleton.getInstance();
		List<LevelExperience> levelList = levelSingleton.getLevelList();
		for (LevelExperience le : levelList) {
			if (experience >= le.getMinExperience() && experience <= le.getMaxExperience()) {
				return le;
			}
		}
		return levelList.get(levelList.size());
	}

	// 获取等级的起始经验值
	public static long getNextLevelExperience(int level) {
		LevelSingleton levelSingleton = LevelSingleton.getInstance();
		List<LevelExperience> levelList = levelSingleton.getLevelList();
		for (LevelExperience le : levelList) {
			if (le.getLevel() == level) {
				return le.getMinExperience();
			}
		}
		return 0;
	}
}

/*
 * 等级初始化单利类
 */
@Log4j
class LevelSingleton {
	private List<LevelExperience> levelList = new ArrayList<LevelExperience>();

	// 私有构造方法
	private LevelSingleton() {
		log.info("开始初始化等级内容...");
		levelList.add(new LevelExperience(0, "注册用户", 0, 399));
		levelList.add(new LevelExperience(1, "等级1", 400, 549));
		levelList.add(new LevelExperience(2, "等级2", 550, 799));
		levelList.add(new LevelExperience(3, "等级3", 800, 1199));
		levelList.add(new LevelExperience(4, "等级4", 1200, 2199));
		levelList.add(new LevelExperience(5, "等级5", 2200, 7199));
		levelList.add(new LevelExperience(6, "等级6", 7200, 8199));
	}

	/* 使用一个内部类来维护单例 */
	private static class SingletonFactory {
		private static LevelSingleton instance = new LevelSingleton();
	}

	// 外部获取本类对象
	public static LevelSingleton getInstance() {
		return SingletonFactory.instance;
	}

	// 获取等级信息
	public List<LevelExperience> getLevelList() {
		return levelList;
	}
}