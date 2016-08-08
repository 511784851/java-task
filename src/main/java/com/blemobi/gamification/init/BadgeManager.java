package com.blemobi.gamification.init;

import java.util.ArrayList;
import java.util.List;

import com.blemobi.sep.probuf.GamificationProtos.PBadgeDetail;

public class BadgeManager {

	private static List<PBadgeDetail> badgeList = new ArrayList<PBadgeDetail>();

	// 初始化徽章内容
	public void init() {
		badgeList.add(initPBadgeDetail("发帖达人", "", "发帖200篇", 200));
		badgeList.add(initPBadgeDetail("关注达人", "", "关注1000个", 1000));
		badgeList.add(initPBadgeDetail("点赞达人", "", "点赞1500个", 1500));
		badgeList.add(initPBadgeDetail("回复达人", "", "回复300个", 300));
		badgeList.add(initPBadgeDetail("评论达人", "", "评论300个", 300));
		badgeList.add(initPBadgeDetail("加好友达人", "", "加好友200个", 200));
		badgeList.add(initPBadgeDetail("转发达人", "", "转发150篇", 150));
		badgeList.add(initPBadgeDetail("在线达人", "", "在线120小时", 120));
		badgeList.add(initPBadgeDetail("社区达人", "", "社区20个", 20));
		badgeList.add(initPBadgeDetail("登录达人", "", "登录60次", 60));
		badgeList.add(initPBadgeDetail("红包达人", "", "发红包30个", 30));
		badgeList.add(initPBadgeDetail("等级4", "", "经验等级4", 4));
		badgeList.add(initPBadgeDetail("等级5", "", "经验等级5", 5));
		badgeList.add(initPBadgeDetail("等级6", "", "经验等级6", 6));
	}

	private PBadgeDetail initPBadgeDetail(String name, String icon, String description, int target) {
		return PBadgeDetail.newBuilder()
				.setName(name)
				.setIcon(icon)
				.setDescription(description)
				.setTarget(target)
				.build();
	}

	public static List<PBadgeDetail> getBadgeList() {
		return badgeList;
	}
}
