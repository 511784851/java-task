package com.blemobi.library.redis;

import com.blemobi.library.consul.BaseService;
import com.google.common.base.Strings;

import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Log4j
public class RedisManager {
	/*
	 * 获得Redis连接
	 */
	public static Jedis getRedis() {
		JedisPool jedisPool = getJedisPool();
		log.debug("Redis Pool Info -> NumActive=[" + jedisPool.getNumActive() + "],NumIdle=[" + jedisPool.getNumIdle()
				+ "]");
		Jedis jedis = jedisPool.getResource();

		String auth = BaseService.getProperty("redis_user_auth");
		if (!Strings.isNullOrEmpty(auth)) {
			jedis.auth(auth);
		}

		AutoReturnRedis.putJedis(jedis);
		return jedis;
	}

	/*
	 * 释放Redis连接
	 */
	@SuppressWarnings("deprecation")
	public static void returnResource(Jedis... jediss) {
		JedisPool jedisPool = getJedisPool();
		for (Jedis jedis : jediss) {
			jedisPool.returnResource(jedis);
			AutoReturnRedis.remJedis(jedis);
		}
	}

	private static JedisPool getJedisPool() {
		RedisPoolSingleton redisPool = RedisPoolSingleton.getInstance();
		JedisPool jedisPool = redisPool.getJedisPool();
		return jedisPool;
	}
}