package com.blemobi.task.basic;

/*
 * 任务难度
 */
public class Difficulty {
	// 难度ID
	private int id;
	// 描述
	private String desc;
	// 经验值变化
	private int exp;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

}