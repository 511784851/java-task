package com.blemobi.task.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.blemobi.library.cache.UserBaseCache;
import com.blemobi.library.redis.dao.RedisDao;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.AccountProtos.PUserBase;
import com.blemobi.sep.probuf.ResultProtos.PInt32List;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.ResultProtos.PResult;
import com.blemobi.sep.probuf.ResultProtos.PStringList;
import com.blemobi.sep.probuf.TaskApiProtos.PGoldExchg;
import com.blemobi.sep.probuf.TaskProtos.PGoldDetail;
import com.blemobi.sep.probuf.TaskProtos.PGoldDetails;
import com.blemobi.sep.probuf.TaskProtos.PGoldTask;
import com.blemobi.sep.probuf.TaskProtos.PTaskInfo;
import com.blemobi.task.basic.GoldInfo;
import com.blemobi.task.basic.TaskData;
import com.blemobi.task.basic.TaskInfo;
import com.blemobi.task.util.Global;
import com.google.common.base.Strings;

/**
 * 任务相关处理
 * 
 * @author zhaoyong
 *
 */
public class TaskService {
	private String uuid;
	private String goldKey;
	private String novicTK;
	private String dailyTK;
	private String detailGK;
	private String language;

	/**
	 * 构造方法
	 */
	public TaskService(String uuid) {
		this.uuid = uuid;
		this.goldKey = Global.GOLD_KEY + uuid;
		this.novicTK = Global.DAILY_KEY + uuid;
		this.dailyTK = novicTK + ":" + TaskData.getDailyDate();
		this.detailGK = Global.DETAIL_KEY + uuid;
	}

	public TaskService() {

	}

	/**
	 * 获取金币和任务列表
	 * 
	 * @return
	 * @throws IOException
	 */
	public PMessage list() throws IOException {
		int gold = UserGoldUtil.getUserGold(goldKey);
		PUserBase user = UserBaseCache.get(uuid);

		List<PTaskInfo> myNovicTask = getMyTask(novicTK, TaskData.getForNovic());
		List<PTaskInfo> myDailyTask = getMyTask(dailyTK, TaskData.getForDaily());

		PGoldTask goldTask = PGoldTask.newBuilder().setGold(gold).setUser(user).addAllNovicTask(myNovicTask)
				.addAllDailyTask(myDailyTask).build();

		return ReslutUtil.createReslutMessage(goldTask);
	}

	/**
	 * 获取任务列表
	 * 
	 * @param key
	 * @param tasks
	 * @return
	 */
	private List<PTaskInfo> getMyTask(String key, List<TaskInfo> tasks) {
		Map<String, String> myTask = findMyTask(key);
		return tasks.stream().map(t -> makeMyTask(myTask, t)).filter(t -> t != null)
				.sorted((t1, t2) -> t2.getState() - t1.getState()).collect(Collectors.toList());
	}

	/**
	 * 生成单条任务信息
	 * 
	 * @param count
	 *            已完成次数
	 * @param taskInfo
	 * @return
	 */
	private PTaskInfo makeMyTask(Map<String, String> myTask, TaskInfo t) {
		short ID = t.getID();// 任务ID
		String counts = myTask.get(ID + "");
		byte count = Strings.isNullOrEmpty(counts) ? 0 : Byte.parseByte(counts);// 已完成次数
		byte loop = (byte) (t.getTarg() * t.getLoop());// 要求次数
		int state = count < 0 ? -1 : count < loop ? 0 : t.isNocivTask() ? 1 : -1;// 任务状态（0-进行中，1-可领奖，-1-已完成）
		if (count > t.getLoop())
			count = t.getLoop();
		return t.isNocivTask() && state == -1 ? null
				: PTaskInfo.newBuilder().setID(ID).setGold(t.getGold()).setState(state).setCount(count)
						.setLoop(t.getLoop()).setDesc(t.getDesc(language)).build();
	}

	/**
	 * 查询我的任务信息
	 * 
	 * @param key
	 * @return
	 */
	private Map<String, String> findMyTask(String k) {
		return new RedisDao<Map<String, String>>().exec(r -> r.hgetAll(k));
	}

	/**
	 * 领奖励
	 */
	public PMessage receive(short ID) {
		boolean bool = new RedisDao<Boolean>().execLock(Global.LOCK_KEY + ":rece:" + uuid, () -> {
			byte count = UserGoldUtil.getTaskCount(novicTK, ID);// 已完成次数
			TaskInfo taskInfo = TaskData.get(ID);// 任务信息
			byte trag = taskInfo.getTarg();// 可完成次数
			if (count >= trag) {
				UserGoldUtil ugu = new UserGoldUtil(uuid, ID + "", (byte) 1, taskInfo.getGold(), taskInfo.getDesc_sc());
				if (!ugu.incrAndDetail())
					throw new RuntimeException("金币流水操作异常");
				new RedisDao<>().exec(r -> r.hset(novicTK, ID + "", Byte.MIN_VALUE + ""));
			}
			return false;
		});
		return bool ? ReslutUtil.createSucceedMessage() : ReslutUtil.createSucceedMessage();
	}

	/**
	 * 分页获取金币明细
	 * 
	 * @param sx
	 * @param ex
	 * @param st
	 * @param et
	 * @param type
	 * @return
	 */
	public PMessage details(long idx, int size, long st, long et, byte type) {
		List<String> set = new RedisDao<List<String>>().exec(r -> r.lrange(detailGK, 0, -1));
		Collections.reverse(set);

		List<PGoldDetail> list = set.stream().map(d -> makeDetail(d)).filter(g -> idx == 0 ? true : g.getTime() < idx)
				.filter(g -> type == 0 ? true : g.getType() == type)
				.filter(g -> st == 0 ? true : g.getTime() >= st && et == 0 ? true : g.getTime() <= et).limit(size)
				.collect(Collectors.toList());

		int gold = idx == 0 ? UserGoldUtil.getUserGold(goldKey) : 0;

		PGoldDetails details = PGoldDetails.newBuilder().setGold(gold).addAllDetail(list).build();
		return ReslutUtil.createReslutMessage(details);
	}

	/**
	 * 生成单条金币明细信息
	 * 
	 * @param value
	 * @return
	 */
	private PGoldDetail makeDetail(String s) {
		GoldInfo g = GoldInfo.parse(s);
		String desc = g.getDesc() == null ? "" : g.getDesc();
		return PGoldDetail.newBuilder().setTime(g.getTime()).setGold(g.getGold()).setType(g.getType()).setDesc(desc)
				.build();
	}

	/**
	 * 批量查询用户金币数量
	 * 
	 * @param list
	 * @return
	 */
	public PInt32List findGold(PStringList list) {
		List<Integer> golds = list.getListList().stream().map(u -> UserGoldUtil.getUserGold(Global.GOLD_KEY + u))
				.collect(Collectors.toList());
		return PInt32List.newBuilder().addAllList(golds).build();
	}

	/**
	 * 监管操作金币
	 * 
	 * @param uuid
	 *            用户uuid
	 * @param gold
	 *            金币数量
	 * @param type
	 *            0-减少，1-增加
	 * @param content
	 *            描述
	 * @return
	 */
	public PResult operation(String uuid, short gold, byte type, String content) {
		gold = (short) (type == 1 ? gold : -gold);
		UserGoldUtil ugu = new UserGoldUtil(uuid, "", (byte) 4, gold, content);
		boolean bool = ugu.incrAndDetail();
		return bool ? ReslutUtil.createPResultMessage(0, "ok") : ReslutUtil.createPResultMessage(-1, "金币不足");
	}

	/**
	 * 金币兑换/消费
	 * 
	 * @param ge
	 * @return
	 */
	public PResult exchg(PGoldExchg ge) {
		UserGoldUtil ugu = new UserGoldUtil(ge.getUuid(), ge.getOrderNo(), (byte) 3, (short) -ge.getGold(),
				"商城订单：" + ge.getOrderNo());
		boolean bool = ugu.incrAndDetail();
		return bool ? ReslutUtil.createPResultMessage(0, "ok") : ReslutUtil.createPResultMessage(-1, "金币不足");
	}
}