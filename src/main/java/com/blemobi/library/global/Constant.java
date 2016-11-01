package com.blemobi.library.global;

public class Constant {

	// Consul定时任务间隔时间，单位：毫秒
	private static final long ConsulIntervalTime = 1000 * 30;

	/**
	 * 获取访问consul服务器的间隔时间。
	 * 
	 * @return 间隔时间，单位是毫秒的值。
	 */
	public static long getConsulIntervaltime() {
		return ConsulIntervalTime;
	}
}