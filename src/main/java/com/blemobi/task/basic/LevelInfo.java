package com.blemobi.task.basic;

/*
 * 等级信息
 */
public class LevelInfo {
	// 等级ID
	private int level;
	// 经验值范围（小）
	private long exp_min;
	// 经验值范围（大）
	private long exp_max;
	// 可接最大日常任务数
	private int max;
	// 中文简体
	private String title_sc;
	// 中文繁体
	private String title_tc;
	// 英文
	private String title_en;
	// 韩文
	private String title_kr;
	// 可接最大困难+史诗的个数
	private int max_h;
	// 简单概率
	private int simple_pro;
	// 一般概率
	private int normal_pro;
	// 困难概率
	private int hard_pro;
	// 史诗概率
	private int epic_pro;

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public long getExp_min() {
		return exp_min;
	}

	public void setExp_min(long exp_min) {
		this.exp_min = exp_min;
	}

	public long getExp_max() {
		return exp_max;
	}

	public void setExp_max(long exp_max) {
		this.exp_max = exp_max;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public String getTitle_sc() {
		return title_sc;
	}

	public void setTitle_sc(String title_sc) {
		this.title_sc = title_sc;
	}

	public String getTitle_tc() {
		return title_tc;
	}

	public void setTitle_tc(String title_tc) {
		this.title_tc = title_tc;
	}

	public String getTitle_en() {
		return title_en;
	}

	public void setTitle_en(String title_en) {
		this.title_en = title_en;
	}

	public String getTitle_kr() {
		return title_kr;
	}

	public void setTitle_kr(String title_kr) {
		this.title_kr = title_kr;
	}

	public String getTitle(String language) {
		if ("zh-tw".equals(language))
			return title_tc;// 中文繁体
		else if ("en-us".equals(language))
			return title_en;// 英文
		else if ("ko-kr".equals(language))
			return title_kr;// 韩文
		else
			return title_sc;// 中文简体（默认）
	}

	public int getMax_h() {
		return max_h;
	}

	public void setMax_h(int max_h) {
		this.max_h = max_h;
	}

	public int getSimple_pro() {
		return simple_pro;
	}

	public void setSimple_pro(int simple_pro) {
		this.simple_pro = simple_pro;
	}

	public int getNormal_pro() {
		return normal_pro;
	}

	public void setNormal_pro(int normal_pro) {
		this.normal_pro = normal_pro;
	}

	public int getHard_pro() {
		return hard_pro;
	}

	public void setHard_pro(int hard_pro) {
		this.hard_pro = hard_pro;
	}

	public int getEpic_pro() {
		return epic_pro;
	}

	public void setEpic_pro(int epic_pro) {
		this.epic_pro = epic_pro;
	}

}