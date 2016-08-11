package com.blemobi.gamification.helper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.blemobi.sep.probuf.GamificationProtos.PBadgeDetail;
import com.blemobi.sep.probuf.GamificationProtos.PTaskKey;

import lombok.extern.log4j.Log4j;

/*
 * 徽章访问类
 */
public class BadgeHelper {
	// 获取单条徽章的信息
	public static PBadgeDetail getBadgeDetail(String key) {
		BadgeSingleton badgeSingleton = BadgeSingleton.getInstance();
		Map<String, PBadgeDetail> badgeMap = badgeSingleton.getBadgeMap();
		return badgeMap.get(key);
	}

	// 获取全部的徽章信息
	public static Collection<PBadgeDetail> getBadgeList() {
		BadgeSingleton badgeSingleton = BadgeSingleton.getInstance();
		Map<String, PBadgeDetail> badgeMap = badgeSingleton.getBadgeMap();
		return badgeMap.values();
	}

	// 获取全部徽章的key
	public static Set<String> getBadgeKeys() {
		BadgeSingleton badgeSingleton = BadgeSingleton.getInstance();
		Map<String, PBadgeDetail> badgeMap = badgeSingleton.getBadgeMap();
		return badgeMap.keySet();
	}
}

/*
 * 徽章初始化单利类
 */
@Log4j
class BadgeSingleton {
	private Map<String, PBadgeDetail> badgeMap = new LinkedHashMap<String, PBadgeDetail>();

	// 私有构造方法
	public BadgeSingleton() {
		log.info("开始初始化徽章内容...");
		badgeMap.put(PTaskKey.PUBLISH.toString(), initPBadgeDetail("发帖达人", "", "发帖200篇", 200));
		badgeMap.put(PTaskKey.FOLLOW.toString(), initPBadgeDetail("关注达人", "", "关注1000个", 1000));
		badgeMap.put(PTaskKey.VOTE.toString(), initPBadgeDetail("点赞达人", "", "点赞1500个", 1500));
		badgeMap.put(PTaskKey.REPLY.toString(), initPBadgeDetail("回复达人", "", "回复300个", 300));
		badgeMap.put("COMMENT", initPBadgeDetail("评论达人", "", "评论300个", 300));
		badgeMap.put(PTaskKey.ADDFRIEND.toString(), initPBadgeDetail("加好友达人", "", "加好友200个", 200));
		badgeMap.put(PTaskKey.FORWARD.toString(), initPBadgeDetail("转发达人", "", "转发150篇", 150));
		badgeMap.put("ONLIN", initPBadgeDetail("在线达人", "", "在线120小时", 120));
		badgeMap.put(PTaskKey.ADDCOMMUNITY.toString(), initPBadgeDetail("社区达人", "", "社区20个", 20));
		badgeMap.put("LOGIN", initPBadgeDetail("登录达人", "", "登录60次", 60));
		badgeMap.put(PTaskKey.REDPACKET.toString(), initPBadgeDetail("红包达人", "", "发红包30个", 30));
		badgeMap.put("VIP4", initPBadgeDetail("等级4", "", "经验等级4", 4));
		badgeMap.put("VIP5", initPBadgeDetail("等级5", "", "经验等级5", 5));
		badgeMap.put("VIP6", initPBadgeDetail("等级6", "", "经验等级6", 6));
	}

	private PBadgeDetail initPBadgeDetail(String name, String icon, String description, int target) {
		return PBadgeDetail.newBuilder().setName(name).setIcon(icon).setDescription(description).setTarget(target)
				.build();
	}

	/* 使用一个内部类来维护单例 */
	private static class SingletonFactory {
		private static BadgeSingleton instance = new BadgeSingleton();
	}

	// 外部获取本类对象
	public static BadgeSingleton getInstance() {
		return SingletonFactory.instance;
	}

	// 获取徽章信息
	public Map<String, PBadgeDetail> getBadgeMap() {
		return badgeMap;
	}
}
