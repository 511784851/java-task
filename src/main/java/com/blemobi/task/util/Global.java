package com.blemobi.task.util;

/**
 * Redis 的key统一定义
 * 
 * @author zhaoyong
 *
 */
public class Global {
	/**
	 * 用户金币账户 [String]task:gold:<uuid>
	 */
	public static final String GOLD_KEY = "task:gold:";

	/**
	 * 用户任务信息<hashmap>h=task:daily:<uuid>,m=<k=ID,v=进度>
	 */
	public static final String DAILY_KEY = "task:daily:";
	/**
	 * 用户金币明细 [List] task:detail:<uuid>
	 */
	public static final String DETAIL_KEY = "task:detail:";
	/**
	 * 单个用户操作同步锁名称[String]task:lock:<uuid>
	 */
	public static final String LOCK_KEY = "task:lock:";

	/**
	 * 是否初始化过用户任务信息[String]task:check:<uuid>
	 */
	public static final String CHECK_KEY = "task:check:";
}
