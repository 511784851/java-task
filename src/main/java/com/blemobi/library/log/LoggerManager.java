package com.blemobi.library.log;
/**
 * 
 * @author 李子才<davis.lee@blemobi.com>
 * 这是java的System.out输出流和System.err输出流重定向到Log4j的工具类。
 */

import java.util.Enumeration;
import java.util.Map;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import com.blemobi.library.consul.ConsulChangeListener;
import com.blemobi.library.consul.ConsulManager;
import com.blemobi.library.consul.SocketInfo;

public class LoggerManager {
	private static String logLevel = null;
	
	public static void startService(){
		StdOutErrRedirect.redirectSystemOutAndErrToLog();
		ConsulManager.addConsulChangeListener(adapter);
		checkAndCloseConsole();
	}
	// 创建Consul服务器的适配器对象，该对象能接受从consul服务器传递过来的配置信息变更通知。
	private static ConsulChangeListener adapter = new ConsulChangeListener() {
		public void onEnvChange(Map<String, String> prop) {
			String level = prop.get("log_level");
			setLogLevel(level);
		}

		public void onServiceChange(String serviceName, SocketInfo[] socketInfo) {
			
		}
	};
	
	private static void checkAndCloseConsole() {
		String env = System.getProperty("EnvMode", "local");
		if(!env.equalsIgnoreCase("local")){
			try {
				Enumeration en=LogManager.getRootLogger().getAllAppenders();
				while(en.hasMoreElements()){
					Object obj = en.nextElement();
					if(obj instanceof ConsoleAppender){
						LogManager.getRootLogger().removeAppender((Appender) obj);
					}
				}
			} catch (Exception e) {
				
			}
		}
	}
	
	private static void setLogLevel(String level) {
		if(level==null) level = "";
		if(!level.equals(logLevel)){
			logLevel = level;
			if(level.equalsIgnoreCase("ERROR")){
				LogManager.getRootLogger().setLevel(Level.ERROR);  
			}else if(level.equalsIgnoreCase("WARN")){
				LogManager.getRootLogger().setLevel(Level.WARN);  
			}else if(level.equalsIgnoreCase("INFO")){
				LogManager.getRootLogger().setLevel(Level.INFO);  
			}else{
				LogManager.getRootLogger().setLevel(Level.DEBUG);  
			}
		}
	}
}
