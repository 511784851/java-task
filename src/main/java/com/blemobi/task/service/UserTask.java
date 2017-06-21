package com.blemobi.task.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.blemobi.library.grpc.AccountGrpcClient;
import com.blemobi.library.grpc.CommentsGrpcClient;
import com.blemobi.library.grpc.CommunityGrpcClient;
import com.blemobi.library.grpc.LoginGrpcClient;
import com.blemobi.library.grpc.NetDiskGrpcClient;
import com.blemobi.library.grpc.NewsGrpcClient;
import com.blemobi.library.redis.dao.RedisDao;
import com.blemobi.sep.probuf.ResultProtos.PInt32List;
import com.blemobi.sep.probuf.TaskApiProtos.PTaskMsg;
import com.blemobi.sep.probuf.TaskApiProtos.PTaskMsgs;
import com.blemobi.task.basic.MsgInfo;
import com.blemobi.task.basic.TaskData;
import com.blemobi.task.util.Global;

import lombok.extern.log4j.Log4j;

/**
 * 初始化用户任务信息
 * 
 * @author zhaoyong
 *
 */
@Log4j
public class UserTask {

	private String uuid;

	public UserTask(String uuid) {
		this.uuid = uuid;
	}

	public void init() {
		// 是否已初始化
		if (new RedisDao<Boolean>().exec(r -> r.exists(Global.CHECK_KEY + uuid)))
			return;

		// 各个服务->消息内容
		Map<String, List<MsgInfo>> msg_group = TaskData.getMsgGroup();
		msg_group.keySet().stream().forEach(s -> {
			try {
				// 消息内容
				PTaskMsgs taskMsgs = makeTaskMsgs(s, msg_group.get(s));
				// 分发消息
				PInt32List cl = checkMsgIds(s, taskMsgs);
				// 按顺序重组
				taskMsgs = makeNewMsg(taskMsgs, cl);
				log.debug("查询用户[" + uuid + "][" + s + "]的任务消息结果 -> " + taskMsgs);
				// 处理任务进度
				NotifyService notifyService = new NotifyService(taskMsgs, false);
				notifyService.exec();
				// 标识用于已初始化
				new RedisDao<>().exec(r -> r.setnx(Global.CHECK_KEY + uuid, ""));
			} catch (Exception e) {
				log.error("查询用户[" + uuid + "][" + s + "]的任务消息状态异常： " + e.getMessage());
				e.printStackTrace();
			}
		});
	}

	/**
	 * 构建要查询的消息内容
	 * 
	 * @param s
	 *            服务名称
	 * @param l
	 *            服务下消息内容
	 * @return PTaskMsgs
	 */
	private PTaskMsgs makeTaskMsgs(String s, List<MsgInfo> l) {
		List<PTaskMsg> list = l.stream()
				.map(m -> PTaskMsg.newBuilder().setUuid(uuid).setMsgID(m.getMsgID()).setCount(50).build())
				.collect(Collectors.toList());
		return PTaskMsgs.newBuilder().addAllTaskMsg(list).build();
	}

	/**
	 * 将返回结果按顺序重组
	 * 
	 * @param taskMsgs
	 *            消息内容
	 * @param cl
	 *            消息内容对应的增量
	 */
	private PTaskMsgs makeNewMsg(PTaskMsgs taskMsgs, PInt32List cl) {
		List<PTaskMsg> list = new ArrayList<PTaskMsg>();
		int i = 0;
		for (PTaskMsg m : taskMsgs.getTaskMsgList()) {
			PTaskMsg tm = m.toBuilder().setCount(cl.getList(i++)).build();
			list.add(tm);
		}
		return PTaskMsgs.newBuilder().addAllTaskMsg(list).build();
	}

	/**
	 * 分发消息
	 * 
	 * @param server
	 *            服务名称
	 * @param taskMsgs
	 *            消息内容
	 * @return
	 */
	private PInt32List checkMsgIds(String server, PTaskMsgs taskMsgs) {
		if ("login".equals(server)) {
			LoginGrpcClient client = new LoginGrpcClient();
			return client.checkMsgIds(taskMsgs);
		} else if ("account".equals(server)) {
			AccountGrpcClient client = new AccountGrpcClient();
			return client.checkMsgIds(taskMsgs);
		} else if ("community".equals(server)) {
			CommunityGrpcClient client = new CommunityGrpcClient();
			return client.checkMsgIds(taskMsgs);
		} else if ("netdisk".equals(server)) {
			NetDiskGrpcClient client = new NetDiskGrpcClient();
			return client.checkMsgIds(taskMsgs);
		} else if ("news".equals(server)) {
			NewsGrpcClient client = new NewsGrpcClient();
			return client.checkMsgIds(taskMsgs);
		} else if ("comment".equals(server)) {
			CommentsGrpcClient client = new CommentsGrpcClient();
			return client.checkMsgIds(taskMsgs);
		} else
			throw new RuntimeException("没有找到对应的服务名称：" + server);
	}

}