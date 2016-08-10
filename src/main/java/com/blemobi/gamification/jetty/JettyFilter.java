package com.blemobi.gamification.jetty;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;

import com.blemobi.library.client.AccountHttpClient;
import com.blemobi.library.client.BaseHttpClient;
import com.blemobi.library.exception.BaseException;
import com.blemobi.library.global.PathGlobal;
import com.blemobi.library.util.CommonUtil;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.ResultProtos.PResult;
import com.google.common.base.Strings;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;

import lombok.extern.log4j.Log4j;

/**
 * 过滤器：
 * 
 * 1.记录访问日志
 * 
 * 2.统一验证用户uuid&token是否正确，正确继续执行。错误直接返回client（如果有部分url不需要验证用户uuid&token，
 * 可增加url验证规则， 从前端APP发起的请求都需要验证）
 * 
 * @author andy.zhao@blemobi.com
 */
@Log4j
public class JettyFilter implements Filter {

	/**
	 * 初始化
	 * 
	 */
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	/**
	 * 过滤器
	 * 
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		int errorCode = 1901000;
		String errorMsg = "系统繁忙";

		try {
			HttpServletRequest servletRequest = (HttpServletRequest) request;
			
			String uuid = getCookieFromRequest(servletRequest, "uuid");
			String token = getCookieFromRequest(servletRequest, "token"); 

			if (Strings.isNullOrEmpty(uuid) || Strings.isNullOrEmpty(token)) {
				errorCode = 1901001;
				errorMsg = "uuid或token错误";
			} else {
				boolean b = findUserOpenIdFromAcc(uuid, token);
				if (b) {// 验证通过
					chain.doFilter(request, response);// 继续执行	
				} else {
					errorCode = 1901001;
					errorMsg = "uuid或token错误";
				}
			}
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
		
		responseResult(response, errorCode, errorMsg);
	}

	/**
	 * 过滤器销毁
	 * 
	 */
	public void destroy() {
		log.info("Filter Destroy Finish!");
	}

	/**
	 * 从账户系统校验用户信息是否合法
	 * @param uuid 用户uuid
	 * @param token 用户token
	 * @return boolean 用户信息是否合法
	 * @throws IOException 抛出IOException异常
	 * @throws ClientProtocolException 抛出ClientProtocolException异常 
	 * @throws ChatException 抛出ChatException异常 
	 */
	private boolean findUserOpenIdFromAcc(String uuid, String token) throws ClientProtocolException, IOException, BaseException {
		boolean bool = false;

		Cookie[] cookies = CommonUtil.createLoginCookieParams(uuid, token);
		BaseHttpClient clientUtil = new AccountHttpClient(PathGlobal.GetUser, null, cookies);
		PMessage message = clientUtil.getMethod();

		log.info("从账户系统校验用户uuid["+uuid+"]返回message： " + message);	
		String type = message.getType();
		if ("PUser".equals(type)) {
			bool = true;
		} else if ("PResult".equals(type)) {
			PResult result = PResult.parseFrom(message.getData());
			throw new BaseException(1000, "account system Info:[ErrorCode=" + result.getErrorCode() + ",ErrorMsg=" + result.getErrorMsg() + "]");
		} else {
			throw new BaseException(1000, "account system error");
		}
		
		return bool;
	}

	/**
	 * 返回包处理
	 * @param response response对象
	 * @param errorCode 返回码
	 * @param errorMsg 返回码描述
	 * @return void 返回无
	 * @throws IOException 
	 */
	private void responseResult(ServletResponse response, int errorCode, String errorMsg) throws IOException {
		PMessage message = ReslutUtil.createErrorMessage(errorCode, errorMsg);
		byte[] data = message.toByteArray();

		response.setContentType(MediaTypeExt.APPLICATION_PROTOBUF);
		response.setContentLength(data.length);
		ServletOutputStream out = response.getOutputStream();
		out.write(data);
		out.flush();
		out.close();
	}

	/**
	 * 从cookie中获取参数值
	 * @param request request对象
	 * @param key 参数名称
	 * @return String 参数值
	 */
	private String getCookieFromRequest(HttpServletRequest request, String key) {
		String value = "";
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				if (cookie.getName().equals(key)) {
					value = cookie.getValue();
					break;
				}
			}
		}
		return value;
	}
}
