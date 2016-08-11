package com.blemobi.gamification.helper;

public class LevelExperience {
	private int level;
	private String name;
	private long minExperience;
	private long maxExperience;

	public LevelExperience(int level, String name, int minExperience, int maxExperience) {
		this.level = level;
		this.name = name;
		this.minExperience = minExperience;
		this.maxExperience = maxExperience;
	}

	public int getLevel() {
		return level;
	}

	public String getName() {
		return name;
	}

	public long getMinExperience() {
		return minExperience;
	}

	public long getMaxExperience() {
		return maxExperience;
	}
}