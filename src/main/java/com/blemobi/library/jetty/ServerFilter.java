package com.blemobi.library.jetty;

import java.util.List;

import javax.servlet.Filter;

public class ServerFilter {
	private Filter filter;
	private List<String> pathList;

	public ServerFilter(Filter filter, List<String> pathList) {
		this.filter = filter;
		this.pathList = pathList;
	}

	public Filter getFilter() {
		return filter;
	}

	public List<String> getPathList() {
		return pathList;
	}

}
