package com.blemobi.task.core;

import java.io.IOException;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import com.blemobi.library.cache.CacheInvalid;
import com.blemobi.library.cache.LiveThread;
import com.blemobi.library.consul.BaseService;
import com.blemobi.library.consul.ConsulManager;
import com.blemobi.library.health.HealthManager;
import com.blemobi.library.jetty.JettyServer;
import com.blemobi.library.jetty.ServerFilter;
import com.blemobi.library.log.LoggerManager;
import com.blemobi.task.basic.BasicData;
import com.blemobi.task.util.RankingThread;
import com.google.common.base.Strings;

import lombok.extern.log4j.Log4j;

/**
 * 服务启动入口
 * 
 * @author zhaoyong
 *
 */
@Log4j
public class TaskManager {
	/**
	 * 服务名称
	 */
	private static final String selfName = "task";
	/**
	 * 发布的Rest接口路径
	 */
	private static final String packages = "com.blemobi." + selfName + ".rest";
	/**
	 * 连接Consul服务器的间隔时间
	 */
	private static final long consulIntervalTime = 1000 * 30;
	/**
	 * 用户缓存失效时间（单位：毫秒）
	 */
	private static final long live_time = 1 * 24 * 60 * 60 * 1000;
	/**
	 * 最大排名
	 */
	private static final int RANK_MAX = 200;

	public static void main(String[] args) throws Exception {
		// 初始化Consul
		ConsulManager.startService(selfName, args, consulIntervalTime); // 启动连接Consul服务
		// 发布Consul的健康发现
		startHealth();
		// 启动Jetty HTTP服务器
		startJetty();
		// 启动线程，计算排行
		RankingThread rankingThread = new RankingThread(RANK_MAX);
		rankingThread.start();
		// 启动线程，管理用户缓存
		LiveThread LiveThread = new LiveThread(live_time, new CacheInvalid());
		LiveThread.start();
		// 启动Consul日志管理
		LoggerManager.startService();
		log.info("Start Task Server Finish!");
	}

	/**
	 * 发布Consul的健康发现
	 */
	private static void startHealth() {
		String health_check_port = BaseService.getProperty("health_check_port");
		int check_port = Integer.parseInt(health_check_port);
		HealthManager.startService(check_port, selfName);
	}

	/**
	 * 读取配置文件中的数据
	 */
	private static void loadData()
			throws IOException, InterruptedException, EncryptedDocumentException, InvalidFormatException {
		String task_config_url = "";
		do {
			Thread.sleep(5000);
			task_config_url = BasicData.getTaskConfig();
		} while (Strings.isNullOrEmpty(task_config_url));
		log.info("任务配置文件url: " + task_config_url);
		BasicData basicData = new BasicData(task_config_url);
		basicData.init();
	}

	/**
	 * 启动Jetty HTTP服务器
	 */
	private static void startJetty() throws Exception {
		String jetty_port = BaseService.getProperty("jetty_port");
		int port = Integer.parseInt(jetty_port);

		FilterProperty filterProperty = new FilterProperty();
		List<ServerFilter> serverFilterList = filterProperty.getFilterList();

		JettyServer jettyServer = new JettyServer(selfName, packages, port, serverFilterList);
		// 读取任务配置数据
		loadData();
		jettyServer.start();
	}
}