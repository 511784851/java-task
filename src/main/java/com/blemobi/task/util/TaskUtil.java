package com.blemobi.task.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blemobi.library.cache.UserBaseCache;
import com.blemobi.library.consul.BaseService;
import com.blemobi.library.redis.LockManager;
import com.blemobi.library.redis.RedisManager;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.AccountProtos.PUserBase;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.TaskProtos.PTaskInfo;
import com.blemobi.sep.probuf.TaskProtos.PTaskLevel;
import com.blemobi.sep.probuf.TaskProtos.PTaskLevelList;
import com.blemobi.sep.probuf.TaskProtos.PTaskList;
import com.blemobi.sep.probuf.TaskProtos.PTaskUserBasic;
import com.blemobi.task.basic.LevelHelper;
import com.blemobi.task.basic.LevelInfo;
import com.blemobi.task.basic.TaskHelper;
import com.blemobi.task.basic.TaskInfo;
import com.blemobi.task.basic.TaskTag;
import com.blemobi.task.msg.AchievementMsg;
import com.blemobi.task.msg.NotifyMsg;
import com.blemobi.task.msg.SubscribeMsg;
import com.google.common.base.Strings;

import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;

/*
 * 任务相关处理
 */
@Log4j
public class TaskUtil {
	public final static String diffculty = "dif_";
	private final long defaultEXP = 0;

	private String uuid;
	// private PUser user;

	private int taskId;
	private String language;
	private String userInfoKey;
	private String userMainTaskKey;
	private String userDailyTaskKey;
	private long dailyTime;

	/*
	 * 构造方法（用户初始化）
	 */
	public TaskUtil(String uuid) {
		this.uuid = uuid;
		this.userInfoKey = Constant.GAME_USER_INFO + uuid;
		this.userMainTaskKey = Constant.GAME_TASK_MAIN + uuid;
		this.dailyTime = TaskHelper.getDailyDate();
		this.userDailyTaskKey = Constant.GAME_TASK_DAILY + uuid + ":" + dailyTime;
	}

	/*
	 * 构造方法（任务列表、等级列表）
	 */
	public TaskUtil(String uuid, String language) {
		this(uuid);
		this.language = language;
	}

	/*
	 * 构造方法（接取任务，领取任务奖励）
	 */
	public TaskUtil(String uuid, int taskId) {
		this(uuid);
		this.taskId = taskId;
	}

	/*
	 * 初始化用户基础信息、主线任务
	 */
	public boolean init() {
		Jedis jedis = RedisManager.getRedis();
		// 基础信息
		int level = LevelHelper.getLevelByExp(defaultEXP);
		jedis.hsetnx(userInfoKey, "exp", defaultEXP + "");// 经验值
		jedis.hsetnx(userInfoKey, "level", level + "");// 经验等级
		jedis.hsetnx(userInfoKey, "num", "0");// 已完成任务数
		// 获取可默认接取的主线任务列表
		List<TaskInfo> mainTaskList = TaskHelper.getNoDependMainTaskByLevel(level);
		for (TaskInfo taskInfo : mainTaskList) {
			long rtn = jedis.hsetnx(userMainTaskKey, taskInfo.getTaskid() + "", "0");
			if (rtn == 1) {
				SubscribeMsg.add(uuid, taskInfo.getType(), -1);// 消息订阅（永久）
			}
		}
		RedisManager.returnResource(jedis);
		AchievementMsg.add(uuid, 100, level);// 等级成就
		log.debug("用户[" + uuid + "]信息初始化完成");
		return true;
	}

	/*
	 * 接取每日任务
	 */
	public boolean receive() {
		if (TaskHelper.getTaskTag(taskId) != TaskTag.DAILY) {// 非日常任务
			return true;
		}
		String lock = Constant.GAME_USER_LOCK + uuid + ":RECEIVE";
		Jedis jedis = RedisManager.getRedis();
		boolean isLock = LockManager.getLock(lock, 30);
		if (!isLock) {// 没有获得当前用户接任务的安全锁（很难出现此情况）
			RedisManager.returnResource(jedis);
			return false;
		}
		String target = jedis.hget(userDailyTaskKey, taskId + "");
		if (!"-1".equals(target)) {
			RedisManager.returnResource(jedis);
			LockManager.releaseLock(lock);
			return false;
		}
		jedis.hset(userDailyTaskKey, taskId + "", "0");
		SubscribeMsg.add(uuid, TaskHelper.getDailyTask(taskId).getType(), dailyTime);// 消息订阅

		RedisManager.returnResource(jedis);
		LockManager.releaseLock(lock);
		return true;
	}

	/*
	 * 获取任务列表
	 */
	public PMessage list() throws IOException {
		Jedis jedis = RedisManager.getRedis();
		PTaskUserBasic userBasic = getUserBasic(jedis);// 基础信息
		PTaskList.Builder taskListBuilder = PTaskList.newBuilder().setUserBasic(userBasic);

		// 主线任务
		List<Integer> taskTypes = new ArrayList<Integer>();// 已完成主线任务类型
		Map<String, String> userMainTask = jedis.hgetAll(userMainTaskKey);
		for (String key : userMainTask.keySet()) {
			PTaskInfo taskInfo = getTaskInfo(key, userMainTask.get(key), 0);
			if (taskInfo.getState() == 3) {// 已完成
				taskTypes.add(taskInfo.getType());
				continue;// 主线任务不显示已完成任务
			}
			taskListBuilder.addMainTask(taskInfo);
		}

		// 日常任务
		if (getIsDaily(jedis)) {
			int max = LevelHelper.getMaxCountByLevel(userBasic.getLevel());// 用户当前等级可接最大日常任务数
			Map<String, String> userDailyTask = jedis.hgetAll(userDailyTaskKey);// 用户今日已初始化的日常任务
			List<Integer> taskids = new ArrayList<Integer>();// 用户今日已初始化日常任务ID
			int count = 0;// 已初始化困难和史诗任务数量
			for (String s : userDailyTask.keySet()) {
				if (s.startsWith(diffculty)) {
					String d = userDailyTask.get(s);
					if ("3".equals(d) || "4".equals(d)) {
						count++;
					}
				} else {
					taskids.add(Integer.parseInt(s));
				}
			}
			int syMax = max - taskids.size();// 预计还可初始化多少日常任务
			log.debug("用户[" + uuid + "]预计还可初始化日常任务数量：" + syMax);
			if (syMax > 0) {
				// 剩余可初始化的日常任务
				List<TaskInfo> syActiveList = new ArrayList<TaskInfo>();
				// 已激活的日常任务列表
				List<TaskInfo> activeList = TaskHelper.getActiveDailyTaskList(taskTypes);
				// 排除今日已初始化的任务
				for (TaskInfo taskInfo : activeList) {
					if (!taskids.contains(taskInfo.getTaskid())) {
						syActiveList.add(taskInfo);
					}
				}
				// 今日剩余可初始化日常任务的数量
				int size = syActiveList.size();
				log.debug("用户[" + uuid + "]最终还可初始化日常任务数量：" + syMax);
				if (size > 0) {
					int max_h = LevelHelper.getMaxHCountByLevel(userBasic.getLevel());// 用户当前等级对应的可接最大困难和史诗日常任务数量
					if (size <= syMax) {// 剩余任务可以全部可初始化
						for (int i = 0; i < size; i++) {
							TaskInfo taskInfo = syActiveList.get(i);
							count = stepDailyTask(taskInfo, userBasic.getLevel(), count, max_h, jedis);
						}
					} else {// 在剩余任务中随机挑选任务
						Set<Integer> set = TaskHelper.getRandomSet(size, syMax);// 随机任务ID
						for (int i : set) {
							TaskInfo taskInfo = syActiveList.get(i);
							count = stepDailyTask(taskInfo, userBasic.getLevel(), count, max_h, jedis);
						}
					}
					userDailyTask = jedis.hgetAll(userDailyTaskKey);
				}
			}
			for (String key : userDailyTask.keySet()) {
				if (!key.startsWith(diffculty)) {
					int did = Integer.parseInt(userDailyTask.get(diffculty + key));
					PTaskInfo taskInfo = getTaskInfo(key, userDailyTask.get(key), did);
					if (taskInfo.getState() == 3) {
						continue;
					}
					taskListBuilder.addDailyTask(taskInfo);
				}
			}
		}

		RedisManager.returnResource(jedis);
		return ReslutUtil.createReslutMessage(taskListBuilder.build());
	}

	/*
	 * 领奖励
	 */
	public PMessage reward() {
		Jedis jedis = RedisManager.getRedis();
		String lock = Constant.GAME_USER_LOCK + uuid + ":REWARD";
		boolean isLock = LockManager.getLock(lock, 30);
		if (!isLock) {// 没有获得当前用户领奖励的安全锁（很难出现此情况）
			RedisManager.returnResource(jedis);
			return ReslutUtil.createErrorMessage(1001000, "系统繁忙");
		}

		// 任务进度
		String userTaskKey = getUserTaskKey();
		String targetStr = jedis.hget(userTaskKey, taskId + "");
		if (Strings.isNullOrEmpty(targetStr)) {
			RedisManager.returnResource(jedis);
			LockManager.releaseLock(lock);
			return ReslutUtil.createErrorMessage(2901002, "任务不可领奖励");
		}

		int target = Integer.parseInt(targetStr);
		TaskInfo taskInfo = TaskHelper.getTaskDetail(taskId);
		int num = taskInfo.getNum();// 任务要求次数
		long exp = taskInfo.getExp();// 任务经验值
		if (TaskHelper.getTaskTag(taskId) == TaskTag.DAILY) {
			int did = Integer.parseInt(jedis.hget(userTaskKey, diffculty + taskId));
			num = TaskHelper.getDailyTaskNum(taskId, did);
			exp = TaskHelper.getDailyTaskExp(taskId, did);
		}
		if (target < num) {
			RedisManager.returnResource(jedis);
			LockManager.releaseLock(lock);
			return ReslutUtil.createErrorMessage(2901002, "任务不可领奖励");
		}

		// 符合领取条件，赠送奖励
		jedis.hset(userTaskKey, taskInfo.getTaskid() + "", Integer.MIN_VALUE + "");// 修改任务状态
		jedis.hincrBy(userInfoKey, "num", 1);// 累加一次任务完成
		long newExp = jedis.hincrBy(userInfoKey, "exp", exp);// 更新经验值
		if (newExp > LevelHelper.getMaxExp()) {
			newExp = LevelHelper.getMaxExp();
			jedis.hset(userInfoKey, "exp", newExp + "");
			log.debug("用户[" + uuid + "]已达最大经验值：" + newExp);
		}

		String oldLevelStr = jedis.hget(userInfoKey, "level");
		int oldLevel = Integer.parseInt(oldLevelStr);// 旧的等级
		int level = LevelHelper.getLevelByExp(newExp);// 新的等级
		if (level > oldLevel) {
			jedis.hset(userInfoKey, "level", level + "");// 更新经验等级
			// 等级推送和通知
			NotifyMsg.add(uuid, level);
			AchievementMsg.add(uuid, 100, level);// 等级成就
		}

		// 任务完成后续处理
		new TaskActive(uuid, jedis).step();
		RedisManager.returnResource(jedis);
		LockManager.releaseLock(lock);

		// 任务达成成就
		TaskTag tag = TaskHelper.getTaskTag(taskId);
		if (tag == TaskTag.MAIN) {
			AchievementMsg.add(uuid, 400, 1);
		} else if (tag == TaskTag.DAILY) {
			AchievementMsg.add(uuid, 401, 1);
		}

		return ReslutUtil.createSucceedMessage();
	}

	/*
	 * 获取等级列表
	 */
	public PMessage level() throws IOException {
		// 用户基础信息
		Jedis jedis = RedisManager.getRedis();
		PTaskUserBasic userBasic = getUserBasic(jedis);
		RedisManager.returnResource(jedis);

		PTaskLevelList.Builder taskLevelList = PTaskLevelList.newBuilder().setUserBasic(userBasic);

		// 等级信息
		for (LevelInfo li : LevelHelper.getAllLevelList()) {
			PTaskLevel taskLevel = PTaskLevel.newBuilder().setId(li.getLevel()).setName(li.getTitle(language))
					.setMinExp(li.getExp_min()).setMaxExp(li.getExp_max()).build();
			taskLevelList.addTaskLevel(taskLevel);
		}

		return ReslutUtil.createReslutMessage(taskLevelList.build());
	}

	/*
	 * 获取用户基础信息
	 */
	private PTaskUserBasic getUserBasic(Jedis jedis) throws IOException {
		Map<String, String> userInfo = jedis.hgetAll(userInfoKey);
		int level = Integer.parseInt(userInfo.get("level"));
		long exp = Long.parseLong(userInfo.get("exp"));
		PUserBase userBase = UserBaseCache.get(uuid);
		String nickname = userBase.getNickname();
		String headimg = userBase.getHeadImgURL();
		LevelInfo levelInfo = LevelHelper.getLevelInfoByLevel(level);
		LevelInfo nextLevelInfo = LevelHelper.getNextLevelInfoByLevel(level);

		return PTaskUserBasic.newBuilder().setLevel(level).setExp(exp).setLevelName(levelInfo.getTitle(language))
				.setNextLevel(nextLevelInfo.getLevel()).setNextLevelExp(nextLevelInfo.getExp_min())
				.setNextLevelName(nextLevelInfo.getTitle(language)).setNickname(nickname).setHeadimg(headimg).build();
	}

	/*
	 * 产生一个随机难度日常任务并初始化
	 */
	private int stepDailyTask(TaskInfo taskInfo, int level, int count, int max_h, Jedis jedis) {
		int dif = 1;// 随机任务难度
		if (max_h - count > 0) {
			// 在所有难度中根据概率产生一个难度ID
			dif = LevelHelper.getRandomDifficultyAll(level);
		} else {
			// 在简单和一般难度任务中根据概率产生一个难度ID
			dif = LevelHelper.getRandomDifficulty(level);
		}
		if (dif == 3 || dif == 4) {
			count++;
		}
		jedis.hsetnx(userDailyTaskKey, taskInfo.getTaskid() + "", "-1");
		jedis.hsetnx(userDailyTaskKey, diffculty + taskInfo.getTaskid(), dif + "");
		return count;
	}

	/*
	 * 根据taskId获取用户任务的key
	 */
	private String getUserTaskKey() {
		TaskTag tag = TaskHelper.getTaskTag(taskId);
		if (tag == TaskTag.MAIN) // 主线任务
			return userMainTaskKey;
		else if (tag == TaskTag.DAILY) // 日常任务
			return userDailyTaskKey;
		return null;
	}

	/*
	 * 获取任务信息
	 */
	private PTaskInfo getTaskInfo(String key, String targetStr, int did) {
		int taskId = Integer.parseInt(key);// 任务ID
		int target = Integer.parseInt(targetStr);// 任务进度
		TaskInfo taskInfo = TaskHelper.getTaskDetail(taskId);// 任务信息
		int num = taskInfo.getNum();// 任务要求次数
		long exp = taskInfo.getExp();// 任务经验值
		if (TaskHelper.getTaskTag(taskId) == TaskTag.DAILY) {
			num = TaskHelper.getDailyTaskNum(taskId, did);
			exp = TaskHelper.getDailyTaskExp(taskId, did);
		}
		int complete = 0;// 已完成次数
		int state = 0; // 任务状态（0-未接受，1-进行中，2-可领奖，3-已完成）
		if (target == -1) {
			state = 0;
			complete = 0;
		} else if (target >= 0 && target < num) {// 进行中
			state = 1;
			complete = target;
		} else if (target >= num) {// 可领奖
			state = 2;
			complete = num;
		} else if (target < -1) {// 已完成
			state = 3;
			complete = num;
		}

		String des = TaskHelper.getTaskDes(taskInfo.getType(), language, num);

		return PTaskInfo.newBuilder().setTaskid(taskId).setExp(exp).setType(taskInfo.getType()).setState(state)
				.setComplete(complete).setNum(num).setDesc(des).build();
	}

	/*
	 * 验证是否达到接取日常任务的条件
	 */
	private boolean getIsDaily(Jedis jedis) {
		String daily_max_man_str = BaseService.getProperty("daily_max_man");
		int daily_max_man = Integer.parseInt(daily_max_man_str);
		int totol = 0;
		Map<String, String> map = jedis.hgetAll(userMainTaskKey);
		if (map != null) {
			for (String s : map.values()) {
				if (Integer.parseInt(s) < -1) {// 已完成
					totol++;
				}
			}
		}
		log.debug("用户[" + uuid + "]已完成主线任务数量：" + totol);
		return totol >= daily_max_man ? true : false;
	}
}