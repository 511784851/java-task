package com.blemobi.task.basic;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j;

/**
 * 任务信息管理类
 * 
 * @author zhaoyong
 *
 */
@Log4j
public class TaskData {
	// 存放全部消息信息
	private static Map<Short, MsgInfo> MSG_DATA = new HashMap<Short, MsgInfo>();
	// 存放全部任务信息
	private static Map<Short, TaskInfo> TASK_DATA = new HashMap<Short, TaskInfo>();
	// 消息分发服务器分组
	private static Map<String, List<MsgInfo>> MSG_GROUP = new HashMap<String, List<MsgInfo>>();

	static {
		try {
			new TaskFile(MSG_DATA, TASK_DATA).load();
			MSG_GROUP = MSG_DATA.values().stream().collect(Collectors.groupingBy(MsgInfo::getServer));
		} catch (Exception e) {
			log.error("加载任务配置信息异常：" + e.getMessage());
			e.printStackTrace();
			System.exit(0);
		} finally {
			MSG_DATA.values().forEach(System.out::println);
			TASK_DATA.values().forEach(System.out::println);
		}
	}

	/**
	 * 获取全部任务信息
	 * 
	 * @return Collection<TaskInfo>任务信息列表
	 */
	public static Collection<TaskInfo> get() {
		return TASK_DATA.values();
	}

	/**
	 * 根据任务ID获取任务信息
	 * 
	 * @param ID
	 *            任务ID
	 * @return TaskInfo任务信息
	 */
	public static TaskInfo get(short ID) {
		return TASK_DATA.get(ID);
	}

	/**
	 * 获取全部新手任务
	 * 
	 * @return List<TaskInfo>任务信息列表
	 */
	public static List<TaskInfo> getForNovic() {
		return filter(t -> t.isNocivTask());
	}

	/**
	 * 获取全部日常任务
	 * 
	 * @return List<TaskInfo>任务信息列表
	 */
	public static List<TaskInfo> getForDaily() {
		return filter(t -> t.isDailyTask());
	}

	/**
	 * 根据条件过滤
	 * 
	 * @param condition
	 *            过滤条件
	 * @return List<TaskInfo>任务信息列表
	 */
	private static List<TaskInfo> filter(Predicate<TaskInfo> p) {
		return TASK_DATA.values().stream().filter(t -> p.test(t)).collect(Collectors.toList());
	}

	/**
	 * 获得所有消息分组数据
	 * 
	 * @return
	 */
	public static Map<String, List<MsgInfo>> getMsgGroup() {
		return MSG_GROUP;
	}

	/**
	 * 获取当日12点时间戳（秒）
	 * 
	 * @return long 时间
	 */
	@SuppressWarnings("static-access")
	public static long getDailyDate() {
		Calendar c = Calendar.getInstance();// 使用默认时区和语言环境获得一个日历。
		c.add(Calendar.DAY_OF_MONTH, +1);// 取当前日期的后一天.
		c.set(c.get(c.YEAR), c.get(c.MONTH), c.get(c.DATE), 0, 0, 0);
		return c.getTime().getTime() / 1000;
	}

}