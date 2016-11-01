package com.blemobi.library.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.log4j.Log4j;


@Log4j
public class ListParamFilter implements Filter {
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		HttpServletRequest servletRequest = (HttpServletRequest) request;
		printRequestInfo(servletRequest);// 访问日志
		chain.doFilter(request, response);
	}

	public void destroy() {
		// TODO Auto-generated method stub
		
	}
	/**
	 * 访问日志
	 * @param request
	 * @return void
	 */
	private void printRequestInfo(HttpServletRequest request) {
		StringBuffer sb = new StringBuffer();
		sb.append("\r\n-------------Print Param Start---------------\r\n");
		sb.append("Recv Client, ip=[" + request.getRemoteAddr() + "]\r\n");
		sb.append("Request URL=[" + request.getRequestURL().toString() + "]\r\n");
		sb.append("Request Method=[" + request.getMethod() + "]\r\n");
		if(request.getParameterMap()!=null){
			for (String key : request.getParameterMap().keySet()) {
				sb.append("Param " + key + "=[" + request.getParameter(key) + "]\r\n");
			}
		}
		if(request.getCookies()!=null){
			for (Cookie cookie : request.getCookies()) {
				sb.append("Cookie " + cookie.getName() + "=[" + cookie.getValue() + "]\r\n");
			}
		}
		
		sb.append("-------------Print Param End---------------\r\n");
		log.debug(sb.toString());
	}
	
}
