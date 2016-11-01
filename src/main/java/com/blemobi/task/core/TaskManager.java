package com.blemobi.task.core;

import java.util.List;

import com.blemobi.library.consul.BaseService;
import com.blemobi.library.consul.ConsulManager;
import com.blemobi.library.global.Constant;
import com.blemobi.library.health.HealthManager;
import com.blemobi.library.jetty.JettyServer;
import com.blemobi.library.jetty.ServerFilter;
import com.blemobi.library.log.LoggerManager;
import com.blemobi.task.basic.TaskHelper;

import lombok.extern.log4j.Log4j;

@Log4j
public class TaskManager {

	public static void main(String[] args) throws Exception {
		final String selfName = "task";
		final String packages = "com.blemobi." + selfName + ".rest";

		// 初始化Consul
		long consulIntervalTime = Constant.getConsulIntervaltime();// 获取连接Consul服务器的间隔时间
		ConsulManager.startService(selfName, args, consulIntervalTime); // 启动连接Consul服务

		TaskHelper.init();

		log.info("Starting Task Server ...");

		// 启动Jetty HTTP服务器
		String jetty_port = BaseService.getProperty("jetty_port");
		int port = Integer.parseInt(jetty_port);
		FilterProperty filterProperty = new FilterProperty();
		List<ServerFilter> serverFilterList = filterProperty.getFilterList();

		log.info("Task Server Running Port:" + jetty_port);
		JettyServer jettyServer = new JettyServer(selfName, packages, port, serverFilterList);
		jettyServer.start();

		// 发布Consul的健康发现
		String health_check_port = BaseService.getProperty("health_check_port");
		int check_port = Integer.parseInt(health_check_port);
		HealthManager.startService(check_port, selfName);

		// 初始化Consul日志管理
		LoggerManager.startService();

		log.info("Start Task Server Finish!");
		log.info("Waiting client connect...");
	}
}