package com.blemobi.task.core;

import java.util.ArrayList;
import java.util.List;

import com.blemobi.library.filter.FromFilter;
import com.blemobi.library.jetty.ServerFilter;
import com.blemobi.task.filter.TokenFilter;

/*
 * 服务启动过滤器配置
 */
public class FilterProperty {
	// 需要验证用户uuid&token的path，对应filter：JettyFilter
	private List<String> tokenPathArray;

	// 需要验证调用服务的path，对应filter：FromFilter
	private List<String> fromPathArray;

	public FilterProperty() {
		tokenPathArray = new ArrayList<String>();
		tokenPathArray.add("/task/user/list");
		tokenPathArray.add("/task/user/level");
		tokenPathArray.add("/task/user/receive");
		tokenPathArray.add("/task/user/reward");

		fromPathArray = new ArrayList<String>();
		// fromPathArray.add("/task/callback/msgid");
	}

	public List<ServerFilter> getFilterList() {
		List<ServerFilter> serverFilterList = new ArrayList<ServerFilter>();

		if (tokenPathArray != null) {
			serverFilterList.add(new ServerFilter(new TokenFilter(), tokenPathArray));
		}
		if (fromPathArray != null) {
			serverFilterList.add(new ServerFilter(new FromFilter(), fromPathArray));
		}

		return serverFilterList;
	}
}
