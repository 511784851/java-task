package com.blemobi.gamification.core;

import com.blemobi.gamification.jetty.JettyServer;
import com.blemobi.library.consul.BaseService;
import com.blemobi.library.consul.ConsulManager;
import com.blemobi.library.global.Constant;
import com.blemobi.library.health.HealthManager;

import lombok.extern.log4j.Log4j;

@Log4j
public class GamificationManager {

	public static void main(String[] args) throws Exception {
		String selfName = "gamification";
		String packages = "com.blemobi." + selfName + ".rest";

		// 初始化Consul，获取连接Consul服务器的间隔时间
		long consulIntervalTime = Constant.getConsulIntervaltime();

		// 初始化Consul, 确定连接了正确的服务器环境（本地测试，北美测试服务器，或者是生产服务器）
		ConsulManager.startService(selfName, args, consulIntervalTime); // 启动连接Consul服务

		log.info("Starting Gamification Server ...");

		// 启动Jetty HTTP服务器
		String jetty_port = BaseService.getProperty("jetty_port");
		int port = Integer.parseInt(jetty_port);
		log.info("Gamification Server Running Port:" + jetty_port);
		JettyServer jettyServer = new JettyServer();
		jettyServer.startServer(port, packages, selfName);

		// 发布Consul的健康发现
		String health_check_port = BaseService.getProperty("health_check_port");
		int check_port = Integer.parseInt(health_check_port);
		HealthManager.startService(check_port, selfName);

		/*
		 * 1）从Consul读取信息 2) 开启Jetty HTTP服务器 3) 通过protobuff连接帐户服务器 4) 连接wukong
		 */
		log.info("Start Gamification Server Finish!");
		log.info("Waiting client connect...");
	}

}
