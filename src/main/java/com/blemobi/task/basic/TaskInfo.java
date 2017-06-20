package com.blemobi.task.basic;

import java.util.List;

/**
 * 任务信息
 * 
 * @author zhaoyong
 *
 */
public class TaskInfo {
	// ID（小于2000新手任务，大于等于2000每日任务）
	private short ID;
	// 关联消息ID
	private List<Short> msgIDs;
	// 金币
	private short gold;
	// 要求次数
	private byte targ;
	// 可完成次数
	private byte loop;
	// 中文简体
	private String desc_sc;
	// 中文繁体
	private String desc_tc;
	// 英文
	private String desc_en;
	// 韩文
	private String desc_kr;

	public short getID() {
		return ID;
	}

	public void setID(short iD) {
		ID = iD;
	}

	public List<Short> getMsgIDs() {
		return msgIDs;
	}

	public void setMsgIDs(List<Short> msgIDs) {
		this.msgIDs = msgIDs;
	}

	public short getGold() {
		return gold;
	}

	public void setGold(short gold) {
		this.gold = gold;
	}

	public byte getTarg() {
		return targ;
	}

	public void setTarg(byte targ) {
		this.targ = targ;
	}

	public byte getLoop() {
		return loop;
	}

	public void setLoop(byte loop) {
		this.loop = loop;
	}

	public String getDesc_sc() {
		return desc_sc;
	}

	public void setDesc_sc(String desc_sc) {
		this.desc_sc = desc_sc;
	}

	/**
	 * 获得语言对应任务描述（默认简体中文）
	 * 
	 * @param language
	 *            语言
	 * @return
	 */
	public String getDesc(String language) {
		return "zh-tw".equals(language) ? desc_tc
				: "en-us".equals(language) ? desc_en : "ko-kr".equals(language) ? desc_kr : desc_sc;
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

	/**
	 * 是否是新手任务
	 * 
	 * @return
	 */
	public boolean isNocivTask() {
		return ID < 2000 ? true : false;
	}

	/**
	 * 是否是日常任务
	 * 
	 * @return
	 */
	public boolean isDailyTask() {
		return !isNocivTask();
	}

	/**
	 * 获得该任务对应的Redis键
	 * 
	 * @param uuid
	 *            用户uuid
	 * @param dailyTime
	 *            当日时间
	 * @return
	 */
	public String getRedisKey(String uuid, long dailyTime) {
		StringBuilder sb = new StringBuilder("task:daily:");
		sb.append(uuid);
		return isNocivTask() ? sb.toString() : sb.append(":").append(dailyTime).toString();
	}

	@Override
	public String toString() {
		return new StringBuilder().append("ID=").append(ID).append(", msgIDs=").append(msgIDs).append(", gold=")
				.append(gold).append(", targ=").append(targ).append(", loop=").append(loop).append(", desc=")
				.append(desc_sc).toString();
	}

}