package com.blemobi.gamification.rest;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.GamificationProtos.PTaskKey;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;

@Path("/task")
public class TaskProcess {
	private String GAME_USER_TASK = "chat:game:task:";
	
	
	/**
	 * 任务通知
	 * 
	 * @param taskKey
	 *            任务ID
	 * @return PMessage 返回PMessage对象数据
	 */
	@POST
	@Path("serNotify")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage serNotify(@FormParam("uuid") String uuid, @FormParam("taskKey") String taskKey) {

		if (PTaskKey.PUBLISH.toString().equals(taskKey)) {// 发帖

		} else if (PTaskKey.FOLLOW.toString().equals(taskKey)) {// 关注用户

		} else if (PTaskKey.VOTE.toString().equals(taskKey)) {// 点赞帖子

		} else if (PTaskKey.REPLY.toString().equals(taskKey)) { // 回复帖子

		} else if (PTaskKey.ADDCOMMUNITY.toString().equals(taskKey)) {// 加入社区

		} else if (PTaskKey.PUBLISHCOMMUNITY.toString().equals(taskKey)) {// 论坛发帖

		} else if (PTaskKey.ADDFRIEND.toString().equals(taskKey)) { // 加平台好友

		} else if (PTaskKey.FORWARD.toString().equals(taskKey)) { // 转发帖子

		} else if (PTaskKey.REDPACKET.toString().equals(taskKey)) {// 发红包

		} else if (PTaskKey.REMIND.toString().equals(taskKey)) { // @人

		} else if (PTaskKey.PROFILE.toString().equals(taskKey)) { // 资料完善

		} else if (PTaskKey.ADDCONTACT.toString().equals(taskKey)) {// 加手机通讯录好友

		} else if (PTaskKey.FEEDBACK.toString().equals(taskKey)) { // 意见反馈

		} else if (PTaskKey.BINDACCOUNT.toString().equals(taskKey)) {// 绑定第三方账号

		} else if (PTaskKey.ADDOTHERFRIEND.toString().equals(taskKey)) {// 加第三方方平台好友

		} else if (PTaskKey.REGISTER.toString().equals(taskKey)) {// 注册

		} else {
			return ReslutUtil.createErrorMessage(1901012, "taskKey error");
		}

		return ReslutUtil.createErrorMessage(0, "ok");
	}
	
	@POST
	@Path("notify")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage notify(@CookieParam("uuid") String uuid, @FormParam("taskKey") String taskKey) {

		if (PTaskKey.OPENCONTACT.toString().equals(taskKey)) { // 开启手机通讯录访问权限

		} else if (PTaskKey.DOWNLOADAPP.toString().equals(taskKey)) {// 下载机器人助手

		} else {
			return ReslutUtil.createErrorMessage(1901012, "taskKey error");
		}

		return ReslutUtil.createErrorMessage(0, "ok");
	}
	
	/**
	 * 任务通知
	 * 
	 * @param type
	 *            任务ID
	 * @return PMessage 返回PMessage对象数据
	 */
	@POST
	@Path("accept")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage accept(@CookieParam("uuid") String uuid, @CookieParam("token") String token,
			@QueryParam("taskKey") String taskKey) {

		if (PTaskKey.PUBLISH.toString().equals(taskKey)) {// 发帖

		} else if (PTaskKey.FOLLOW.toString().equals(taskKey)) {// 关注用户

		} else if (PTaskKey.VOTE.toString().equals(taskKey)) {// 点赞帖子

		} else if (PTaskKey.REPLY.toString().equals(taskKey)) { // 回复帖子

		} else if (PTaskKey.ADDCOMMUNITY.toString().equals(taskKey)) {// 加入社区

		} else if (PTaskKey.PUBLISHCOMMUNITY.toString().equals(taskKey)) {// 论坛发帖

		} else if (PTaskKey.ADDFRIEND.toString().equals(taskKey)) { // 加平台好友

		} else if (PTaskKey.FORWARD.toString().equals(taskKey)) { // 转发帖子

		} else if (PTaskKey.REDPACKET.toString().equals(taskKey)) {// 发红包

		} else if (PTaskKey.REMIND.toString().equals(taskKey)) { // @人

		} else if (PTaskKey.PROFILE.toString().equals(taskKey)) { // 资料完善

		} else if (PTaskKey.OPENCONTACT.toString().equals(taskKey)) { // 开启手机通讯录访问权限

		} else if (PTaskKey.ADDCONTACT.toString().equals(taskKey)) {// 加手机通讯录好友

		} else if (PTaskKey.FEEDBACK.toString().equals(taskKey)) { // 意见反馈

		} else if (PTaskKey.BINDACCOUNT.toString().equals(taskKey)) {// 绑定第三方账号

		} else if (PTaskKey.DOWNLOADAPP.toString().equals(taskKey)) {// 下载机器人助手

		} else if (PTaskKey.ADDOTHERFRIEND.toString().equals(taskKey)) {// 加第三方方平台好友

		} else if (PTaskKey.REGISTER.toString().equals(taskKey)) {// 注册

		} else {
			return ReslutUtil.createErrorMessage(1901012, "taskKey error");
		}

		return ReslutUtil.createErrorMessage(0, "ok");
	}
}