package com.blemobi.task.bat;

import com.blemobi.tools.DateUtils;
import lombok.extern.log4j.Log4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;

/**
 * Description:
 * User: HUNTER.POON
 * Date: 2017/6/13 09:46
 */
@Log4j
public class QuartzManager {
    private final static String JOB_GROUP_NAME = "QUARTZ_JOBGROUP_NAME";//任务组
    private final static String TRIGGER_GROUP_NAME = "QUARTZ_TRIGGERGROUP_NAME";//触发器组

    /**
     * 添加任务的方法
     *
     * @param jobName     任务名
     * @param triggerName 触发器名
     * @param jobClass    执行任务的类
     * @param seconds     间隔时间
     * @throws SchedulerException
     */
    public static void addJob(String jobName, String triggerName, Class<? extends Job> jobClass, int seconds) throws SchedulerException {
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sche = sf.getScheduler();
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, JOB_GROUP_NAME).build();
        Trigger trigger = TriggerBuilder.newTrigger()//创建一个新的TriggerBuilder来规范一个触发器
                .withIdentity(triggerName, TRIGGER_GROUP_NAME)//给触发器起一个名字和组名
                .startNow()//立即执行
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInSeconds(seconds)//时间间隔  单位：秒
                                .repeatForever()//一直执行
                )
                .build();//产生触发器
        //向Scheduler中添加job任务和trigger触发器
        sche.scheduleJob(jobDetail, trigger);
        //启动
        sche.start();
    }


    /**
     * 添加任务的方法
     *
     * @param jobName     任务名
     * @param triggerName 触发器名
     * @param jobClass    执行任务的类
     * @param beginDate   触发时间
     * @param unit 间隔单位 H、M、S
     * @param interval 间隔
     * @throws SchedulerException
     */
    public static void addJob(String jobName, String triggerName, Class<? extends Job> jobClass, Date beginDate, char
            unit, int interval) throws SchedulerException {
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sche = sf.getScheduler();
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, JOB_GROUP_NAME).build();
        ScheduleBuilder<?> schedBuilder = null;
        switch (unit) {
            case 'H':
                schedBuilder = SimpleScheduleBuilder.repeatHourlyForever(interval);
                break;
            case 'M':
                schedBuilder = SimpleScheduleBuilder.repeatMinutelyForever(interval);
                break;
            case 'S':
                schedBuilder = SimpleScheduleBuilder.repeatSecondlyForever(interval);
                break;
            default:
                schedBuilder = SimpleScheduleBuilder.repeatSecondlyForever(interval);
                break;
        }
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerName, TRIGGER_GROUP_NAME).startAt(beginDate).withSchedule(schedBuilder).build();
        sche.scheduleJob(jobDetail, trigger);
        sche.start();
        log.debug(String.format("job->%s,trigger->%s,jobClass->%s,begin->%s,unit->%s,interval->%d", jobName,
                triggerName, jobClass.getSimpleName(), DateUtils.getDate19(beginDate), unit, interval));
    }
}
