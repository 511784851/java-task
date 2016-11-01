package com.blemobi.task.util;

public class Constant {
	// 用户信息
	public static String GAME_USER_INFO = "game:user:";// 用户基础信息（经验值、经验等级）
	public static String GAME_TASK_MAIN = "game:task:main:";// 用户主线任务
	public static String GAME_TASK_DAILY = "game:task:daily:";// 用户每日任务
	public static String GAME_TASK_ACTIVE = "game:task:active:";// 用户已激活的每日任务
	public static String GAME_MSGID = "game:msgid:";// 用户任务消息订阅

	// 基础配置信息
	public static String GAME_BASE_TASK = "game:base:task:";// 任务配置
	public static String GAME_BASE_TARGET = "game:base:target:";// 每日任务内容随机次数
	public static String GAME_BASE_LOOP = "game:base:loop:";// 每日任务内容根据用户等级不同的次数，和随机次数有关
	public static String GAME_BASE_ACHIEVEMENT = "game:base:achievement:";// 成就配置

	// 数据配置
	public static String BASIC_LEVEL_DATA_CONFIG = "/data.xml";
	public static String BASIC_TASK_DATA_CONFIG = "/任务类型表.xls";
}