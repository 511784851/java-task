package com.blemobi.task.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;

import com.blemobi.library.cache.UserBaseCache;
import com.blemobi.library.client.NewsHttpClient;
import com.blemobi.library.client.SocialHttpClient;
import com.blemobi.library.redis.RedisManager;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.AccountProtos.PUser;
import com.blemobi.sep.probuf.AccountProtos.PUserBase;
import com.blemobi.sep.probuf.DataPublishingProtos.PGuy;
import com.blemobi.sep.probuf.DataPublishingProtos.PRank;
import com.blemobi.sep.probuf.NewsProtos.PRecommendUser;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.google.common.base.Strings;

import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;

/*
 * 经验值排名
 */
@Log4j
public class RankingUtil {
	private final int rankmax = 200;
	private static long staticCacheTime = 0;
	private static List<PGuy> staticGuyList;
	private static Map<String, Integer> staticGuyRankMap;

	private String uuid;
	private Jedis jedis;

	/*
	 * 构造方法
	 */
	public RankingUtil(String uuid) {
		this.uuid = uuid;
		this.jedis = RedisManager.getRedis();
	}

	/*
	 * 总排名
	 */
	public PMessage rankingAll() throws ClientProtocolException, IOException {
		if (System.currentTimeMillis() - staticCacheTime > 1 * 60 * 1000) {// 重新排行
			// 取出所有用户信息
			Set<String> set = jedis.keys(Constant.GAME_USER_INFO + "*");
			PGuy[] guyArray = new PGuy[set.size()];
			int i = 0;
			for (String key : set) {
				String memberUuid = key.substring(Constant.GAME_USER_INFO.length());
				int exp = getOtherUserExp(memberUuid);
				PUserBase userBase = UserBaseCache.get(memberUuid);
				PGuy guy = PGuy.newBuilder().setUuid(memberUuid).setNickname(userBase.getNickname())
						.setHeadImgURL(userBase.getHeadImgURL()).setRankValue(exp).build();
				guyArray[i++] = guy;
			}
			RedisManager.returnResource(jedis);
			// 根据经验值排序
			Arrays.sort(guyArray, new PGuyComparator());
			// 只保留前rankmax名
			PGuy[] rankGuyArray;
			if (guyArray.length > rankmax) {
				rankGuyArray = new PGuy[rankmax];
				System.arraycopy(guyArray, 0, rankGuyArray, 0, rankmax);
			} else {
				rankGuyArray = guyArray;
			}
			// 获得前rankmax名用户的排名
			Map<String, Integer> guyRankMap = new HashMap<String, Integer>();
			int rank = 0;
			for (PGuy guy : rankGuyArray) {
				guyRankMap.put(guy.getUuid(), ++rank);
			}

			staticGuyList = Arrays.asList(rankGuyArray);
			staticGuyRankMap = guyRankMap;
			staticCacheTime = System.currentTimeMillis();
			log.debug("总排行数据计算完成！" + staticGuyList.size());
		} else {
			RedisManager.returnResource(jedis);
		}

		int exp = getOtherUserExp(uuid);
		Integer rankObject = staticGuyRankMap.get(uuid);
		int rank = rankObject != null ? rankObject : -1;

		PRank prank = PRank.newBuilder().setRankValue(exp).setRank(rank).addAllGuys(staticGuyList).build();
		return ReslutUtil.createReslutMessage(prank);
	}

	/*
	 * 好友排名
	 */
	public PMessage rankFirend() throws ClientProtocolException, IOException {
		List<PGuy> guyList = new ArrayList<PGuy>();
		// 好友信息
		SocialHttpClient httpClient = new SocialHttpClient();
		List<PUser> firendList = httpClient.getAllFriendList(uuid);
		for (PUser user : firendList) {
			if (UserRelation.levelList.contains(user.getLevelInfo().getLevelType())) {// 排除VO用户
				String otherUuid = user.getUuid();
				int otherExp = getOtherUserExp(otherUuid);
				PGuy guy = PGuy.newBuilder().setUuid(otherUuid).setNickname(user.getNickname())
						.setHeadImgURL(user.getHeadImgURL()).setRankValue(otherExp).build();

				guyList.add(guy);
			}
		}
		RedisManager.returnResource(jedis);
		log.debug("好友数量！" + guyList.size());
		return rankFollowOrFirend(guyList);
	}

	/*
	 * 关注排名
	 */
	public PMessage rankFollow() throws ClientProtocolException, IOException {
		List<PGuy> guyList = new ArrayList<PGuy>();
		// 关注用户列表
		NewsHttpClient httpClient = new NewsHttpClient();
		List<PRecommendUser> firendList = httpClient.getAllFollowList(uuid);
		for (PRecommendUser user : firendList) {
			if (UserRelation.levelList.contains(user.getLeveltype())) {// 排除VO用户
				String otherUuid = user.getUuid();
				int otherExp = getOtherUserExp(otherUuid);

				PGuy guy = PGuy.newBuilder().setUuid(otherUuid).setNickname(user.getNickname())
						.setHeadImgURL(user.getHeadImgURL()).setRankValue(otherExp).build();

				guyList.add(guy);
			}
		}
		RedisManager.returnResource(jedis);
		log.debug("关注数量！" + firendList.size());
		return rankFollowOrFirend(guyList);
	}

	/*
	 * 排序处理
	 */
	private PMessage rankFollowOrFirend(List<PGuy> guyList) throws IOException {
		// 用户自己
		int exp = getOtherUserExp(uuid);
		PUserBase userBase = UserBaseCache.get(uuid);
		PGuy myGuy = PGuy.newBuilder().setUuid(uuid).setNickname(userBase.getNickname())
				.setHeadImgURL(userBase.getHeadImgURL()).setRankValue(exp).build();
		guyList.add(myGuy);
		// 根据经验值排序
		Collections.sort(guyList, new PGuyComparator());
		// 获取自己排名
		int rank = 1;
		for (PGuy guy : guyList) {
			if (uuid.equals(guy.getUuid())) {// 自己
				break;
			}
			++rank;
		}
		PRank prank = PRank.newBuilder().setRankValue(exp).setRank(rank).addAllGuys(guyList).build();
		return ReslutUtil.createReslutMessage(prank);
	}

	/*
	 * 获得用户的经验值（不存在就初始化用户）
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

/*
 * 排序实现类
 */
class PGuyComparator implements Comparator<PGuy> {
	public int compare(PGuy o1, PGuy o2) {
		return o2.getRankValue() - o1.getRankValue();
	}
};