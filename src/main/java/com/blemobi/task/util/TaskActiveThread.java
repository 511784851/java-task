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
			try {
				// 队列中取出一个成员
				String uuid = queue.poll();
				if (Strings.isNullOrEmpty(uuid)) {
					RedisManager.returnResource(jedis);
					log.debug("没有用户任务后续处理");
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						log.debug("用户任务后续处理被唤醒了");
					}
					jedis = RedisManager.getRedis();
				} else {
					String userInfoKey = Constant.GAME_USER_INFO + uuid;
					String userMainTaskKey = Constant.GAME_TASK_MAIN + uuid;

					String levelStr = jedis.hget(userInfoKey, "level");// 经验等级
					int level = Integer.parseInt(levelStr);

					// 获取符合等级有依赖的主线任务
					List<TaskInfo> taskList = TaskHelper.getDependMainTaskByLevel(level);
					for (TaskInfo taskInfo : taskList) {
						boolean bool = jedis.hexists(userMainTaskKey, taskInfo.getTaskid() + "");
						if (!bool) {// 还未接取
							char logic = taskInfo.getLogic();
							List<Integer> depends = taskInfo.getDepend();
							if (logic == '|') {// 验证是否有其中一个完成了
								for (int depend : depends) {
									if (isFinish(depend, userMainTaskKey, jedis)) {
										// 有一个完成，可以默认接取了
										jedis.hsetnx(userMainTaskKey, taskInfo.getTaskid() + "", "0");
										SubscribeThread.addQueue(uuid, taskInfo.getType(), -1);
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
								if (allbool) {// 全部完成，可接取了
									jedis.hsetnx(userMainTaskKey, taskInfo.getTaskid() + "", "0");
									SubscribeThread.addQueue(uuid, taskInfo.getType(), -1);
								}
							} else {
								if (isFinish(depends.get(0), userMainTaskKey, jedis)) {
									// 有一个完成，可以默认接取了
									jedis.hsetnx(userMainTaskKey, taskInfo.getTaskid() + "", "0");
									SubscribeThread.addQueue(uuid, taskInfo.getType(), -1);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				log.error("消息订阅异常: " + e.getMessage());
			}
		}
	}

	private boolean isFinish(int taskId, String userMainTaskKey, Jedis jedis) {
		String targetStr = jedis.hget(userMainTaskKey, taskId + "");
		int target = Integer.parseInt(targetStr);
		if (target < -1) {
			return true;// 任务完成
		} else {
			return false;// 任务未完成
		}
	}
}