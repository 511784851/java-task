package com.blemobi.task.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.blemobi.library.util.CommonUtil;
import com.blemobi.library.util.ReslutUtil;
import com.google.common.base.Strings;

/**
 * 过滤器：
 * 
 * @author andy.zhao@blemobi.com
 */
public class TokenFilter implements Filter {

	public void init(FilterConfig filterConfig) throws ServletException {

	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		String uuid = CommonUtil.getCookieParam(httpServletRequest, "uuid");
		String token = CommonUtil.getCookieParam(httpServletRequest, "token");

		if (Strings.isNullOrEmpty(uuid) || Strings.isNullOrEmpty(token)) {
			ReslutUtil.createResponse(response, 1901001, "uuid or token is null");
		}

		// 继续执行
		chain.doFilter(request, response);
	}

	public void destroy() {

	}
}