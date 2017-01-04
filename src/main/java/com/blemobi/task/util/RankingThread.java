package com.blemobi.task.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blemobi.library.cache.UserBaseCache;
import com.blemobi.library.redis.RedisManager;
import com.blemobi.sep.probuf.AccountProtos.PUserBase;
import com.blemobi.sep.probuf.DataPublishingProtos.PGuy;
import com.google.common.base.Strings;

import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;

/**
 * 经验值排名
 * 
 * @author zhaoyong
 *
 */
@Log4j
public class RankingThread extends Thread {
	private int rankmax;
	public static List<PGuy> staticGuyList;
	public static Map<String, Integer> staticGuyRankMap;

	private Jedis jedis;

	/**
	 * 构造方法
	 * 
	 * @param uuid
	 */
	public RankingThread(int rankmax) {
		this.rankmax = rankmax;
	}

	public void run() {
		while (true) {
			jedis = RedisManager.getLongRedis();
			try {
				rankingAll();
			} catch (Exception e) {
				log.error("消息订阅处理异常");
				e.printStackTrace();
			} finally {
				RedisManager.returnResource(jedis);
			}

			try {
				Thread.sleep(60 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 排名
	 * 
	 * @throws IOException
	 */
	public void rankingAll() throws IOException {
		// 取出所有用户信息
		PGuy[] guyArray = getAllUserInfo();
		// 根据经验值排序
		Arrays.sort(guyArray, new ExpComparator());
		// 只保留前rankmax名
		PGuy[] rankGuyArray = copyRankmax(guyArray);
		// 获得前rankmax名用户的排名
		Map<String, Integer> guyRankMap = makeRank(rankGuyArray);
		// 缓存起来
		staticGuyList = Arrays.asList(rankGuyArray);
		staticGuyRankMap = guyRankMap;
		log.debug("总排行数据计算完成！" + staticGuyList.size());
	}

	/**
	 * 取出所有用户信息
	 * 
	 * @return
	 * @throws IOException
	 */
	private PGuy[] getAllUserInfo() throws IOException {
		Set<String> set = jedis.keys(Constant.GAME_USER_INFO + "*");
		PGuy[] guyArray = new PGuy[set.size()];
		int i = 0;
		for (String key : set) {
			guyArray[i++] = getPGuy(key);
		}
		return guyArray;
	}

	/**
	 * 获取用户详情
	 * 
	 * @param key
	 * @return
	 * @throws IOException
	 */
	private PGuy getPGuy(String key) throws IOException {
		String memberUuid = key.substring(Constant.GAME_USER_INFO.length());
		int exp = getOtherUserExp(memberUuid);
		PUserBase userBase = UserBaseCache.get(memberUuid);
		return PGuy.newBuilder().setUuid(memberUuid).setNickname(userBase.getNickname())
				.setHeadImgURL(userBase.getHeadImgURL()).setRankValue(exp).build();
	}

	/**
	 * 只保留前rankmax名
	 * 
	 * @param guyArray
	 * @return
	 */
	private PGuy[] copyRankmax(PGuy[] guyArray) {
		PGuy[] rankGuyArray;
		if (guyArray.length > rankmax) {
			rankGuyArray = new PGuy[rankmax];
			System.arraycopy(guyArray, 0, rankGuyArray, 0, rankmax);
		} else {
			rankGuyArray = guyArray;
		}
		return rankGuyArray;
	}

	/**
	 * 名次处理
	 * 
	 * @return
	 * @throws IOException
	 */
	private Map<String, Integer> makeRank(PGuy[] rankGuyArray) throws IOException {
		Map<String, Integer> guyRankMap = new HashMap<String, Integer>();
		int rank = 0;
		for (PGuy guy : rankGuyArray) {
			guyRankMap.put(guy.getUuid(), ++rank);
		}
		return guyRankMap;
	}

	/**
	 * 获得用户的经验值（不存在就初始化用户）
	 * 
	 * @param uuid
	 * @return
	 */
	private int getOtherUserExp(String uuid) {
		String exp = jedis.hget(Constant.GAME_USER_INFO + uuid, "exp");
		if (Strings.isNullOrEmpty(exp)) {
			TaskUtil taskUtil = new TaskUtil(uuid);
			taskUtil.init();
			exp = jedis.hget(Constant.GAME_USER_INFO + uuid, "exp");
		}
		return Integer.parseInt(exp);
	}

}