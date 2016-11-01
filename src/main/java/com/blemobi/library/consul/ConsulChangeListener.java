package com.blemobi.library.consul;
/**
 * 
 * @author 李子才<davis.lee@blemobi.com>
 * 这是Consul服务器取信息的适配器接口类。
 */

import java.util.Map;
public interface ConsulChangeListener {
	
	/**
	 * 当consul服务器中的key-value值有变化是，触发函数的动作
	 * @param prop 是key-value格式的集合
	 */
	public void onEnvChange(Map<String,String> prop);
	
	/**
	 * 当consul服务器中的某个名称服务的所有服务器的列表
	 * @param serviceName 是服务的名称
	 * @param serverInfo  是serviceName服务的对应所有服务器，是一个二维数组格式。serverInfo[i][0]是IP地址，serverInfo[i][1]是端口值。
	 */
	public void onServiceChange(String serviceName, SocketInfo[] socketInfo);
}
