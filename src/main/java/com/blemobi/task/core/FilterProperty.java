package com.blemobi.task.core;

import java.util.ArrayList;
import java.util.List;

import com.blemobi.library.filter.FromFilter;
import com.blemobi.library.jetty.ServerFilter;
import com.blemobi.task.filter.TokenFilter;

/**
 * 服务启动过滤器配置
 * 
 * @author zhaoyong
 *
 */
public class FilterProperty {
	/**
	 * 需要验证用户uuid&token的path，对应filter：JettyFilter
	 */
	private List<String> tokenPathArray;

	/**
	 * 需要验证调用服务的path，对应filter：FromFilter
	 */
	private List<String> fromPathArray;

	/**
	 * 构造方法
	 */
	public FilterProperty() {
		tokenPathArray = new ArrayList<String>();
		tokenPathArray.add("/v1/task/gold/list");
		tokenPathArray.add("/v1/task/gold/receive");
		tokenPathArray.add("/v1/task/gold/details");
		tokenPathArray.add("/v1/task/gold/operation");

		fromPathArray = new ArrayList<String>();
	}

	/**
	 * 获取过滤器配置
	 * 
	 * @return
	 */
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
