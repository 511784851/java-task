package com.blemobi.task.rest;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.blemobi.sep.probuf.ResultProtos.PInt32List;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.ResultProtos.PResult;
import com.blemobi.sep.probuf.ResultProtos.PStringList;
import com.blemobi.sep.probuf.TaskProtos.PGoldDetails;
import com.blemobi.task.service.TaskService;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;

/**
 * 监管接口
 * 
 * @author zhaoyong
 *
 */
@Path("/v1/task/user")
public class UserProcess {
	/**
	 * 金币明细
	 * 
	 * @param uuid
	 * @param idx
	 * @param size
	 * @param st
	 * @param et
	 * @param type
	 * @return
	 * @throws InvalidProtocolBufferException
	 */
	@GET
	@Path("details")
	@Produces(MediaTypeExt.APPLICATION_JSON)
	public PGoldDetails details(@QueryParam("uuid") String uuid, @QueryParam("idx") long idx,
			@QueryParam("size") int size, @QueryParam("st") long st, @QueryParam("et") long et,
			@QueryParam("type") byte type) throws InvalidProtocolBufferException {
		TaskService taskService = new TaskService(uuid);
		PMessage message = taskService.details(idx, size, st, et, type);
		return PGoldDetails.parseFrom(message.getData());
	}

	/**
	 * 金币增加或减少（监管）
	 * 
	 * @param uuid
	 * @param gold
	 * @param type
	 *            类型（0-减少,1-增加）
	 * @param content
	 * @return
	 */
	@POST
	@Path("operation")
	@Produces(MediaTypeExt.APPLICATION_JSON)
	public PResult operation(@FormParam("uuid") String uuid, @FormParam("gold") short gold,
			@FormParam("type") byte type, @FormParam("content") String content) {
		TaskService taskService = new TaskService(uuid);
		return taskService.operation(uuid, gold, type, content);
	}

	/**
	 * 查询用户金币数量
	 * 
	 * @param uuids
	 * @return
	 */
	@GET
	@Path("gold")
	@Produces(MediaTypeExt.APPLICATION_JSON)
	public PInt32List gold(@QueryParam("uuids") String uuids) {
		String[] array = uuids.split(",");

		PStringList.Builder builder = PStringList.newBuilder();
		for (String uuid : array) {
			builder.addList(uuid);
		}
		TaskService taskService = new TaskService();
		return taskService.findGold(builder.build());
	}
}