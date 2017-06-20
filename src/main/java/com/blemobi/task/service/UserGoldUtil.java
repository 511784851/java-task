package com.blemobi.task.service;

import com.blemobi.library.redis.dao.RedisDao;
import com.blemobi.task.basic.GoldInfo;
import com.blemobi.task.util.Global;
import com.google.common.base.Strings;

import lombok.extern.log4j.Log4j;

/**
 * 金币流水处理
 * 
 * @author zhaoyong
 *
 */
@Log4j
public class UserGoldUtil {
	private String uuid;
	private String ID;
	private byte type;
	private short gold;
	private String desc;

	/**
	 * 构造方法
	 * 
	 * @param uuid
	 *            用户uuid
	 * @param ID
	 *            ID或编号
	 * @param type
	 *            类型（1-新手任务，2-每日任务，3-商城消费，4-监管操作）
	 * @param gold
	 *            金币（大于0收入，小于0支出）
	 * @param desc
	 *            描述
	 */
	public UserGoldUtil(String uuid, String ID, byte type, short gold, String desc) {
		this.uuid = uuid;
		this.ID = ID;
		this.type = type;
		this.gold = gold;
		this.desc = desc;
	}

	/**
	 * 金币流水（线程安全）
	 * 
	 * @return
	 */
	public boolean incrAndDetail() {
		// 对单个用户使用多环境锁机制
		return new RedisDao<Boolean>().execLock(Global.LOCK_KEY + uuid, () -> {
			if (!check())
				return false;// 金币不足，无法消费
			String content = new GoldInfo(ID, type, gold, desc).toString();
			new RedisDao<>().exectx(r -> {
				r.incrBy(Global.GOLD_KEY + uuid, gold);// 更新金币数量
				r.rpush(Global.DETAIL_KEY + uuid, content);// 增加金币流水
			});
			log.debug("用户[" + uuid + "]金币变化：" + gold);
			return true;
		});
	}

	/**
	 * 如果是消费，验证金币是否足够
	 * 
	 * @return
	 */
	private boolean check() {
		if (gold > 0)
			return true;
		int myGold = UserGoldUtil.getUserGold(Global.GOLD_KEY + uuid);
		return gold + myGold > 0 ? true : false;
	}

	/**
	 * 获得用户金币
	 * 
	 * @param k
	 * @return
	 */
	public static int getUserGold(String k) {
		String gold = new RedisDao<String>().exec(r -> r.get(k));
		return Strings.isNullOrEmpty(gold) ? 0 : Integer.parseInt(gold);
	}

	/**
	 * 获得用户任务已完成次数
	 * 
	 * @param k
	 * @param ID
	 * @return
	 */
	public static byte getTaskCount(String k, short ID) {
		String count = new RedisDao<String>().exec(r -> r.hget(k, ID + ""));
		return Strings.isNullOrEmpty(count) ? 0 : Byte.parseByte(count);
	}

}