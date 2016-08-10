package com.blemobi.gamification.init;

import java.util.ArrayList;
import java.util.List;

public class LevelHelper {

	private static List<LevelExperience> levelList = new ArrayList<LevelExperience>();

	// 初始化徽章内容
	public void init() {
		levelList.add(new LevelExperience(0, "注册用户", 0, 399));
		levelList.add(new LevelExperience(1, "等级1", 400, 549));
		levelList.add(new LevelExperience(2, "等级2", 550, 799));
		levelList.add(new LevelExperience(3, "等级3", 800, 1199));
		levelList.add(new LevelExperience(4, "等级4", 1200, 2199));
		levelList.add(new LevelExperience(5, "等级5", 2200, 7199));
		levelList.add(new LevelExperience(6, "等级6", 7200, 8199));
	}

	public static LevelExperience getLevel(long experience) {
		for (LevelExperience le : levelList) {
			if (experience >= le.getMinExperience() && experience <= le.getMaxExperience()) {
				return le;
			}
		}
		return levelList.get(levelList.size());
	}

	public static long getNextLevelExperience(int level) {
		for (LevelExperience le : levelList) {
			if (le.getLevel() == level) {
				return le.getMinExperience();
			}
		}
		return 0;
	}
}