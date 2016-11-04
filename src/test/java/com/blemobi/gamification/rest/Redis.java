package com.blemobi.gamification.rest;

import com.blemobi.task.util.Constant;

import redis.clients.jedis.Jedis;

public class Redis {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		Jedis jedis = new Jedis();
//		jedis.hsetnx(Constant.GAME_USER_INFO + "a9", "exp", "9");
//		jedis.hsetnx(Constant.GAME_USER_INFO + "a6", "exp", "6");
//		jedis.hsetnx(Constant.GAME_USER_INFO + "a8", "exp", "8");
//		jedis.hsetnx(Constant.GAME_USER_INFO + "a2", "exp", "2");
//		jedis.hsetnx(Constant.GAME_USER_INFO + "a3", "exp", "3");

		 Jedis jedis = new Jedis("192.168.1.245", 6379);
		 for (String key : jedis.keys("task:*")) {
		 System.out.println(key);
		 jedis.del(key);
		 }
	}

}
