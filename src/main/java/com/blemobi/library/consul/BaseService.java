package com.blemobi.library.consul;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import lombok.extern.log4j.Log4j;

@Log4j
public class BaseService {
	private static  Map<String, ServiceInfo> sis = new HashMap<String, ServiceInfo>();
	private static  Map<String, String> env = new HashMap<String, String>();
	private static  String logLevel = null;
	
	/**
	 * 从某个服务的consul服务器列表。
	 * @param serviceName 某个服务的名称。
	 * @return 返回某个服务的的全部连接信息。
	 */
	public static SocketInfo[] getRegisterServer(String serviceName) {
		synchronized(sis){
			ServiceInfo si = sis.get(serviceName);
			if(si==null){
				log.warn("The service["+serviceName+"] can not find.");
				return null;
			}else{
				SocketInfo[] all = si.getAllSocketInfo();
				return all;
			}
		}
	}
	
	/**
	 * 从某个服务的consul服务器列表中，获取可用列表中随机选一个，如果没可用的，则返回null。
	 * @param serviceName 某个服务的名称。
	 * @return 返回某个服务的的可用连接信息。
	 */
	public static SocketInfo getActiveServer(String serviceName) {
		
		synchronized(sis){
			ServiceInfo si = sis.get(serviceName);
			if(si==null){
				log.warn("The service["+serviceName+"] can not find.");
				return null;
			}else{
				SocketInfo[] all = si.getAllSocketInfo();
				List<String> error = si.getErrorList();
				SocketInfo[] healthAccount = getOnlineServer(all,error);
				if(healthAccount.length==0){
					log.warn("The service["+serviceName+"] health node not exist.");
					return null;
				}else{
					int ramdom = (int)(Math.random() * healthAccount.length);
					return healthAccount[ramdom];
				}
				
			}
		}
	}
	
	/**
	 * 从某个服务的consul服务器列表中，获取健康的服务器列表。
	 * @param all 某个服务的所有服务器，也就是从consul服务器中获取到该服务的所有服务器。
	 * @param error 某个服务的所有服务器中，有异常的服务器类别。
	 * @return 返回某个服务的健康的服务器的列表，也就是“all集合”中排除掉“error集合”后 ，剩余的结果。
	 */
	private static SocketInfo[] getOnlineServer(SocketInfo[] all, List<String> error) {
		int count = all.length;
		SocketInfo[] buff = new SocketInfo[count];
		int index = 0;
		
		String[] errorList = new String[error.size()];
		error.toArray(errorList);
		
		for(int i=0;i<count;i++){
			String v = all[i].getIpAddr() + "-" + all[i].getPort();
			if(!isExist(errorList,v)){
				buff[index] = new SocketInfo(all[i].getIpAddr(),all[i].getPort());
				index++;
			}
		}

		SocketInfo[] rtn = new SocketInfo[index];
		System.arraycopy(buff, 0, rtn, 0, rtn.length);
		
		return rtn;
	}
	
	/**
	 * 判断集合里是否存在某个元素。
	 * @param errorList 大集合。
	 * @param v 待查找存在的字符串对象。
	 * @return 如果存在，则返回true。
	 */
	private static boolean isExist(String[] errorList, String v) {
		for(int i=0;i<errorList.length;i++){
			if(errorList[i].equals(v)){
				return true;
			}
		}
		return false;
	}



	/**
	 * 报告有异常的账户服务器，该服务器将记录在异常服务器列表中，当用户获取账户服务器时，则过滤掉该服务器。
	 * 记录服务器的信息为(ip+port)的方式来代表一台服务器。
	 * @param addr 有异常的服务器名称。
	 * @param addr 有异常的服务器的IP地址。
	 * @param port 有异常的服务器的Port端口。
	 */
	public static void reportErrorServer(String serviceName, String addr, int port) {
		synchronized(sis){
			ServiceInfo si = sis.get(serviceName);
			if(si!=null){
				si.reportErrorServer(addr, port);
			}
		}
	}

	/**
	 * 获取consul中K-V键值信息。
	 * @param key 键的名称。
	 * @return 对应键的值。
	 */
	public static String getProperty(String key) {
		return env.get(key);
	}

	// 创建Consul服务器的适配器对象，该对象能接受从consul服务器传递过来的配置信息变更通知。
	public static ConsulChangeListener adapter = new ConsulChangeListener() {

		public void onEnvChange(Map<String, String> prop) {
			//log.debug("----------------onEnvChange-----------");
			synchronized(env){
				env.clear();
				for(Entry<String, String> kv:prop.entrySet()){
					//log.debug("consul "+kv.getKey()+" = ["+kv.getValue()+"]");
				}
				
				for(Entry<String, String> kv:prop.entrySet()){
					env.put(kv.getKey(), kv.getValue());
				}
			}
		}

		public void onServiceChange(String serviceName, SocketInfo[] socketInfo) {
			synchronized(sis){
				//log.info("The service["+serviceName+"], count="+socketInfo.length);
				for(SocketInfo si:socketInfo){
					//log.info("The service["+serviceName+"], SocketInfo addr=["+si.getIpAddr()+"], port=["+si.getPort()+"]");
				}
				
				ServiceInfo si = sis.get(serviceName);
				if(si==null){
					si = new ServiceInfo();
					sis.put(serviceName, si);
				}
				si.setAllSocketInfo(socketInfo);
				si.clearErrorServer();
			}
			
		}

	};
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
	static{
		ConsulManager.addConsulChangeListener(adapter);
	}
	
}
