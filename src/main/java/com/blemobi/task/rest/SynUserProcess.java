package com.blemobi.task.rest;

import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.blemobi.library.cache.UserBaseCache;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.AccountProtos.PUserBaseList;
import com.blemobi.sep.probuf.ResultProtos.PBinaryMsg;
import com.blemobi.sep.probuf.ResultProtos.PBinaryMsgList;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;

@Path("/v1/task/inside")
public class SynUserProcess {
	/**
	 * 用户信息变更同步
	 * 
	 * @param binaryMsgList
	 *            用户数据
	 * @return
	 * @throws InvalidProtocolBufferException
	 */
	@POST
	@Path("msgpush/consumer")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage consumer(PBinaryMsgList binaryMsgList) throws InvalidProtocolBufferException {
		List<PBinaryMsg> list = binaryMsgList.getListList();
		for (PBinaryMsg binaryMsg : list) {
			if (binaryMsg.getMsgType() == 2) {
				PUserBaseList userBaseListArray = PUserBaseList.parseFrom(binaryMsg.getMsgData());
				userBaseListArray.getListList().forEach(user -> UserBaseCache.put(user.getUUID(), user));
			}
		}
		return ReslutUtil.createSucceedMessage();
	}
}