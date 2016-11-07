package com.blemobi.task.core;

import java.util.List;

import com.blemobi.library.consul.BaseService;
import com.blemobi.library.consul.ConsulManager;
import com.blemobi.library.global.Constant;
import com.blemobi.library.health.HealthManager;
import com.blemobi.library.jetty.JettyServer;
import com.blemobi.library.jetty.ServerFilter;
import com.blemobi.library.log.LoggerManager;
import com.blemobi.task.basic.BasicData;
import com.google.common.base.Strings;

import lombok.extern.log4j.Log4j;

@Log4j
public class TaskManager {

	public static void main(String[] args) throws Exception {
		final String selfName = "task";
		final String packages = "com.blemobi." + selfName + ".rest";

		// 初始化Consul
		long consulIntervalTime = Constant.getConsulIntervaltime();// 获取连接Consul服务器的间隔时间
		ConsulManager.startService(selfName, args, consulIntervalTime); // 启动连接Consul服务

		// 发布Consul的健康发现
		String health_check_port = BaseService.getProperty("health_check_port");
		int check_port = Integer.parseInt(health_check_port);
		HealthManager.startService(check_port, selfName);

		// 读取配置文件中的数据
		String task_config_url = BasicData.getTaskConfig();// BaseService.getProperty("task_config_url");
		log.debug("task_config_url: " + task_config_url);
		while (Strings.isNullOrEmpty(task_config_url)) {
			Thread.sleep(1000);
			task_config_url = BasicData.getTaskConfig();
			log.debug("task_config_url: " + task_config_url);
		}

		BasicData basicData = new BasicData(task_config_url);
		basicData.init();

		log.info("Starting Task Server ...");

		// 启动Jetty HTTP服务器
		String jetty_port = BaseService.getProperty("jetty_port");
		int port = Integer.parseInt(jetty_port);
		FilterProperty filterProperty = new FilterProperty();
		List<ServerFilter> serverFilterList = filterProperty.getFilterList();

		log.info("Task Server Running Port:" + jetty_port);
		JettyServer jettyServer = new JettyServer(selfName, packages, port, serverFilterList);
		jettyServer.start();

		// 初始化Consul日志管理
		LoggerManager.startService();

		log.info("Start Task Server Finish!");
		log.info("Waiting client connect...");
	}
}