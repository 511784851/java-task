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

import com.blemobi.library.redis.RedisManager;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.AccountProtos.PLevelInfo;
import com.blemobi.sep.probuf.AccountProtos.PUser;
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
	private String userInfoKey;
	private Jedis jedis;
	private Map<String, String> userInfoMap;

	/*
	 * 构造方法
	 */
	public RankingUtil(String uuid) {
		this.uuid = uuid;
		this.userInfoKey = Constant.GAME_USER_INFO + uuid;
		this.jedis = RedisManager.getRedis();
		this.userInfoMap = jedis.hgetAll(userInfoKey);
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
				Map<String, String> map = jedis.hgetAll(key);
				PGuy guy = PGuy.newBuilder().setUuid(key.substring(Constant.GAME_USER_INFO.length()))
						.setNickname(map.get("nickname")).setHeadImgURL(map.get("headimg"))
						.setRankValue(Integer.parseInt(map.get("exp"))).build();
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

		int exp = Integer.parseInt(userInfoMap.get("exp"));
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
		List<PUser> firendList = UserRelation.getFirendList(uuid);
		for (PUser user : firendList) {
			if (UserRelation.levelList.contains(user.getLevelInfo().getLevelType())) {// 排除VO用户
				String otherUuid = user.getUuid();
				int otherExp = getOtherUserExp(otherUuid, user);

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
		List<PRecommendUser> firendList = UserRelation.getFollowList(uuid);
		for (PRecommendUser user : firendList) {
			if (UserRelation.levelList.contains(user.getLeveltype())) {// 排除VO用户
				String otherUuid = user.getUuid();
				int otherExp = getOtherUserExp(otherUuid, user);

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
	private PMessage rankFollowOrFirend(List<PGuy> guyList) {
		// 用户自己
		int exp = Integer.parseInt(userInfoMap.get("exp"));
		PGuy myGuy = PGuy.newBuilder().setUuid(uuid).setNickname(userInfoMap.get("nickname"))
				.setHeadImgURL(userInfoMap.get("headimg")).setRankValue(exp).build();
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
	private int getOtherUserExp(String uuid, PUser user) {
		String exp = jedis.hget(Constant.GAME_USER_INFO + uuid, "exp");
		if (Strings.isNullOrEmpty(exp)) {
			TaskUtil taskUtil = new TaskUtil(uuid, user);
			taskUtil.init();
			exp = jedis.hget(Constant.GAME_USER_INFO + uuid, "exp");
		}
		return Integer.parseInt(exp);
	}

	/*
	 * 获得用户的经验值（不存在就初始化用户）
	 */
	private int getOtherUserExp(String uuid, PRecommendUser ruser) {
		String exp = jedis.hget(Constant.GAME_USER_INFO + uuid, "exp");
		if (Strings.isNullOrEmpty(exp)) {
			PUser user = PUser.newBuilder().setUuid(uuid).setNickname(ruser.getNickname())
					.setHeadImgURL(ruser.getHeadImgURL())
					.setLevelInfo(PLevelInfo.newBuilder().setLevelType(ruser.getLeveltype())).build();

			TaskUtil taskUtil = new TaskUtil(uuid, user);
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