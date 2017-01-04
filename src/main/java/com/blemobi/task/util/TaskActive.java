package com.blemobi.task.util;

import java.util.List;

import com.blemobi.task.basic.TaskHelper;
import com.blemobi.task.basic.TaskInfo;
import com.blemobi.task.msg.SubscribeMsgPool;
import com.google.common.base.Strings;

import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;

/**
 * 经验等级提升或主线任务完成后续处理
 * 
 * @author zhaoyong
 *
 */
@Log4j
public class TaskActive {
	private String uuid;
	private String userInfoKey;
	private String userMainTaskKey;
	private Jedis jedis;

	/**
	 * 构造方法
	 */
	public TaskActive(String uuid, Jedis jedis) {
		this.uuid = uuid;
		this.userInfoKey = Constant.GAME_USER_INFO + uuid;
		this.userMainTaskKey = Constant.GAME_TASK_MAIN + uuid;
		this.jedis = jedis;
	}

	/**
	 * 处理
	 */
	public void step() {
		String levelStr = jedis.hget(userInfoKey, "level");// 经验等级
		int level = Integer.parseInt(levelStr);
		// 获取符合等级的主线任务
		List<TaskInfo> taskList = TaskHelper.getMainTaskByLevel(level);
		for (TaskInfo taskInfo : taskList) {
			boolean bool = jedis.hexists(userMainTaskKey, taskInfo.getTaskid() + "");
			if (bool) {// 已接取
				continue;
			}
			// 是否达成接取条件
			boolean isReceive = isReceive(taskInfo);
			if (isReceive) {
				log.debug("用户[" + uuid + "]接取了一个主线任务[" + taskInfo.getTaskid() + "]");
				jedis.hsetnx(userMainTaskKey, taskInfo.getTaskid() + "", "0");
				SubscribeMsgPool.add(uuid, taskInfo.getType(), -1);
			} else {
				log.debug("用户[" + uuid + "]不符合条件接取主线任务[" + taskInfo.getTaskid() + "]");
			}
		}
	}

	/**
	 * 是否达成接取条件
	 * 
	 * @param taskInfo
	 * @return
	 */
	private boolean isReceive(TaskInfo taskInfo) {
		boolean isReceive = false;
		char logic = taskInfo.getLogic();
		List<Integer> depends = taskInfo.getDepend();
		if (logic == 'N') {// 没有依赖
			isReceive = true;
		} else if (logic == '|') {// 验证是否有其中一个完成了
			isReceive = isOnebool(depends);
		} else if (logic == '&') {// 验证是不是全部都完成了
			isReceive = isAllbool(depends);
		} else if (logic == 'Y') {// 只依赖一个任务
			if (isFinish(depends.get(0)))
				isReceive = true;
		}
		return isReceive;
	}

	/**
	 * 是否完成其中一个
	 * 
	 * @param isReceive
	 * @param depends
	 * @return
	 */
	private boolean isOnebool(List<Integer> depends) {
		boolean isReceive = false;
		for (int depend : depends) {
			if (isFinish(depend)) {
				isReceive = true;// 有一个完成，可以默认接取了
				break;
			}
		}
		return isReceive;
	}

	/**
	 * 是否全部完成
	 * 
	 * @param depends
	 * @return
	 */
	private boolean isAllbool(List<Integer> depends) {
		boolean allbool = true;// 是否全部完成
		for (int depend : depends) {
			if (!isFinish(depend)) {
				allbool = false;// 有任务未完成，不符合全部完成的条件
				break;
			}
		}
		return allbool;
	}

	/**
	 * 判定用户主线任务是否完成
	 */
	private boolean isFinish(int taskId) {
		String targetStr = jedis.hget(userMainTaskKey, taskId + "");
		if (Strings.isNullOrEmpty(targetStr)) // 还未接取
			return false;

		int target = Integer.parseInt(targetStr);
		if (target < -1) // 任务完成
			return true;
		else // 任务未完成
			return false;
	}
}