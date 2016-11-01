package com.blemobi.library.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;

/*
 * 自动回收程序中未正常回收的Redis连接对象
 */
@Log4j
public class AutoReturnRedis {
	// 存放活跃的连接对象
	private static Map<Integer, RedisExpire> map = new HashMap<Integer, RedisExpire>();
	// 连接对象的最大使用时间:3分钟
	private static final long expire = 3 * 60 * 1000;

	/*
	 * 存放连接对象，并设置失效时间
	 */
	public static boolean putJedis(Jedis jedis) {
		long now = System.currentTimeMillis();
		long end = now + expire;
		map.put(jedis.hashCode(), new RedisExpire(jedis, end));
		return true;
	}

	/*
	 * 清楚正常回收的连接对象
	 */
	public static boolean remJedis(Jedis jedis) {
		map.remove(jedis.hashCode());
		return true;
	}

	/*
	 * 启动线程，自动回收程序中未正常回收的Redis连接对象
	 */
	static {
		new Thread() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(1 * 60 * 1000);
						long now = System.currentTimeMillis();
						List<Integer> keys = new ArrayList<Integer>();
						for (Integer hc : map.keySet()) {
							keys.add(hc);
						}
						log.info("系统活跃的Redis连接对象数量：" + keys.size());
						for (Integer hc : keys) {
							RedisExpire redisExpire = map.get(hc);
							if (redisExpire != null && now >= redisExpire.getEndTime()) {
								RedisManager.returnResource(redisExpire.getJedis());
								log.warn("系统自动回收了一个Redis Pool连接对象");
							}
						}
					} catch (Exception e) {
						log.error("系统自动回收Reids Pool连接对象异常");
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
}

class RedisExpire {
	private Jedis jedis;
	private long endTime;

	public RedisExpire(Jedis jedis, long endTime) {
		this.jedis = jedis;
		this.endTime = endTime;
	}

	public Jedis getJedis() {
		return jedis;
	}

	public long getEndTime() {
		return endTime;
	}
}