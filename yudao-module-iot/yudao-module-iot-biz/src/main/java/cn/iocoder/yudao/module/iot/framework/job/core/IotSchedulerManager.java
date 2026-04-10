package cn.iocoder.yudao.module.iot.framework.job.core;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * IoT 模块的 Scheduler 管理类，基于 Quartz 实现
 *
 * 疑问：为什么 IoT 模块不复用全局的 SchedulerManager 呢？
 * 回复：yudao-cloud 项目，使用的是 XXL-Job 作为调度中心，无法动态添加任务。
 *
 * @author 芋道源码
 */
@Slf4j
public class IotSchedulerManager {

    private static final String SCHEDULER_NAME = "iotScheduler";

    private final SchedulerFactoryBean schedulerFactoryBean;

    private Scheduler scheduler;

    public IotSchedulerManager(DataSource dataSource,
                               ApplicationContext applicationContext) {
        // 1. 参考 SchedulerFactoryBean 类
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        schedulerFactoryBean.setJobFactory(jobFactory);
        schedulerFactoryBean.setAutoStartup(true);
        schedulerFactoryBean.setSchedulerName(SCHEDULER_NAME);
        schedulerFactoryBean.setDataSource(dataSource);
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);
        Properties properties = new Properties();
        schedulerFactoryBean.setQuartzProperties(properties);
        // 2. 参考 application-local.yaml 配置文件
        // 2.1 Scheduler 相关配置
        properties.put("org.quartz.scheduler.instanceName", SCHEDULER_NAME);
        properties.put("org.quartz.scheduler.instanceId", "AUTO");
        // 2.2 JobStore 相关配置
        properties.put("org.quartz.jobStore.class", "org.springframework.scheduling.quartz.LocalDataSourceJobStore");
        properties.put("org.quartz.jobStore.isClustered", "true");
        properties.put("org.quartz.jobStore.clusterCheckinInterval", "15000");
        properties.put("org.quartz.jobStore.misfireThreshold", "60000");
        // 2.3 线程池相关配置
        properties.put("org.quartz.threadPool.threadCount", "25");
        properties.put("org.quartz.threadPool.threadPriority", "5");
        properties.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        this.schedulerFactoryBean = schedulerFactoryBean;
    }

    public void start() throws Exception {
        log.info("[start][Scheduler 初始化开始]");
        // 初始化
        schedulerFactoryBean.afterPropertiesSet();
        schedulerFactoryBean.start();
        // 获得 Scheduler 对象
        this.scheduler = schedulerFactoryBean.getScheduler();
        log.info("[start][Scheduler 初始化完成]");
    }

    public void stop() {
        log.info("[stop][Scheduler 关闭开始]");
        schedulerFactoryBean.stop();
        this.scheduler = null;
        log.info("[stop][Scheduler 关闭完成]");
    }

    // ========== 参考 SchedulerManager 实现 ==========

    /**
     * 添加或更新 Job 到 Quartz 中
     *
     * @param jobClass 任务处理器的类
     * @param jobName 任务名
     * @param cronExpression CRON 表达式
     * @param jobDataMap 任务数据
     * @throws SchedulerException 添加异常
     */
    public void addOrUpdateJob(Class <? extends Job> jobClass, String jobName,
                               String cronExpression, Map<String, Object> jobDataMap)
            throws SchedulerException {
        if (scheduler.checkExists(new JobKey(jobName))) {
            this.updateJob(jobName, cronExpression);
        } else {
            this.addJob(jobClass, jobName, cronExpression, jobDataMap);
        }
    }

    /**
     * 添加 Job 到 Quartz 中
     *
     * @param jobClass 任务处理器的类
     * @param jobName 任务名
     * @param cronExpression CRON 表达式
     * @param jobDataMap 任务数据
     * @throws SchedulerException 添加异常
     */
    public void addJob(Class <? extends Job> jobClass, String jobName,
                       String cronExpression, Map<String, Object> jobDataMap)
            throws SchedulerException {
        // 创建 JobDetail 对象
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .usingJobData(new JobDataMap(jobDataMap))
                .withIdentity(jobName).build();
        // 创建 Trigger 对象
        Trigger trigger = this.buildTrigger(jobName, cronExpression);
        // 新增 Job 调度
        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * 更新 Job 到 Quartz
     *
     * @param jobName 任务名
     * @param cronExpression CRON 表达式
     * @throws SchedulerException 更新异常
     */
    public void updateJob(String jobName, String cronExpression)
            throws SchedulerException {
        // 创建新 Trigger 对象
        Trigger newTrigger = this.buildTrigger(jobName, cronExpression);
        // 修改调度
        scheduler.rescheduleJob(new TriggerKey(jobName), newTrigger);
    }

    /**
     * 删除 Quartz 中的 Job
     *
     * @param jobName 任务名
     * @throws SchedulerException 删除异常
     */
    public void deleteJob(String jobName) throws SchedulerException {
        // 暂停 Trigger 对象
        scheduler.pauseTrigger(new TriggerKey(jobName));
        // 取消并删除 Job 调度
        scheduler.unscheduleJob(new TriggerKey(jobName));
        scheduler.deleteJob(new JobKey(jobName));
    }

    /**
     * 暂停 Quartz 中的 Job
     *
     * @param jobName 任务名
     * @throws SchedulerException 暂停异常
     */
    public void pauseJob(String jobName) throws SchedulerException {
        scheduler.pauseJob(new JobKey(jobName));
    }

    /**
     * 启动 Quartz 中的 Job
     *
     * @param jobName 任务名
     * @throws SchedulerException 启动异常
     */
    public void resumeJob(String jobName) throws SchedulerException {
        scheduler.resumeJob(new JobKey(jobName));
        scheduler.resumeTrigger(new TriggerKey(jobName));
    }

    /**
     * 立即触发一次 Quartz 中的 Job
     *
     * @param jobName 任务名
     * @throws SchedulerException 触发异常
     */
    public void triggerJob(String jobName) throws SchedulerException {
        scheduler.triggerJob(new JobKey(jobName));
    }

    private Trigger buildTrigger(String jobName, String cronExpression) {
        return TriggerBuilder.newTrigger()
                .withIdentity(jobName)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
    }

}
