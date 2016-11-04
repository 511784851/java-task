package com.blemobi.task.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.blemobi.library.redis.LockManager;
import com.blemobi.library.redis.RedisManager;
import com.blemobi.library.util.ReslutUtil;
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
import com.blemobi.task.notify.NotifyManager;
import com.google.common.base.Strings;

import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;

/*
 * 任务相关处理
 */
@Log4j
public class TaskUtil {
	private final long defaultEXP = 0;

	private String uuid;
	private String nickname;
	private String headimg;

	private int taskId;
	private String language;
	private String userInfoKey;
	private String userMainTaskKey;
	private String userDailyTaskKey;
	private long dailyTime;

	private Jedis jedis;

	/*
	 * 私有构造方法
	 */
	private TaskUtil(String uuid) {
		this.uuid = uuid;
		this.userInfoKey = Constant.GAME_USER_INFO + uuid;
		this.userMainTaskKey = Constant.GAME_TASK_MAIN + uuid;
		this.dailyTime = TaskHelper.getDailyDate();
		this.userDailyTaskKey = Constant.GAME_TASK_DAILY + uuid + ":" + dailyTime;
		this.jedis = RedisManager.getRedis();
	}

	/*
	 * 构造方法（用户初始化、任务列表、等级列表）
	 */
	public TaskUtil(String uuid, String language, String nickname, String headimg) {
		this(uuid);
		this.language = language;
		this.nickname = nickname;
		this.headimg = headimg;
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
		boolean bool = jedis.exists(userInfoKey);
		if (bool)
			return bool;// 已 初始化

		// 基础信息
		int level = LevelHelper.getLevelByExp(defaultEXP);
		jedis.hsetnx(userInfoKey, "exp", defaultEXP + "");// 经验值
		jedis.hsetnx(userInfoKey, "level", level + "");// 经验等级
		jedis.hsetnx(userInfoKey, "num", "0");// 已完成任务数

		// 获取可默认接取的主线任务列表
		List<TaskInfo> mainTaskList = TaskHelper.getNoDependMainTaskByLevel(level);
		for (TaskInfo taskInfo : mainTaskList) {
			long rtn = jedis.hsetnx(userMainTaskKey, taskInfo.getTaskid() + "", "0");
			if (rtn == 1)
				SubscribeThread.addQueue(uuid, taskInfo.getType(), -1);// 消息订阅（永久）
		}

		log.debug("用户[" + uuid + "]信息初始化完成");
		return bool;
	}

	/*
	 * 接取每日任务
	 */
	public PMessage receive() {
		String lock = Constant.GAME_USER_LOCK + uuid + ":RECEIVE";
		boolean isLock = LockManager.getLock(lock, 30);
		if (!isLock) {// 没有获得当前用户接任务的安全锁（很难出现此情况）
			RedisManager.returnResource(jedis);
			return ReslutUtil.createErrorMessage(1001000, "系统繁忙");
		}

		String target = jedis.hget(userDailyTaskKey, taskId + "");
		if (!"-1".equals(target)) {
			RedisManager.returnResource(jedis);
			LockManager.releaseLock(lock);
			return ReslutUtil.createErrorMessage(2201001, "任务不可接取");
		}

		jedis.hset(userDailyTaskKey, taskId + "", "0");
		RedisManager.returnResource(jedis);
		LockManager.releaseLock(lock);

		// 任务消息订阅
		SubscribeThread.addQueue(uuid, TaskHelper.getDailyTask(taskId).getType(), dailyTime);
		return ReslutUtil.createSucceedMessage();
	}

	/*
	 * 获取任务列表
	 */
	public PMessage list() {
		PTaskUserBasic userBasic = getUserBasic();// 基础信息
		PTaskList.Builder taskListBuilder = PTaskList.newBuilder().setUserBasic(userBasic);

		// 主线任务
		List<Integer> taskTypes = new ArrayList<Integer>();// 已完成主线任务类型
		Map<String, String> userMainTask = jedis.hgetAll(userMainTaskKey);
		for (String key : userMainTask.keySet()) {
			PTaskInfo taskInfo = getTaskInfo(key, userMainTask.get(key));
			if (taskInfo.getState() == 3) {// 已完成
				taskTypes.add(taskInfo.getType());
				continue;// 主线任务不显示已完成任务
			}
			taskListBuilder.addMainTask(taskInfo);
		}

		// 日常任务
		int max = LevelHelper.getMaxCountByLevel(userBasic.getLevel());// 用户当前等级可接最大日常任务数
		Map<String, String> userDailyTask = jedis.hgetAll(userDailyTaskKey);// 用户今日已初始化的日常任务
		List<Integer> taskids = new ArrayList<Integer>();// 用户今日已初始化日常任务ID
		for (String s : userDailyTask.keySet()) {
			taskids.add(Integer.parseInt(s));
		}
		int syMax = max - taskids.size();// 预计还可初始化多少日常任务
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
			int initDaily = syActiveList.size() < syMax ? syActiveList.size() : syMax;
			if (initDaily > 0) {
				// 可以继续初始化日常任务
				for (int i = 0; i < initDaily; i++) {
					TaskInfo taskInfo = syActiveList.get(i);
					jedis.hsetnx(userDailyTaskKey, taskInfo.getTaskid() + "", "-1");
				}
				// 重新获取用户今日已初始化的日常任务
				userDailyTask = jedis.hgetAll(userDailyTaskKey);
			}
		}
		for (String key : userDailyTask.keySet()) {
			PTaskInfo taskInfo = getTaskInfo(key, userDailyTask.get(key));
			if (taskInfo.getState() == 3) {
				continue;
			}
			taskListBuilder.addDailyTask(taskInfo);
		}

		RedisManager.returnResource(jedis);
		return ReslutUtil.createReslutMessage(taskListBuilder.build());
	}

	/*
	 * 领奖励
	 */
	public PMessage reward() {
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
			return ReslutUtil.createErrorMessage(2201002, "任务不可领奖励");
		}

		int target = Integer.parseInt(targetStr);
		TaskInfo taskInfo = TaskHelper.getTaskDetail(taskId);
		if (target < taskInfo.getNum()) {
			RedisManager.returnResource(jedis);
			LockManager.releaseLock(lock);
			return ReslutUtil.createErrorMessage(2201002, "任务不可领奖励");
		}

		// 符合领取条件，赠送奖励
		jedis.hset(userTaskKey, taskInfo.getTaskid() + "", Integer.MIN_VALUE + "");// 修改任务状态
		jedis.hincrBy(userInfoKey, "num", 1);// 累加一次任务完成
		long exp = jedis.hincrBy(userInfoKey, "exp", taskInfo.getExp());// 更新经验值

		String oldLevelStr = jedis.hget(userInfoKey, "level");
		int oldLevel = Integer.parseInt(oldLevelStr);// 旧的等级
		int level = LevelHelper.getLevelByExp(exp);// 新的等级
		if (level > oldLevel) {
			jedis.hset(userInfoKey, "level", level + "");// 更新经验等级
			NotifyManager notifyManager = new NotifyManager(uuid, level);
			notifyManager.notifyMsg();
		}

		RedisManager.returnResource(jedis);
		LockManager.releaseLock(lock);

		// 任务完成后续处理
		TaskActiveThread.addQueue(uuid);
		return ReslutUtil.createSucceedMessage();
	}

	/*
	 * 获取等级列表
	 */
	public PMessage level() {
		// 用户基础信息
		PTaskUserBasic userBasic = getUserBasic();
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
	private PTaskUserBasic getUserBasic() {
		Map<String, String> userInfo = jedis.hgetAll(userInfoKey);
		int level = Integer.parseInt(userInfo.get("level"));
		long exp = Long.parseLong(userInfo.get("exp"));
		LevelInfo levelInfo = LevelHelper.getLevelInfoByLevel(level);
		LevelInfo nextLevelInfo = LevelHelper.getNextLevelInfoByLevel(level);

		return PTaskUserBasic.newBuilder().setLevel(level).setExp(exp).setLevelName(levelInfo.getTitle(language))
				.setNextLevel(nextLevelInfo.getLevel()).setNextLevelExp(nextLevelInfo.getExp_min())
				.setNextLevelName(nextLevelInfo.getTitle(language)).setNickname(nickname).setHeadimg(headimg).build();
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
	private PTaskInfo getTaskInfo(String key, String targetStr) {
		int taskId = Integer.parseInt(key);// 任务ID
		int target = Integer.parseInt(targetStr);// 任务进度
		TaskInfo taskInfo = TaskHelper.getTaskDetail(taskId);// 任务信息
		int num = taskInfo.getNum();// 任务要求次数
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

		return PTaskInfo.newBuilder().setTaskid(taskId).setExp(taskInfo.getExp()).setType(taskInfo.getType())
				.setState(state).setComplete(complete).setNum(num).setDesc(des).build();
	}
}