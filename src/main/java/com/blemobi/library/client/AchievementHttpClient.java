package com.blemobi.library.client;

import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.http.NameValuePair;

import com.blemobi.library.consul.BaseService;
import com.blemobi.library.consul.SocketInfo;

/**
 * @author 赵勇<andy.zhao@blemobi.com> 账户系统调用类
 */
public class AchievementHttpClient extends BaseHttpClient {
	public AchievementHttpClient(String basePath, List<NameValuePair> params, Cookie[] cookies) {
		super(basePath, params, cookies);
		SocketInfo socketInfo = BaseService.getActiveServer("achievement");
		super.socketInfo = socketInfo;
		super.createUrl();
	}
}