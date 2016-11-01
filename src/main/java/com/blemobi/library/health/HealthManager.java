package com.blemobi.library.health;

import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import lombok.extern.log4j.Log4j;

@Log4j
public class HealthManager {
	/**
	 * 启动对外的健康服务监听。
	 * 
	 * @param healthPort
	 *            健康服务的端口。
	 */
	public static void startService(int healthPort, String serviceName) {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(healthPort), 0);
			server.createContext("/", new HealthHandler(serviceName));
			server.setExecutor(null); // creates a default executor
			server.start();
			log.info("Health Report Server Running Port:" + healthPort);
		} catch (Exception e) {
			log.info("Health Report Server catch an exception, Port[" + healthPort + "] is used!");
			log.info("System exit!");
			log.info("Good bye!");

			System.exit(0);
		}
	}
}
