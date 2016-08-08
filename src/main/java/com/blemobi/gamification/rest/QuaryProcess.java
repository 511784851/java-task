package com.blemobi.gamification.rest;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.blemobi.gamification.init.BadgeManager;
import com.blemobi.gamification.init.TaskManager;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.GamificationProtos.PAchievement;
import com.blemobi.sep.probuf.GamificationProtos.PBadgeDetail;
import com.blemobi.sep.probuf.GamificationProtos.PGamification;
import com.blemobi.sep.probuf.GamificationProtos.PTaskDetail;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;

@Path("/user")
public class QuaryProcess {

	/**
	 * 任务通知
	 * 
	 * @param type
	 *            任务ID
	 * @return PMessage 返回PMessage对象数据
	 */
	@GET
	@Path("task")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage detail(@CookieParam("uuid") String uuid, @CookieParam("token") String token) {
		PGamification.Builder gamificationBuilder = PGamification.newBuilder()
				.setExperience(50)
				.setLevel(1);

		Collection<PTaskDetail> taskKeyCollection = TaskManager.getTaskList();
		Iterator<PTaskDetail> it = taskKeyCollection.iterator();
		while (it.hasNext()) {			
			PTaskDetail taskDetail = it.next();
			
			int progress = 1;
			int statu = progress >= taskDetail.getTarget()?3:2;
					
			taskDetail = taskDetail.toBuilder()
					.setProgress(progress)
					.setStatu(statu)
					.build();
			gamificationBuilder.addTask(taskDetail);
		}

		return ReslutUtil.createReslutMessage(gamificationBuilder.build());
	}
	
	@GET
	@Path("achievement")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage achievement(@CookieParam("uuid") String uuid, @CookieParam("token") String token) {
		PAchievement.Builder achievementBuilder = PAchievement.newBuilder()
				.setExperience(50)
				.setLevel(1)
				.setNextLevelExperience(500)
				.setUpgradeExperience(125)
				.setBadgeHave(2)
				.setBadgeSum(10);

		List<PBadgeDetail> badgeList = BadgeManager.getBadgeList();
		for(PBadgeDetail badgeDetail : badgeList) {	
			badgeDetail = badgeDetail.toBuilder()
					.setStatu(2)
					.setProgress("75%")
					.setTime(0)
					.build();
			achievementBuilder.addBadge(badgeDetail);
		}

		return ReslutUtil.createReslutMessage(achievementBuilder.build());
	}
}