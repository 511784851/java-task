package com.blemobi.task.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.blemobi.library.util.CommonUtil;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.AccountProtos.ELevelType;
import com.blemobi.sep.probuf.AccountProtos.PUser;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.task.notify.UserRelation;
import com.google.common.base.Strings;

/**
 * 过滤器：
 * 
 * @author andy.zhao@blemobi.com
 */
public class TokenFilter implements Filter {

	/// 允许的用户角色
	private List<Integer> levelList;

	public void init(FilterConfig filterConfig) throws ServletException {
		levelList = new ArrayList<Integer>();
		levelList.add(ELevelType.User_VALUE);
		levelList.add(ELevelType.Vip_VALUE);
		levelList.add(ELevelType.Vipp_VALUE);
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		String uuid = CommonUtil.getCookieParam(httpServletRequest, "uuid");
		String token = CommonUtil.getCookieParam(httpServletRequest, "token");

		if (Strings.isNullOrEmpty(uuid) || Strings.isNullOrEmpty(token)) {
			ReslutUtil.createResponse(response, 1901001, "uuid or token is null");
		}

		PMessage message = UserRelation.getUserInfo(uuid);
		if (!"PUser".equals(message.getType())) {
			ReslutUtil.createResponse(response, 1901001, "用户不存在");
		}

		PUser user = PUser.parseFrom(message.getData());
		int levelType = user.getLevelInfo().getLevelType();
		if (levelList.contains(levelType)) {
			ReslutUtil.createResponse(response, 1901001, "没有权限使用任务系统");
		}

		// 继续执行
		request.setAttribute("nickname", user.getNickname());
		request.setAttribute("headimg", user.getHeadImgURL());
		chain.doFilter(request, response);
	}

	public void destroy() {

	}
}