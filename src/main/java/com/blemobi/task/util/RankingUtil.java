package com.blemobi.task.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;

import com.blemobi.library.redis.RedisManager;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.AccountProtos.PUser;
import com.blemobi.sep.probuf.AccountProtos.PUserBase;
import com.blemobi.sep.probuf.DataPublishingProtos.PGuy;
import com.blemobi.sep.probuf.DataPublishingProtos.PRank;
import com.blemobi.sep.probuf.NewsProtos.PRecommendUser;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.task.notify.UserRelation;
import com.google.common.base.Strings;

import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;

/*
 * 经验值排名
 */
@Log4j
public class RankingUtil {
	private final int rankmax = 200;
	private static long cacheTime = 0;
	private static List<PGuy> allRankGuyList = new ArrayList<PGuy>();

	private String uuid;
	private String language;
	private String userInfoKey;
	private long exp = 0;
	private Jedis jedis;

	/*
	 * 构造方法
	 */
	public RankingUtil(String uuid, String language) {
		this.uuid = uuid;
		this.userInfoKey = Constant.GAME_USER_INFO + uuid;
		this.language = language;
		this.jedis = RedisManager.getRedis();

		String expStr = jedis.hget(userInfoKey, "exp");
		if (!Strings.isNullOrEmpty(expStr)) {
			exp = Long.parseLong(expStr);
		}
	}

	public PMessage rankingAll() throws ClientProtocolException, IOException {
		if (System.currentTimeMillis() - cacheTime > 10 * 60 * 1000) {
			// 重新排行
			Set<String> set = jedis.keys(Constant.GAME_USER_INFO + "*");
			long[] expArray = new long[set.size()];
			String[] uuidArray = new String[set.size()];
			int c = 0;
			for (String key : set) {
				String uuid = key.substring(Constant.GAME_USER_INFO.length());
				long exp = Long.parseLong(jedis.hget(key, "exp"));
				expArray[c] = exp;
				uuidArray[c] = uuid;
				c++;
			}

			int max = expArray.length > rankmax ? rankmax : expArray.length;
			// 冒泡排序
			long expTemp = 0;
			String uuidTemp = "";
			for (int i = 0; i < max; i++) {
				for (int j = i + 1; j < max; j++) {
					if (expArray[i] < expArray[j]) {
						expTemp = expArray[i];
						expArray[i] = expArray[j];
						expArray[j] = expTemp;

						uuidTemp = uuidArray[i];
						uuidArray[i] = uuidArray[j];
						uuidArray[j] = uuidTemp;
					}
				}
			}

			String uuids = "";
			for (String u : uuidArray) {
				if (uuids.length() > 0) {
					uuids += ",";
				}
				uuids += u;
			}
			// 批量获取用户信息
			List<PUserBase> userBaseList = UserRelation.getUserListInfo(uuids);
			List<PGuy> rankGuyList = new ArrayList<PGuy>();
			for (int i = 0; i < userBaseList.size(); i++) {
				PUserBase userBase = userBaseList.get(i);

				PGuy guy = PGuy.newBuilder().setUuid(userBase.getUUID()).setNickname(userBase.getNickname())
						.setHeadImgURL(userBase.getHeadImgURL()).setRankValue((int) expArray[i]).build();

				rankGuyList.add(guy);
			}

			allRankGuyList = rankGuyList;
			cacheTime = System.currentTimeMillis();
			log.debug("总排行数据计算完成！" + userBaseList.size());
		}

		PRank.Builder rankBuilder = PRank.newBuilder();
		rankBuilder.setRankValue((int) exp);
		int ranknum = -1;
		for (int i = 0; i < allRankGuyList.size(); i++) {
			PGuy guy = allRankGuyList.get(i);
			if (uuid.equals(guy.getUuid())) {// 自己
				ranknum = i + 1;
			}
			rankBuilder.addGuys(guy);
		}
		rankBuilder.setRank(ranknum);

		return ReslutUtil.createReslutMessage(rankBuilder.build());
	}

	// 好友排名
	public PMessage rankFirend() throws ClientProtocolException, IOException {
		// 获取用户自己
		List<PUserBase> userBaseList = UserRelation.getUserListInfo(uuid);
		PUserBase userBase = userBaseList.get(0);
		PUser myuser = PUser.newBuilder().setUuid(userBase.getUUID()).setHeadImgURL(userBase.getHeadImgURL())
				.setNickname(userBase.getNickname()).build();

		List<PUser> firends = UserRelation.getFirendList(uuid);
		log.debug("好友数量！" + firends.size());
		List<PUser> firendList = new ArrayList<PUser>();

		Set<String> set = jedis.keys(Constant.GAME_USER_INFO + "*");
		for (PUser user : firends) {
			log.debug("好友uuid！" + uuid);
			for (String key : set) {
				String uuid = key.substring(Constant.GAME_USER_INFO.length());
				if (user.getUuid().equals(uuid)) {
					firendList.add(user);
					break;
				}
			}
		}
		firendList.add(myuser);
		long[] expArray = new long[firendList.size()];
		PUser[] userArray = new PUser[firendList.size()];
		int c = 0;
		for (PUser user : firendList) {
			String uuid = user.getUuid();
			long exp = Long.parseLong(jedis.hget(Constant.GAME_USER_INFO + uuid, "exp"));
			expArray[c] = exp;
			userArray[c] = user;
			c++;
		}

		long expTemp = 0;
		PUser userTemp = null;
		for (int i = 0; i < expArray.length; i++) {
			for (int j = i + 1; j < expArray.length; j++) {
				if (expArray[i] < expArray[j]) {
					expTemp = expArray[i];
					expArray[i] = expArray[j];
					expArray[j] = expTemp;

					userTemp = userArray[i];
					userArray[i] = userArray[j];
					userArray[j] = userTemp;
				}
			}
		}

		PRank.Builder rankBuilder = PRank.newBuilder();
		rankBuilder.setRankValue((int) exp);
		int ranknum = -1;
		for (int i = 0; i < userArray.length; i++) {
			PUser user = userArray[i];
			if (uuid.equals(user.getUuid())) {// 自己
				ranknum = i + 1;
			}

			PGuy guy = PGuy.newBuilder().setUuid(user.getUuid()).setNickname(user.getNickname())
					.setHeadImgURL(user.getHeadImgURL()).setRankValue((int) expArray[i]).build();

			rankBuilder.addGuys(guy);
		}
		rankBuilder.setRank(ranknum);
		return ReslutUtil.createReslutMessage(rankBuilder.build());
	}

	// 关注排名
	public PMessage rankFollow() throws ClientProtocolException, IOException {
		// 获取用户自己
		List<PUserBase> userBaseList = UserRelation.getUserListInfo(uuid);
		PUserBase userBase = userBaseList.get(0);
		PRecommendUser myuser = PRecommendUser.newBuilder().setUuid(userBase.getUUID())
				.setHeadImgURL(userBase.getHeadImgURL()).setNickname(userBase.getNickname()).build();

		List<PRecommendUser> firends = UserRelation.getFollowList(uuid);
		log.debug("关注数量！" + firends.size());
		List<PRecommendUser> firendList = new ArrayList<PRecommendUser>();

		Set<String> set = jedis.keys(Constant.GAME_USER_INFO + "*");
		for (PRecommendUser user : firends) {
			log.debug("关注uuid！" + uuid);
			for (String key : set) {
				String uuid = key.substring(Constant.GAME_USER_INFO.length());
				if (user.getUuid().equals(uuid)) {
					firendList.add(user);
					break;
				}
			}
		}
		firendList.add(myuser);
		long[] expArray = new long[firendList.size()];
		PRecommendUser[] userArray = new PRecommendUser[firendList.size()];
		int c = 0;
		for (PRecommendUser user : firendList) {
			String uuid = user.getUuid();
			long exp = Long.parseLong(jedis.hget(Constant.GAME_USER_INFO + uuid, "exp"));
			expArray[c] = exp;
			userArray[c] = user;
			c++;
		}

		long expTemp = 0;
		PRecommendUser userTemp = null;
		for (int i = 0; i < expArray.length; i++) {
			for (int j = i + 1; j < expArray.length; j++) {
				if (expArray[i] < expArray[j]) {
					expTemp = expArray[i];
					expArray[i] = expArray[j];
					expArray[j] = expTemp;

					userTemp = userArray[i];
					userArray[i] = userArray[j];
					userArray[j] = userTemp;
				}
			}
		}

		PRank.Builder rankBuilder = PRank.newBuilder();
		rankBuilder.setRankValue((int) exp);
		int ranknum = -1;
		for (int i = 0; i < userArray.length; i++) {
			PRecommendUser user = userArray[i];
			if (uuid.equals(user.getUuid())) {// 自己
				ranknum = i + 1;
			}

			PGuy guy = PGuy.newBuilder().setUuid(user.getUuid()).setNickname(user.getNickname())
					.setHeadImgURL(user.getHeadImgURL()).setRankValue((int) expArray[i]).build();

			rankBuilder.addGuys(guy);
		}
		rankBuilder.setRank(ranknum);
		return ReslutUtil.createReslutMessage(rankBuilder.build());
	}
}