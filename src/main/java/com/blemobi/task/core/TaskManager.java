package com.blemobi.task.core;

import com.blemobi.library.cache.CacheListener;
import com.blemobi.library.consul_v1.Constants;
import com.blemobi.library.consul_v1.Constants.CONFIG_KV_KEY;
import com.blemobi.library.consul_v1.ConsulClientMgr;
import com.blemobi.library.consul_v1.ConsulServiceMgr;
import com.blemobi.library.consul_v1.PropsUtils;
import com.blemobi.library.grpc_v1.GRPCServer;
import com.blemobi.library.grpc_v1.auth.AuthProvider;
import com.blemobi.library.grpc_v1.auth.ConsulAuthProvider;
import com.blemobi.library.grpc_v1.interceptor.AuthServerInterceptor;
import com.blemobi.library.jetty.JettyServer;
import com.blemobi.library.jetty.ServerFilter;
import com.blemobi.task.bat.OnOffJob;
import com.blemobi.task.bat.QuartzManager;
import com.blemobi.task.bat.ResetStockJob;
import com.blemobi.task.grpcservice.TaskGRPCImpl;
import com.blemobi.tools.DateUtils;
import io.grpc.ServerInterceptor;
import lombok.extern.log4j.Log4j;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 服务启动入口
 * 
 * @author zhaoyong
 *
 */
@Log4j
public class TaskManager {
	private static String ADDRESS = "127.0.0.1";// 测试、生产环境
	/**
	 * 服务名称
	 */
	private static final String selfName = "task";
	/**
	 * 要发布rest服务的
	 */
	private static final String packages = "com.blemobi." + selfName + ".rest";
	/**
	 * 失效时间（单位：毫秒）
	 */
	private static final long invalid_time = 3 * 24 * 60 * 60 * 1000;
	/**
	 * 扫描间隔时间（单位：毫秒）
	 */
	private static final long sleep_time = 1 * 24 * 60 * 60 * 1000;

	public static void main(String[] args) throws Exception {
		Constants.SERVER_NM = selfName;
		ConsulClientMgr.initial(args, selfName, ADDRESS);
		log.info("consul client initialed");
		Integer jettyPort = PropsUtils.getInteger(CONFIG_KV_KEY.JETTY_PORT);
		log.info("jetty port:" + jettyPort);
		if (!ConsulClientMgr.getENV_TYPE().equalsIgnoreCase("local")) {
			Integer healthPort = PropsUtils.getInteger(CONFIG_KV_KEY.HEALTH_CHECK_PORT);
			log.info("health check port:" + healthPort);
			ConsulServiceMgr.registerServiceWithHealthChk(jettyPort, selfName, healthPort, selfName, null);
		}
		QuartzManager.addJob("resetStockJob", "resetStockTrigger", ResetStockJob.class, new Date(DateUtils
				.getDayStart()), 'H', 24);
		QuartzManager.addJob("OnOffJob", "OnOffTrigger", OnOffJob.class, new Date(DateUtils
				.getDayStart()), 'M', 1);
		startJetty(jettyPort);
		startGRPC();
		log.info("Start Task Server Finish!");

		// 启动线程管理用户缓存
		new CacheListener(invalid_time, sleep_time, uuid -> log.debug("用户缓存信息过期了！ uuid=" + uuid)).run();
	}

	/**
	 * 启动Jetty HTTP服务器
	 * 
	 * @throws Exception
	 */
	private static void startJetty(Integer port) throws Exception {
		// 过滤器配置
		FilterProperty filterProperty = new FilterProperty();
		List<ServerFilter> serverFilterList = filterProperty.getFilterList();
		// 启动Jetty服务
		JettyServer jettyServer = new JettyServer(selfName, packages, port, serverFilterList);
		jettyServer.start();
	}

	/**
	 * 启动GRPC服务
	 * 
	 * @throws Exception
	 */
	private static void startGRPC() throws Exception {
		AuthProvider authProvider = new ConsulAuthProvider();
		ServerInterceptor authInterceptor = new AuthServerInterceptor(authProvider);
		Set<Class<?>> anno = new HashSet<Class<?>>();
		anno.add(TaskGRPCImpl.class);
		GRPCServer.start(selfName, anno, authInterceptor);
	}
}