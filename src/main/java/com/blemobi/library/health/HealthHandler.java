package com.blemobi.library.health;

/**
 * @author 李子才<davis.lee@blemobi.com>
 * 这是聊天系统的健康服务发现的实现类，它是实现了HttpHandler接口。
 */

import java.io.IOException;
import java.io.OutputStream;

import com.pakulov.jersey.protobuf.internal.MediaTypeExt;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HealthHandler implements HttpHandler {

	private String serviceName = "";

	public HealthHandler(String serviceName) {
		this.serviceName = serviceName;
	}

	public void handle(HttpExchange t) throws IOException {
		String resp = readRespData();
		byte[] pMessage = resp.getBytes();
		t.sendResponseHeaders(200, pMessage.length);
		Headers h = t.getResponseHeaders();
		// 根据系统管理员要求，必须返回json格式的内容。因此在http的head里，要声明内容类型。
		h.set("Content-Type", MediaTypeExt.APPLICATION_JSON);
		OutputStream os = t.getResponseBody();
		os.write(pMessage);
		os.close();
	}

	// 根据系统管理员的要求，返回固定的json格式内容。
	private String readRespData() {
		return "{\"serviceName\" : \"" + this.serviceName + "\",\"isHealthy\" : true } ";
	}
}
