package com.blemobi.task.basic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/*
 * 用户游戏等级管理类
 */
public class TaskHelper {
	// 根据日常任务ID获取日常任务详情
	public static TaskInfo getDailyTask(int taskId) {
		return BasicData.dailyTaskMap.get(taskId);
	}

	// 根据日常任务ID获取日常任务详情
	public static List<TaskInfo> getDailyTaskList(int... taskIds) {
		List<TaskInfo> taskList = new ArrayList<TaskInfo>();
		for (int taskId : taskIds) {
			taskList.add(getDailyTask(taskId));
		}
		return taskList;
	}

	// 根据主线任务ID获取主线任务详情
	public static TaskInfo getMainTask(int taskId) {
		return BasicData.mainTaskMap.get(taskId);
	}

	// 根据主线任务ID获取主线任务详情
	public static TaskInfo getTaskDetail(int taskId) {
		TaskTag tag = getTaskTag(taskId);
		if (tag == TaskTag.MAIN) {
			return getMainTask(taskId);
		} else if (tag == TaskTag.DAILY) {
			return getDailyTask(taskId);
		}
		return null;
	}

	// 根据任务ID获取任务Tag（主线还是日常）
	public static TaskTag getTaskTag(int taskId) {
		return BasicData.taskIdtoTag.get(taskId);
	}

	// 获取msgid对应的逻辑服务器名称
	public static String getServerByMsgid(int msgid) {
		return BasicData.taskTypeMap.get(msgid).getServer();
	}

	// 获取任务类型的任务描述
	public static String getTaskDes(int type, String language, int... params) {
		String desc = "";
		if ("zh_tw".equals(language)) {// 中文繁体
			desc = BasicData.taskTypeMap.get(type).getDesc_tc();
		} else if ("en_us".equals(language)) {// 英文
			desc = BasicData.taskTypeMap.get(type).getDesc_en();
		} else if ("ko_kr".equals(language)) {// 韩文
			desc = BasicData.taskTypeMap.get(type).getDesc_kr();
		} else {// 中文简体
			desc = BasicData.taskTypeMap.get(type).getDesc_sc();
		}
		return desc.replace("%d", params[0] + "");
	}

	// 获取已激活的日常任务列表
	public static List<TaskInfo> getActiveDailyTaskList(List<Integer> taskIds) {
		List<TaskInfo> taskList = new ArrayList<TaskInfo>();

		List<Integer> types = new ArrayList<Integer>();
		for (int taskId : taskIds) {
			types.add(getMainTask(taskId).getType());
		}

		Collection<TaskInfo> coll = BasicData.dailyTaskMap.values();
		for (TaskInfo taskInfo : coll) {
			if (types.contains(taskInfo.getType())) {
				taskList.add(taskInfo);
			}
		}
		return taskList;
	}

	/*
	 * 获取符合等级有依赖的主线任务
	 */
	public static List<TaskInfo> getDependMainTaskByLevel(int level) {
		List<TaskInfo> taskList = new ArrayList<TaskInfo>();
		for (TaskInfo taskInfo : BasicData.mainTaskMap.values()) {
			if (level >= taskInfo.getLevel()) {// 等级符合
				int[] depends = taskInfo.getDepend();
				if (depends != null && depends.length > 0) {// 有依赖
					taskList.add(taskInfo);
				}
			}
		}
		return taskList;
	}

	/*
	 * 获取符合等级无依赖的主线任务
	 */
	public static List<TaskInfo> getNoDependMainTaskByLevel(int level) {
		List<TaskInfo> taskList = new ArrayList<TaskInfo>();
		for (TaskInfo taskInfo : BasicData.mainTaskMap.values()) {
			if (level >= taskInfo.getLevel()) {// 等级符合
				int[] depends = taskInfo.getDepend();
				if (depends == null || depends.length == 0) {// 无依赖
					taskList.add(taskInfo);
				}
			}
		}
		return taskList;
	}

	// 获取msgid对应的任务列表
	public static Map<Integer, TaskTag> getTaskListByMsgid(int msgid) {
		TaskTypeInfo taskTypeInfo = BasicData.taskTypeMap.get(msgid);
		return taskTypeInfo.getTaskidMap();
	}

	/*
	 * 获取当晚X点的时间戳（秒）
	 */
	public static long getDailyDate() {
		Calendar calendar = Calendar.getInstance();// 使用默认时区和语言环境获得一个日历。
		calendar.add(Calendar.DAY_OF_MONTH, +1);// 取当前日期的后一天.

		calendar.set(calendar.get(calendar.YEAR), calendar.get(calendar.MONTH), calendar.get(calendar.DATE), 0, 0, 0);
		Date time = calendar.getTime();

		return time.getTime() / 1000;
	}

	// 生成不重复的随机数集合
	private static Set<Integer> getRandomSet(int max, int number) {
		Map<Integer, Object> map = new HashMap<Integer, Object>();
		Random ra = new Random();
		while (map.size() < number) {
			int r = ra.nextInt(max);
			map.put(r, r);
		}

		return map.keySet();
	}
}