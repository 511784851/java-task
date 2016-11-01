package com.blemobi.task.basic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.blemobi.task.util.Constant;

import lombok.extern.log4j.Log4j;

/*
 * 用户游戏等级管理类
 */
@Log4j
public class TaskHelper {
	// 全部任务类型（消息订阅）
	private static Map<Integer, TaskTypeInfo> taskTypeMap = new LinkedHashMap<Integer, TaskTypeInfo>();
	// 全部主线任务
	private static Map<Integer, TaskInfo> mainTaskMap = new LinkedHashMap<Integer, TaskInfo>();
	// 全部日常任务
	private static Map<Integer, TaskInfo> dailyTaskMap = new LinkedHashMap<Integer, TaskInfo>();
	// 任务ID对应的任务Tag（主线还是日常）
	private static Map<Integer, TaskTag> taskIdtoTag = new LinkedHashMap<Integer, TaskTag>();

	static {
		try {
			CsvFileUtil csvFileUtil = new CsvFileUtil(Constant.BASIC_TASK_DATA_CONFIG, taskTypeMap, mainTaskMap,
					taskIdtoTag, dailyTaskMap);
			csvFileUtil.readTaskType(0);
			csvFileUtil.readMainTask(1);
			csvFileUtil.readDailyTask(2);
			csvFileUtil.close();
			csvFileUtil.out();
			log.debug("任务配置数据读取完成！");
		} catch (Exception e) {
			log.error("任务配置数据读取异常：" + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void init() {

	}

	// 根据日常任务ID获取日常任务详情
	public static TaskInfo getDailyTask(int taskId) {
		return dailyTaskMap.get(taskId);
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
		return mainTaskMap.get(taskId);
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
		return taskIdtoTag.get(taskId);
	}

	// 获取msgid对应的逻辑服务器名称
	public static String getServerByMsgid(int msgid) {
		return taskTypeMap.get(msgid).getServer();
	}

	// 获取任务类型的任务描述
	public static String getTaskDes(int type, String language, int... params) {
		return taskTypeMap.get(type).getDesc_sc().replace("%d", params[0] + "");
	}

	// 获取已激活的日常任务列表
	public static List<TaskInfo> getActiveDailyTaskList(List<Integer> taskIds) {
		List<TaskInfo> taskList = new ArrayList<TaskInfo>();

		List<Integer> types = new ArrayList<Integer>();
		for (int taskId : taskIds) {
			types.add(getMainTask(taskId).getType());
		}

		Collection<TaskInfo> coll = dailyTaskMap.values();
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
		for (TaskInfo taskInfo : mainTaskMap.values()) {
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
		for (TaskInfo taskInfo : mainTaskMap.values()) {
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
		TaskTypeInfo taskTypeInfo = taskTypeMap.get(msgid);
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