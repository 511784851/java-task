package com.blemobi.library.client;

import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.http.NameValuePair;

import com.blemobi.library.consul.BaseService;
import com.blemobi.library.consul.SocketInfo;

/**
 * @author 赵勇<andy.zhao@blemobi.com> 社区系统调用类
 */
public class CommunityHttpClient extends BaseHttpClient {
	public CommunityHttpClient(String basePath, List<NameValuePair> params, Cookie[] cookies) {
		super(basePath, params, cookies);
		SocketInfo socketInfo = BaseService.getActiveServer("community");
		super.socketInfo = socketInfo;
		super.createUrl();
	}
}