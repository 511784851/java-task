package com.blemobi.task.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;

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

	// 构造方法
	private TaskUtil(String uuid) {
		this.uuid = uuid;
		this.userInfoKey = Constant.GAME_USER_INFO + uuid;
		this.userMainTaskKey = Constant.GAME_TASK_MAIN + uuid;
		this.dailyTime = TaskHelper.getDailyDate();
		this.userDailyTaskKey = Constant.GAME_TASK_DAILY + uuid + ":" + dailyTime;
		this.jedis = RedisManager.getRedis();
	}

	public TaskUtil(String uuid, String language, String nickname, String headimg) {
		this(uuid);
		this.language = language;
		this.nickname = nickname;
		this.headimg = headimg;
	}

	public TaskUtil(String uuid, int taskId) {
		this(uuid);
		this.taskId = taskId;
	}

	// 初始化用户基础信息、主线任务
	public boolean init() {
		boolean bool = jedis.exists(userInfoKey);
		if (!bool) {// 还未初始化
			int level = LevelHelper.getLevelByExp(defaultEXP);
			// 基础信息
			jedis.hsetnx(userInfoKey, "exp", defaultEXP + "");// 经验值
			jedis.hsetnx(userInfoKey, "level", level + "");// 经验等级

			// 获取可默认接取的主线任务列表
			List<TaskInfo> mainTaskList = TaskHelper.getNoDependMainTaskByLevel(level);
			for (TaskInfo taskInfo : mainTaskList) {
				long rtn = jedis.hsetnx(userMainTaskKey, taskInfo.getTaskid() + "", "0");
				if (rtn == 1)
					SubscribeThread.addQueue(uuid, taskInfo.getType(), -1);// 消息订阅（永久）
			}
			log.debug("用户[" + uuid + "]信息初始化完成");
		}
		return bool;
	}

	// 接取每日任务
	public PMessage receive() {
		TaskInfo taskInfo = TaskHelper.getDailyTask(taskId);
		if (taskInfo != null) {
			long rtn = jedis.hsetnx(userDailyTaskKey, taskInfo.getTaskid() + "", "-1");
			if (rtn == 1) {
				RedisManager.returnResource(jedis);
				// 任务消息订阅
				SubscribeThread.addQueue(uuid, taskInfo.getType(), dailyTime);
				return ReslutUtil.createSucceedMessage();
			} else {
				RedisManager.returnResource(jedis);
				return ReslutUtil.createErrorMessage(210000, "任务已接取");
			}
		} else {
			RedisManager.returnResource(jedis);
			return ReslutUtil.createErrorMessage(210000, "任务ID不存在");
		}
	}

	// 获取任务列表
	public PMessage list() {
		// 基础信息
		Map<String, String> userInfo = jedis.hgetAll(userInfoKey);
		int level = Integer.parseInt(userInfo.get("level"));
		long exp = Long.parseLong(userInfo.get("exp"));
		LevelInfo levelInfo = LevelHelper.getNextLevelByLevel(level);

		PTaskUserBasic userBasic = PTaskUserBasic.newBuilder().setLevel(level).setExp(exp)
				.setNextLevel(levelInfo.getLevel()).setNextLevelExp(levelInfo.getExp_min()).setNickname(nickname)
				.setHeadimg(headimg).build();
		PTaskList.Builder taskListBuilder = PTaskList.newBuilder().setUserBasic(userBasic);

		// 主线任务
		List<Integer> competeTasks = new ArrayList<Integer>();// 已完成任务
		Map<String, String> userMainTask = jedis.hgetAll(userMainTaskKey);
		for (String key : userMainTask.keySet()) {
			int taskId = Integer.parseInt(key);// 任务ID
			String targetStr = userMainTask.get(key);
			int target = Integer.parseInt(targetStr);// 任务进度
			TaskInfo taskInfo = TaskHelper.getMainTask(taskId);// 任务信息
			int num = taskInfo.getNum();// 任务要求次数
			int state = 0; // 任务状态（0-未接受，1-进行中，2-可领奖，3-已完成）
			if (target >= 0 && target < num) {// 进行中
				state = 1;
			} else if (target >= num) {// 可领奖
				state = 2;
			} else {// 已完成
				competeTasks.add(taskId);
				break;
			}

			String des = TaskHelper.getTaskDes(taskInfo.getType(), language, num);

			PTaskInfo ptaskInfo = PTaskInfo.newBuilder().setTaskid(taskId).setExp(taskInfo.getExp())
					.setType(taskInfo.getType()).setState(state).setComplete(target).setNum(num).setDesc(des).build();

			taskListBuilder.addMainTask(ptaskInfo);
		}

		// 日常任务
		int max = LevelHelper.getMaxCountByLevel(level);// 用户当前等级可接最大日常任务数
		Map<String, String> userDailyTask = jedis.hgetAll(userDailyTaskKey);// 用户今日已初始化的日常任务
		Set<String> tasks = userDailyTask.keySet();
		competeTasks.removeAll(tasks);
		int syMax = max - userDailyTask.size();// 还可接多少日常任务
		if (syMax > 0) {
			List<TaskInfo> list = TaskHelper.getActiveDailyTaskList(competeTasks);
			int size = list == null ? 0 : list.size();
			if (size <= syMax) {// 全部初始化
				for (TaskInfo taskInfo : list) {
					long rtn = jedis.hsetnx(userDailyTaskKey, taskInfo.getTaskid() + "", "-1");
					if (rtn == 1) {
						SubscribeThread.addQueue(uuid, taskInfo.getType(), dailyTime);
					}
				}
				userDailyTask = jedis.hgetAll(userDailyTaskKey);// 用户今日已初始化的日常任务
			} else {// 只能初始化syMax个日常任务
				for (int i = 0; i < syMax; i++) {
					TaskInfo taskInfo = list.get(i);
					long rtn = jedis.hsetnx(userDailyTaskKey, taskInfo.getTaskid() + "", "-1");
					if (rtn == 1) {
						SubscribeThread.addQueue(uuid, taskInfo.getType(), dailyTime);
					}
				}
				userDailyTask = jedis.hgetAll(userDailyTaskKey);// 用户今日已初始化的日常任务
			}
		}
		for (String key : userDailyTask.keySet()) {
			int taskId = Integer.parseInt(key);// 任务ID
			String targetStr = userMainTask.get(taskId);
			int target = Integer.parseInt(targetStr);// 任务进度
			TaskInfo taskInfo = TaskHelper.getMainTask(taskId);// 任务信息
			int num = taskInfo.getNum();// 任务要求次数
			int state = 0; // 任务状态（0-未接受，1-进行中，2-可领奖，3-已完成）
			if (target >= 0 && target < num) {// 进行中
				state = 1;
			} else if (target == num) {// 可领奖
				state = 2;
			} else if (target < -1) {// 已完成
				state = 3;
			}

			String des = TaskHelper.getTaskDes(taskInfo.getType(), language, num);

			PTaskInfo ptaskInfo = PTaskInfo.newBuilder().setTaskid(taskId).setExp(taskInfo.getExp())
					.setType(taskInfo.getType()).setState(state).setComplete(target).setNum(num).setDesc(des).build();

			taskListBuilder.addDailyTask(ptaskInfo);
		}

		RedisManager.returnResource(jedis);
		return ReslutUtil.createReslutMessage(taskListBuilder.build());
	}

	// 领奖励
	public PMessage reward() {
		TaskInfo taskInfo = TaskHelper.getTaskDetail(taskId);
		if (taskInfo != null) {
			TaskTag tag = TaskHelper.getTaskTag(taskId);
			String userTaskKey = "";
			if (TaskTag.MAIN == tag) {// 主线任务
				userTaskKey = userMainTaskKey;
			} else if (TaskTag.DAILY == tag) {// 日常任务
				userTaskKey = userDailyTaskKey;
			}
			String target = jedis.hget(userTaskKey, taskId + "");
			int num = Integer.parseInt(target);
			if (num >= taskInfo.getNum()) {// 符合领取条件
				// 赠送奖励
				long exp = jedis.hincrBy(userInfoKey, "exp", taskInfo.getExp());// 更新经验值
				int level = LevelHelper.getLevelByExp(exp);
				jedis.hset(userInfoKey, "level", level + "");// 更新经验等级

				// 修改任务状态
				jedis.hset(userTaskKey, taskInfo.getTaskid() + "", Integer.MIN_VALUE + "");

				// 任务完成后续处理
				TaskActiveThread.addQueue(uuid);
				return ReslutUtil.createSucceedMessage();
			} else {
				RedisManager.returnResource(jedis);
				return ReslutUtil.createErrorMessage(210000, "当前不可领取");
			}
		} else {
			RedisManager.returnResource(jedis);
			return ReslutUtil.createErrorMessage(210000, "任务不存在");
		}
	}

	// 获取等级列表
	public PMessage level() {
		// 基础信息
		Map<String, String> userInfo = jedis.hgetAll(userInfoKey);
		int level = Integer.parseInt(userInfo.get("level"));
		long exp = Long.parseLong(userInfo.get("exp"));
		LevelInfo levelInfo = LevelHelper.getNextLevelByLevel(level);

		PTaskUserBasic userBasic = PTaskUserBasic.newBuilder().setLevel(level).setExp(exp)
				.setNextLevel(levelInfo.getLevel()).setNextLevelExp(levelInfo.getExp_min()).setNickname(nickname)
				.setHeadimg(headimg).build();
		PTaskLevelList.Builder taskLevelList = PTaskLevelList.newBuilder().setUserBasic(userBasic);

		// 等级信息
		for (LevelInfo li : LevelHelper.getAllLevelList()) {
			PTaskLevel taskLevel = PTaskLevel.newBuilder().setId(li.getLevel()).setName(li.getTitle_sc())
					.setMinExp(li.getExp_min()).setMaxExp(li.getExp_max()).build();
			taskLevelList.addTaskLevel(taskLevel);
		}

		RedisManager.returnResource(jedis);
		return ReslutUtil.createReslutMessage(taskLevelList.build());
	}
}