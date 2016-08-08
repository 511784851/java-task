package com.blemobi.gamification.init;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.blemobi.sep.probuf.GamificationProtos.PTaskDetail;
import com.blemobi.sep.probuf.GamificationProtos.PTaskKey;

public class TaskManager {

	private static Map<PTaskKey, PTaskDetail> taskMap = new HashMap<PTaskKey, PTaskDetail>();

	// 初始化任务内容
	public void init() {
		
		putTaskMap(initPTaskDetail(1, PTaskKey.PUBLISH, "发帖", getRandom(), 10));// 发帖
		putTaskMap(initPTaskDetail(1, PTaskKey.FOLLOW, "关注用户", getRandom(), 10));// 关注用户
		putTaskMap(initPTaskDetail(1, PTaskKey.VOTE, "点赞帖子", getRandom(), 5));// 点赞帖子
		putTaskMap(initPTaskDetail(1, PTaskKey.REPLY, "回复帖子", getRandom(), 10)); // 回复帖子
		putTaskMap(initPTaskDetail(1, PTaskKey.ADDCOMMUNITY, "加入社区", getRandom(), 5));// 加入社区
		putTaskMap(initPTaskDetail(1, PTaskKey.PUBLISHCOMMUNITY, "论坛发帖", getRandom(), 15));// 论坛发帖
		putTaskMap(initPTaskDetail(1, PTaskKey.ADDFRIEND, "加平台好友", getRandom(), 5)); // 加平台好友
		putTaskMap(initPTaskDetail(1, PTaskKey.FORWARD, "转发帖子", getRandom(), 10)); // 转发帖子
		putTaskMap(initPTaskDetail(1, PTaskKey.REDPACKET, "发红包", getRandom(), 40));// 发红包
		putTaskMap(initPTaskDetail(1, PTaskKey.REMIND, "@人", getRandom(), 10)); // @人
		putTaskMap(initPTaskDetail(2, PTaskKey.PROFILE, "资料完善", getRandom(), 10)); // 资料完善
		putTaskMap(initPTaskDetail(2, PTaskKey.OPENCONTACT, "开启手机通讯录访问权限", getRandom(), 30)); // 开启手机通讯录访问权限
		//initPTaskDetail(1, PTaskKey.ADDCONTACT, "加手机通讯录好友", experience);// 加手机通讯录好友
		//initPTaskDetail(1, PTaskKey.FEEDBACK, "意见反馈", experience); // 意见反馈
		//initPTaskDetail(1, PTaskKey.BINDACCOUNT, "绑定第三方账号", experience);// 绑定第三方账号
		//initPTaskDetail(1, PTaskKey.DOWNLOADAPP, "下载机器人助手", experience);// 下载机器人助手
		//initPTaskDetail(1, PTaskKey.ADDOTHERFRIEND, "加第三方方平台好友", experience);// 加第三方方平台好友
	}

	private void putTaskMap(PTaskDetail taskDetail) {
		taskMap.put(taskDetail.getTaskKey(), taskDetail);
	}
	
	private PTaskDetail initPTaskDetail(int type, PTaskKey taskKey, String description, int targe, int experience) {
		return PTaskDetail.newBuilder()
				.setType(type)
				.setTaskKey(taskKey)
				.setDescription(description)
				.setTarget(targe)
				.setExperience(experience)
				.build();

	}
	
	public static PTaskDetail getTaskDetail(PTaskKey taskKey){
		return taskMap.get(taskKey);
	}

	public static Collection<PTaskDetail> getTaskList(){
		return taskMap.values();
	}
	
	private int getRandom() {
		Random ra = new Random();
		return ra.nextInt(3)+1;
	}
}
