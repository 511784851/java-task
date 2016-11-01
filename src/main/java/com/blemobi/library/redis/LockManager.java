package com.blemobi.library.redis;

import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;

/*
 * Redis锁的实现，解决分布式多环境对Redis共享数据的处理
 */
@Log4j
public class LockManager {

	// 间隔时间
	private static final long sleepTime = 200;
	// 延迟时间
	private static final long delayTime = 1000;

	/*
	 * 获得锁
	 * 
	 * @param lock 锁的key
	 * 
	 * @param expire 锁的失效时间，秒
	 * 
	 */
	public static boolean getLock(String lock, int expire) {
		long nx = 0;
		long end = System.currentTimeMillis() + (expire * 1000) + delayTime;
		while (nx != 1) {
			Jedis jedis = RedisManager.getRedis();
			nx = jedis.setnx(lock, expire + "");

			if (nx == 1) {// 获得锁成功
				jedis.expire(lock, expire);
				RedisManager.returnResource(jedis);
			} else {
				RedisManager.returnResource(jedis);

				long now = System.currentTimeMillis();
				if (now > end) {
					break;
				}
				try {
					Thread.sleep(sleepTime);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (nx != 1) {
			log.error("获取Redis锁失败");
		}
		return nx == 1;
	}

	/*
	 * 释放锁
	 */
	public static boolean releaseLock(String lock) {
		Jedis jedis = RedisManager.getRedis();
		jedis.del(lock);
		RedisManager.returnResource(jedis);
		return true;
	}

}