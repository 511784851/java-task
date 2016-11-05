package com.blemobi.task.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.blemobi.library.redis.RedisManager;
import com.blemobi.task.basic.TaskHelper;
import com.blemobi.task.basic.TaskInfo;
import com.google.common.base.Strings;

import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;

/*
 * 经验等级提升或主线任务完成后续处理
 */
@Log4j
public class TaskActiveThread extends Thread {
	private static Queue<String> queue = new LinkedList<String>();

	private TaskActiveThread() {

	}

	public static void addQueue(String uuid) {
		queue.add(uuid);
		SubscribeThread.interrupted();
	}

	static {
		new TaskActiveThread().start();
	}

	public void run() {
		Jedis jedis = RedisManager.getRedis();
		while (true) {
			// 队列中取出一个成员
			String uuid = queue.poll();
			if (Strings.isNullOrEmpty(uuid)) {
				log.debug("没有任务触发");
				try {
					Thread.sleep(1000);
				} catch (Exception e) {

				}
			} else {
				try {
					String userInfoKey = Constant.GAME_USER_INFO + uuid;
					String userMainTaskKey = Constant.GAME_TASK_MAIN + uuid;

					String levelStr = jedis.hget(userInfoKey, "level");// 经验等级
					int level = Integer.parseInt(levelStr);
					String logStr = "uuid=[" + uuid + "],level=[" + level + "]";
					log.debug("有任务触发 -： " + logStr);
					// 获取符合等级有依赖的主线任务
					List<TaskInfo> taskList = TaskHelper.getMainTaskByLevel(level);
					for (TaskInfo taskInfo : taskList) {
						boolean bool = jedis.hexists(userMainTaskKey, taskInfo.getTaskid() + "");
						if (!bool) {// 还未接取
							boolean isReceive = false;// 是否达成接取条件
							char logic = taskInfo.getLogic();
							List<Integer> depends = taskInfo.getDepend();
							if (logic == 'N') {// 没有依赖
								isReceive = true;
							} else if (logic == '|') {// 验证是否有其中一个完成了
								for (int depend : depends) {
									if (isFinish(depend, userMainTaskKey, jedis)) {
										isReceive = true;// 有一个完成，可以默认接取了
										break;
									}
								}
							} else if (logic == '&') {// 验证是不是全部都完成了
								boolean allbool = true;// 是否全部完成
								for (int depend : depends) {
									if (!isFinish(depend, userMainTaskKey, jedis)) {
										allbool = false;// 有任务未完成，不符合全部完成的条件
										break;
									}
								}
								isReceive = allbool;
							} else if (logic == 'Y') {// 只依赖一个任务
								if (isFinish(depends.get(0), userMainTaskKey, jedis)) {
									isReceive = true;
								}
							}
							if (isReceive) {
								log.debug("任务触发>>接取了一个主线任务[" + taskInfo.getTaskid() + "] -> " + logStr);
								jedis.hsetnx(userMainTaskKey, taskInfo.getTaskid() + "", "0");
								SubscribeThread.addQueue(uuid, taskInfo.getType(), -1);
							} else {
								log.debug("任务触发>>不符合条件接取主线任务[" + taskInfo.getTaskid() + "] -> " + logStr);
							}
						}
					}
				} catch (Exception e) {
					log.error("任务触发异常: " + e.getMessage());
					e.printStackTrace();
				}
			}

		}
	}

	/*
	 * 判定用户主线任务是否完成
	 */
	private boolean isFinish(int taskId, String userMainTaskKey, Jedis jedis) {
		String targetStr = jedis.hget(userMainTaskKey, taskId + "");
		if (Strings.isNullOrEmpty(targetStr)) {// 还未接取
			return false;
		}

		int target = Integer.parseInt(targetStr);
		if (target < -1) {// 任务完成
			return true;
		} else {// 任务未完成
			return false;
		}
	}
}