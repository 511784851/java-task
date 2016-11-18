package com.blemobi.task.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.blemobi.library.redis.RedisManager;
import com.blemobi.library.util.CommonUtil;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.AccountProtos.PUser;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.task.util.Constant;
import com.blemobi.task.util.TaskUtil;
import com.blemobi.task.util.UserRelation;
import com.google.common.base.Strings;

import redis.clients.jedis.Jedis;

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
			ReslutUtil.createResponse(response, 1001006, "uuid or token is null");
		}

		String userInfoKey = Constant.GAME_USER_INFO + uuid;
		Jedis jedis = RedisManager.getRedis();
		boolean bool = jedis.exists(userInfoKey);
		RedisManager.returnResource(jedis);
		if (!bool) {// 未初始化
			PMessage message = UserRelation.getUserInfo(uuid);
			if (!"PUser".equals(message.getType())) {
				ReslutUtil.createResponse(response, 1001006, "用户不存在");
			}

			PUser user = PUser.parseFrom(message.getData());
			int levelType = user.getLevelInfo().getLevelType();
			if (!UserRelation.levelList.contains(levelType)) {
				ReslutUtil.createResponse(response, 2201000, "没有权限使用任务系统");
			}

			TaskUtil taskUtil = new TaskUtil(uuid, user);
			taskUtil.init();
		}

		// 继续执行
		chain.doFilter(request, response);
	}

	public void destroy() {

	}
}