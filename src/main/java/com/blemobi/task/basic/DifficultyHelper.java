package com.blemobi.task.basic;

/*
 * 经验等级管理类
 */
public class DifficultyHelper {
	/*
	 * 根据难度ID返回经验变化值
	 */
	public static int getDExpById(int id) {
		return BasicData.difficultyMap.get(id).getExp();
	}
}