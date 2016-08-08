package com.blemobi.gamification.jetty;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;

import com.blemobi.library.filter.ListParamFilter;

import lombok.extern.log4j.Log4j;

@Log4j
public class JettyServer {

	private Server jettyServer = null;

	/**
	 * 启动Jetty服务器，对外提供Web服务
	 *
	 * @param port
	 *            服务器对外的端口
	 * @throws Exception
	 */
	public void startServer(final int port, final String packages, String path) {
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/" + path);

		EnumSet<DispatcherType> enumSet = EnumSet.allOf(DispatcherType.class);
		enumSet.add(DispatcherType.REQUEST);

		// 添加需要过滤的PATH
		this.addFilter(context, enumSet);

		this.jettyServer = new Server(port);
		jettyServer.setHandler(context);

		ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
		jerseyServlet.setInitOrder(0);
		jerseyServlet.setInitParameter(ServerProperties.PROVIDER_PACKAGES, packages);

		try {
			jettyServer.start();
		} catch (Exception e) {
			log.info("Game server catch an exception, Port[" + port + "] is used!");
			log.info("System exit!");
			log.info("Good bye!");
			System.exit(0);
		}
	}

	/**
	 * 添加需要校验uuid和token合法性的path
	 *
	 * @param ServletContextHandler
	 */
	private void addFilter(ServletContextHandler context, EnumSet<DispatcherType> enumSet) {
		context.addFilter(ListParamFilter.class, "/*", enumSet);

	}

	public void stopServer() {
		if (jettyServer == null) {
			return;
		}

		try {
			jettyServer.stop();
		} catch (Exception e) {

		} finally {
			jettyServer.destroy();
		}
	}

}
