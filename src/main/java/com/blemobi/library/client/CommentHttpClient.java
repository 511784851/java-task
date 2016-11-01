package com.blemobi.library.client;

import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.http.NameValuePair;

import com.blemobi.library.consul.BaseService;
import com.blemobi.library.consul.SocketInfo;

/**
 * @author 赵勇<andy.zhao@blemobi.com> 评论系统调用类
 */
public class CommentHttpClient extends BaseHttpClient {
	public CommentHttpClient(String basePath, List<NameValuePair> params, Cookie[] cookies) {
		super(basePath, params, cookies);
		SocketInfo socketInfo = BaseService.getActiveServer("comment");
		super.socketInfo = socketInfo;
		super.createUrl();
	}
}