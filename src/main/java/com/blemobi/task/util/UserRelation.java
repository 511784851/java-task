package com.blemobi.task.util;

import java.util.ArrayList;
import java.util.List;

import com.blemobi.sep.probuf.AccountProtos.ELevelType;

public class UserRelation {
	public static List<Integer> levelList;

	/*
	 * 用户角色的允许范围
	 */
	static {
		levelList = new ArrayList<Integer>();
		levelList.add(ELevelType.User_VALUE);
		levelList.add(ELevelType.Vip_VALUE);
		levelList.add(ELevelType.Vipp_VALUE);
	}
}