package com.blemobi.gamification.core;

import com.blemobi.gamification.init.BadgeHelper;
import com.blemobi.gamification.init.LevelHelper;
import com.blemobi.gamification.init.TaskHelper;
import com.blemobi.gamification.jetty.JettyServer;
import com.blemobi.library.consul.ConsulManager;
import com.blemobi.library.global.Constant;
import com.blemobi.library.health.HealthManager;

import lombok.extern.log4j.Log4j;

@Log4j
public class GamificationManager {

	public static void main(String[] args) throws Exception {
		String path = "gamification";
		String packages = "com.blemobi." + path + ".rest";

		// 初始化Consul，获取连接Consul服务器的间隔时间
		long consulIntervalTime = Constant.getConsulIntervaltime();

		// 初始化Consul, 确定连接了正确的服务器环境（本地测试，北美测试服务器，或者是生产服务器）
		ConsulManager.startService(args, consulIntervalTime, path); // 启动连接Consul服务

		// 注册监听器，如果Consul服务器获取有配置信息变更，则通知Constant
		ConsulManager.addConsulChangeListener(Constant.getAdapter());

		// 初始化任务内容
		TaskHelper taskHelper = new TaskHelper();
		taskHelper.init();
		BadgeHelper badgeHelper = new BadgeHelper();
		badgeHelper.init();
		LevelHelper levelHelper = new LevelHelper();
		levelHelper.init();

		log.info("Starting Gamification Server ...");

		// 启动Jetty HTTP服务器
		int port = Constant.getJettyServerPort();
		log.info("Gamification Server Running Port:" + port);
		JettyServer jettyServer = new JettyServer();
		jettyServer.startServer(port, packages, path);

		// 发布Consul的健康发现
		int healthPort = Constant.getChatServiceHealthPort();
		HealthManager.startService(healthPort, path);

		/*
		 * 1）从Consul读取信息 2) 开启Jetty HTTP服务器 3) 通过protobuff连接帐户服务器 4) 连接wukong
		 */
		log.info("Start Gamification Server Finish!");
		log.info("Waiting client connect...");
	}

}
