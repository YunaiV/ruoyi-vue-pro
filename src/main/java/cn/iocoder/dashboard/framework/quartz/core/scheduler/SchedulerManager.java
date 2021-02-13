package cn.iocoder.dashboard.framework.quartz.core.scheduler;

import cn.iocoder.dashboard.framework.quartz.core.enums.JobDataKeyEnum;
import cn.iocoder.dashboard.framework.quartz.core.handler.JobHandlerInvoker;
import org.quartz.*;

/**
 * {@link org.quartz.Scheduler} 的管理器，负责创建任务
 *
 * @author 芋道源码
 */
public class SchedulerManager {

    private final Scheduler scheduler;

    public SchedulerManager(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void addJob(Long jobId, String jobHandlerName, String jobHandlerParam, String cronExpression)
            throws SchedulerException {
        // 创建 JobDetail 对象
        JobDetail jobDetail = JobBuilder.newJob(JobHandlerInvoker.class)
                .usingJobData(JobDataKeyEnum.JOB_ID.name(), jobId)
                .usingJobData(JobDataKeyEnum.JOB_HANDLER_NAME.name(), jobHandlerName)
                .withIdentity(jobHandlerName).build();
        // 创建 Trigger 对象
        Trigger trigger = this.buildTrigger(jobHandlerName, jobHandlerParam, cronExpression);
        // 新增调度
        scheduler.scheduleJob(jobDetail, trigger);
    }

    public void updateJob(String jobHandlerName, String jobHandlerParam, String cronExpression)
            throws SchedulerException {
        // 创建新 Trigger 对象
        Trigger newTrigger = this.buildTrigger(jobHandlerName, jobHandlerParam, cronExpression);
        // 修改调度
        scheduler.rescheduleJob(new TriggerKey(jobHandlerName), newTrigger);
    }

    public void deleteJob(String jobHandlerName) throws SchedulerException {
        scheduler.deleteJob(new JobKey(jobHandlerName));
    }

    public void pauseJob(String jobHandlerName) throws SchedulerException {
        scheduler.pauseJob(new JobKey(jobHandlerName));
    }

    public void resumeJob(String jobHandlerName) throws SchedulerException {
        scheduler.resumeJob(new JobKey(jobHandlerName));
        scheduler.resumeTrigger(new TriggerKey(jobHandlerName));
    }

    public void triggerJob(Long jobId, String jobHandlerName, String jobHandlerParam)
            throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put(JobDataKeyEnum.JOB_ID.name(), jobId);
        data.put(JobDataKeyEnum.JOB_HANDLER_NAME.name(), jobHandlerName);
        data.put(JobDataKeyEnum.JOB_HANDLER_PARAM.name(), jobHandlerParam);
        // 触发任务
        scheduler.triggerJob(new JobKey(jobHandlerName), data);
    }

    private Trigger buildTrigger(String jobHandlerName, String jobHandlerParam, String cronExpression) {
        return TriggerBuilder.newTrigger()
                .withIdentity(jobHandlerName)
                .usingJobData(JobDataKeyEnum.JOB_HANDLER_PARAM.name(), jobHandlerParam)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
    }

}
