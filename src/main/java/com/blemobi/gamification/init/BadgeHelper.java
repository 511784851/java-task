package com.blemobi.gamification.init;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.blemobi.sep.probuf.GamificationProtos.PBadgeDetail;
import com.blemobi.sep.probuf.GamificationProtos.PTaskKey;

public class BadgeHelper {

	private static Map<String, PBadgeDetail> badgeMap = new LinkedHashMap<String, PBadgeDetail>();

	// 初始化徽章内容
	public void init() {	
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
		return PBadgeDetail.newBuilder()
				.setName(name)
				.setIcon(icon)
				.setDescription(description)
				.setTarget(target)
				.build();
	}

	public static PBadgeDetail getBadgeDetail(String key) {
		return badgeMap.get(key);
	}

	public static Collection<PBadgeDetail> getBadgeList() {
		return badgeMap.values();
	}

	public static Set<String> getBadgeKeys() {
		return badgeMap.keySet();
	}
}
