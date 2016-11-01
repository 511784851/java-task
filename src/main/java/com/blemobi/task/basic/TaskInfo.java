package com.blemobi.task.basic;

/*
 * 任务信息
 */
public class TaskInfo {
	// 任务ID
	private int taskid;
	// 任务要求等级
	private int level;
	// 任务完成获得经验
	private long exp;
	// 任务类型ID（消息订阅ID）
	private int type;
	// 任务数量
	private int num;
	// 任务接取依赖条件（"|"或"&"）
	private char logic;
	// 任务接取依赖别的任务
	private int[] depend;
	// 任务描述
	private String desc;

	public int getTaskid() {
		return taskid;
	}

	public void setTaskid(int taskid) {
		this.taskid = taskid;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public long getExp() {
		return exp;
	}

	public void setExp(long exp) {
		this.exp = exp;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public char getLogic() {
		return logic;
	}

	public void setLogic(char logic) {
		this.logic = logic;
	}

	public int[] getDepend() {
		return depend;
	}

	public void setDepend(int[] depend) {
		this.depend = depend;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

}