package com.blemobi.task.grpcservice;

import com.blemobi.library.grpc_v1.GRPCExpTools;
import com.blemobi.library.grpc_v1.annotation.GRPCService;
import com.blemobi.sep.grpc.TaskServiceGrpc;
import com.blemobi.sep.probuf.ResultProtos.PInt32List;
import com.blemobi.sep.probuf.ResultProtos.PInt64List;
import com.blemobi.sep.probuf.ResultProtos.PResult;
import com.blemobi.sep.probuf.ResultProtos.PStringList;
import com.blemobi.sep.probuf.TaskApiProtos.PGoldExchg;
import com.blemobi.sep.probuf.TaskApiProtos.PTaskMsgs;
import com.blemobi.task.service.NotifyService;
import com.blemobi.task.service.TaskService;
import com.blemobi.task.service.UserTask;

import io.grpc.stub.StreamObserver;
import lombok.extern.log4j.Log4j;

/**
 * GRPC服务
 * 
 * @author zhaoyong
 *
 */
@Log4j
@GRPCService
public class TaskGRPCImpl extends TaskServiceGrpc.TaskServiceImplBase {

	/**
	 * 任务消息回调
	 */
	@Override
	public void notify(PTaskMsgs request, StreamObserver<PInt64List> responseObserver) {
		log.debug("有任务消息回调 -> " + request);
		PInt64List list = null;
		try {
			NotifyService notifyService = new NotifyService(request, true);
			list = notifyService.exec();
		} catch (Exception ex) {
			GRPCExpTools.doException(responseObserver, ex);
		} finally {
			log.debug("任务消息回调处理结果 -> " + list);
		}
		responseObserver.onNext(list);
		responseObserver.onCompleted();
	}

	/**
	 * 批量查询用户金币数量
	 */
	@Override
	public void getUserGold(PStringList request, StreamObserver<PInt32List> responseObserver) {
		PInt32List list = null;
		try {
			TaskService taskService = new TaskService();
			list = taskService.findGold(request);
		} catch (Exception ex) {
			GRPCExpTools.doException(responseObserver, ex);
		}
		responseObserver.onNext(list);
		responseObserver.onCompleted();
	}

	/**
	 * 金币兑换/消费
	 */
	@Override
	public void exchg(PGoldExchg request, StreamObserver<PResult> responseObserver) {
		PResult result = null;
		try {
			TaskService taskService = new TaskService();
			result = taskService.exchg(request);
		} catch (Exception ex) {
			GRPCExpTools.doException(responseObserver, ex);
		}
		responseObserver.onNext(result);
		responseObserver.onCompleted();
	}
}
