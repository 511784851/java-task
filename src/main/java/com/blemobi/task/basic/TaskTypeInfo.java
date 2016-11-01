package com.blemobi.task.basic;

import java.util.HashMap;
import java.util.Map;

/*
 * 任务类型
 */
public class TaskTypeInfo {
	// 任务类型ID（消息订阅msgid）
	private int type;
	// 任务逻辑服务器
	private String server;
	// 中文简体
	private String desc_sc;
	// 中文繁体
	private String desc_tc;
	// 英文
	private String desc_en;
	// 韩文
	private String desc_kr;
	// 关联的任务ID
	private Map<Integer, TaskTag> taskidMap = new HashMap<Integer, TaskTag>();

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getDesc_sc() {
		return desc_sc;
	}

	public void setDesc_sc(String desc_sc) {
		this.desc_sc = desc_sc;
	}

	public String getDesc_tc() {
		return desc_tc;
	}

	public void setDesc_tc(String desc_tc) {
		this.desc_tc = desc_tc;
	}

	public String getDesc_en() {
		return desc_en;
	}

	public void setDesc_en(String desc_en) {
		this.desc_en = desc_en;
	}

	public String getDesc_kr() {
		return desc_kr;
	}

	public void setDesc_kr(String desc_kr) {
		this.desc_kr = desc_kr;
	}

	public Map<Integer, TaskTag> getTaskidMap() {
		return taskidMap;
	}

	public void addTaskidMap(int taskid, TaskTag tag) {
		this.taskidMap.put(taskid, tag);
	}

}