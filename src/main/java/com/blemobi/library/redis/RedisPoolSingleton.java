package com.blemobi.library.redis;

import com.blemobi.library.consul.BaseService;

import lombok.extern.log4j.Log4j;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/*
 * Redis连接池单列类
 */
@Log4j
public class RedisPoolSingleton {

	private final int maxIdle = 3;
	private final int maxWaitMillis = 30 * 1000;
	private JedisPool jedisPool;

	/*
	 * 私有构造方法
	 */
	private RedisPoolSingleton() {
		log.info("初始化Redis连接池...");

		int maxTotal = Integer.parseInt(BaseService.getProperty("redis_max_connect_num"));
		String[] redisInfo = BaseService.getProperty("redis_user_addr").split(":");
		String address = redisInfo[0];
		int port = Integer.parseInt(redisInfo[1]);

		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(maxTotal);
		config.setMaxIdle(maxIdle);
		config.setMaxWaitMillis(maxWaitMillis);
		this.jedisPool = new JedisPool(config, address, port);
		log.info("完成Redis连接池的初始化工作.");
	}

	/*
	 * 使用内部类来维护单例
	 */
	private static class SingletonFactory {
		private static final RedisPoolSingleton instance = new RedisPoolSingleton();
	}

	/*
	 * 获得当前对象
	 */
	public static RedisPoolSingleton getInstance() {
		return SingletonFactory.instance;
	}

	/*
	 * 获得Redis连接池
	 */
	public JedisPool getJedisPool() {
		return jedisPool;
	}

}