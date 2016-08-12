package com.blemobi.gamification.helper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import com.blemobi.sep.probuf.GamificationProtos.PTaskDetail;
import com.blemobi.sep.probuf.GamificationProtos.PTaskKey;

import lombok.extern.log4j.Log4j;

/*
 * 任务访问类
 */
public class TaskHelper {
	// 获取单条任务信息
	public static PTaskDetail getTaskDetail(String taskKey) {
		TaskSingleton taskSingleton = TaskSingleton.getInstance();
		Map<String, PTaskDetail> taskMap = taskSingleton.getTaskMap();
		return taskMap.get(taskKey);
	}

	// 获取全部任务信息
	public static Collection<PTaskDetail> getTaskList() {
		TaskSingleton taskSingleton = TaskSingleton.getInstance();
		Map<String, PTaskDetail> taskMap = taskSingleton.getTaskMap();
		return taskMap.values();
	}
}

/*
 * 任务初始化单利类
 */
@Log4j
class TaskSingleton {
	private Map<String, PTaskDetail> taskMap = new LinkedHashMap<String, PTaskDetail>();

	// 私有构造方法
	private TaskSingleton() {
		log.info("开始初始化任务内容...");
		putTaskMap(initPTaskDetail(1, PTaskKey.PUBLISH, "发帖注用户", getRandom(), 10));// 发帖
		putTaskMap(initPTaskDetail(1, PTaskKey.FOLLOW, "关注用户", getRandom(), 10));// 关注用户
		putTaskMap(initPTaskDetail(1, PTaskKey.VOTE, "点赞帖子", getRandom(), 5));// 点赞帖子
		putTaskMap(initPTaskDetail(1, PTaskKey.REPLY, "回复帖子", getRandom(), 10)); // 回复帖子
		putTaskMap(initPTaskDetail(1, PTaskKey.ADDCOMMUNITY, "加入社区", getRandom(), 5));// 加入社区
		putTaskMap(initPTaskDetail(1, PTaskKey.PUBLISHCOMMUNITY, "论坛发帖", getRandom(), 15));// 论坛发帖
		putTaskMap(initPTaskDetail(1, PTaskKey.ADDFRIEND, "加平台好友", getRandom(), 5)); // 加平台好友
		putTaskMap(initPTaskDetail(1, PTaskKey.FORWARD, "转发帖子", getRandom(), 10)); // 转发帖子
		putTaskMap(initPTaskDetail(1, PTaskKey.REDPACKET, "发红包", getRandom(), 40));// 发红包
		putTaskMap(initPTaskDetail(1, PTaskKey.REMIND, "@人", getRandom(), 10)); // @人
		putTaskMap(initPTaskDetail(2, PTaskKey.PROFILE, "资料完善", 1, 10)); // 资料完善
		putTaskMap(initPTaskDetail(2, PTaskKey.OPENCONTACT, "开启手机通讯录访问权限", 1, 30)); // 开启手机通讯录访问权限
		putTaskMap(initPTaskDetail(3, PTaskKey.REGISTER, "注册", 1, 399)); // 注册
		putTaskMap(initPTaskDetail(3, PTaskKey.VIP, "成为VIP", 1, 20)); // 成为VIP

		putTaskMap(initPTaskDetail(1, PTaskKey.ADDCONTACT, "加手机通讯录好友", 1, 0));// 加手机通讯录好友
		putTaskMap(initPTaskDetail(2, PTaskKey.FEEDBACK, "意见反馈", 1, 0)); // 意见反馈
		putTaskMap(initPTaskDetail(2, PTaskKey.BINDACCOUNT, "绑定第三方账号", 1, 0));// 绑定第三方账号
		putTaskMap(initPTaskDetail(2, PTaskKey.DOWNLOADAPP, "下载机器人助手", 1, 0));// 下载机器人助手
		putTaskMap(initPTaskDetail(1, PTaskKey.ADDOTHERFRIEND, "加第三方方平台好友", 1, 0));// 加第三方方平台好友
	}

	private void putTaskMap(PTaskDetail taskDetail) {
		taskMap.put(taskDetail.getTaskKey().toString(), taskDetail);
	}

	private PTaskDetail initPTaskDetail(int type, PTaskKey taskKey, String description, int targe, int experience) {
		PTaskDetail taskDetail = PTaskDetail.newBuilder()
				.setType(type)
				.setTaskKey(taskKey)
				.setDescription(description)
				.setTarget(targe)
				.setExperience(experience)
				.build();
		
		return taskDetail;
	}

	/* 使用一个内部类来维护单例 */
	private static class SingletonFactory {
		private static TaskSingleton instance = new TaskSingleton();
	}

	// 外部获取本类对象
	public static TaskSingleton getInstance() {
		return SingletonFactory.instance;
	}

	// 获取任务信息
	public Map<String, PTaskDetail> getTaskMap() {
		return taskMap;
	}

	// 生成每日任务进度的随机数
	private int getRandom() {
		Random ra = new Random();
		return 3;// ra.nextInt(3)+1;
	}
}
