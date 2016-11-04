package com.blemobi.task.basic;

import java.util.ArrayList;
import java.util.List;

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
	private List<Integer> taskidList = new ArrayList<Integer>();

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

	public List<Integer> getTaskidList() {
		return taskidList;
	}

	public void addTaskidList(int taskid) {
		this.taskidList.add(taskid);
	}

	public String getDesc(String language, int... params) {
		if ("zh-tw".equals(language))
			return desc_tc.replace("%d", params[0] + "");// 中文繁体
		else if ("en-us".equals(language))
			return desc_en.replace("%d", params[0] + "");// 英文
		else if ("ko-kr".equals(language))
			return desc_kr.replace("%d", params[0] + "");// 韩文
		else
			return desc_sc.replace("%d", params[0] + "");// 中文简体（默认）
	}
}