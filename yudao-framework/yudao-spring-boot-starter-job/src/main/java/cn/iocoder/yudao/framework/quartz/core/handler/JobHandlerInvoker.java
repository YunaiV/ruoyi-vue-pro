package cn.iocoder.yudao.framework.quartz.core.handler;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;
import cn.iocoder.yudao.framework.quartz.core.enums.JobDataKeyEnum;
import cn.iocoder.yudao.framework.quartz.core.service.JobLogFrameworkService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import jakarta.annotation.Resource;

import java.time.LocalDateTime;

/**
 * 基础 Job 调用者，负责调用 {@link JobHandler#execute(String)} 执行任务
 *@DisallowConcurrentExecution 禁止并发执行多个相同定义的JobDetail, 这个注解是加在Job类上的, 但意思并不是不能同时执行多个Job,
 * 而是不能并发执行同一个Job Definition(由JobDetail定义),但是可以同时执行多个不同的JobDetail, 也就是说一个JobKey对应的JobDetail实例不会进行并发执行，
 * 举例说明,我们有一个Job类,叫做QuartzJob,QuartzJob1，QuartzJob2他们都是实现了Job接口,并在这些类上加了这个注解@DisallowConcurrentExecution,
 * 然后在这个Job上定义了很多个JobDetail, QuartzJobJobDetail,QuartzJob1JobDetail,QuartzJob2JobDetail，那么当scheduler启动时,
 * 不会并发执行多个QuartzJobJobDetail或者QuartzJob1JobDetail以及QuartzJob2JobDetail，而是QuartzJob类下QuartzJobJobDetail的任务调度同步执行，
 * 其它几个(QuartzJob1JobDetail,QuartzJob2JobDetail的任务调度下的)一样，但是可以同时执行QuartzJobJobDetail,QuartzJob1JobDetail,QuartzJob2JobDetail
 * @PersistJobDataAfterExecution 同样, 也是加在Job上,表示当正常执行完Job后, JobDataMap中的数据应该被改动, 以被下一次调用时用。
 * （注意：@PersistJobDataAfterExecution是持久化JobDetail中的JobDataMap，对Trigger中的JobDataMap无效）
 * 当使用@PersistJobDataAfterExecution 注解时, 为了避免并发时, 存储数据造成混乱, 强烈建议把@DisallowConcurrentExecution注解也加上。
 * @author 芋道源码
 */

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
@Slf4j
public class JobHandlerInvoker extends QuartzJobBean {

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private JobLogFrameworkService jobLogFrameworkService;

    @Override
    protected void executeInternal(JobExecutionContext executionContext) throws JobExecutionException {
        // 第一步，获得 Job 数据
        Long jobId = executionContext.getMergedJobDataMap().getLong(JobDataKeyEnum.JOB_ID.name());
        String jobHandlerName = executionContext.getMergedJobDataMap().getString(JobDataKeyEnum.JOB_HANDLER_NAME.name());
        String jobHandlerParam = executionContext.getMergedJobDataMap().getString(JobDataKeyEnum.JOB_HANDLER_PARAM.name());
        int refireCount  = executionContext.getRefireCount();
        int retryCount = (Integer) executionContext.getMergedJobDataMap().getOrDefault(JobDataKeyEnum.JOB_RETRY_COUNT.name(), 0);
        int retryInterval = (Integer) executionContext.getMergedJobDataMap().getOrDefault(JobDataKeyEnum.JOB_RETRY_INTERVAL.name(), 0);

        // 第二步，执行任务
        Long jobLogId = null;
        LocalDateTime startTime = LocalDateTime.now();
        String data = null;
        Throwable exception = null;
        try {
            // 记录 Job 日志（初始）
            jobLogId = jobLogFrameworkService.createJobLog(jobId, startTime, jobHandlerName, jobHandlerParam, refireCount + 1);
            // 执行任务
            data = this.executeInternal(jobHandlerName, jobHandlerParam);
        } catch (Throwable ex) {
            exception = ex;
        }

        // 第三步，记录执行日志
        this.updateJobLogResultAsync(jobLogId, startTime, data, exception, executionContext);

        // 第四步，处理有异常的情况
        handleException(exception, refireCount, retryCount, retryInterval);
    }

    private String executeInternal(String jobHandlerName, String jobHandlerParam) throws Exception {
        // 获得 JobHandler 对象
        JobHandler jobHandler = applicationContext.getBean(jobHandlerName, JobHandler.class);
        Assert.notNull(jobHandler, "JobHandler 不会为空");
        // 执行任务
        return jobHandler.execute(jobHandlerParam);
    }

    private void updateJobLogResultAsync(Long jobLogId, LocalDateTime startTime, String data, Throwable exception,
                                         JobExecutionContext executionContext) {
        LocalDateTime endTime = LocalDateTime.now();
        // 处理是否成功
        boolean success = exception == null;
        if (!success) {
//            data = getRootCauseMessage(exception);
            var stackString = ExceptionUtil.stacktraceToString(exception);
            data = stackString.length() > 4000? stackString.substring(0,4000) : stackString;
        }
        // 更新日志
        try {
            jobLogFrameworkService.updateJobLogResultAsync(jobLogId, endTime, (int) LocalDateTimeUtil.between(startTime, endTime).toMillis(), success, data);
        } catch (Exception ex) {
            log.error("[executeInternal][Job({}) logId({}) 记录执行日志失败({}/{})]",
                    executionContext.getJobDetail().getKey(), jobLogId, success, data);
        }
    }

    private void handleException(Throwable exception,
                                 int refireCount, int retryCount, int retryInterval) throws JobExecutionException {
        // 如果有异常，则进行重试
        if (exception == null) {
            return;
        }
        // 情况一：如果到达重试上限，则直接抛出异常即可
        if (refireCount >= retryCount) {
            throw new JobExecutionException(exception);
        }

        // 情况二：如果未到达重试上限，则 sleep 一定间隔时间，然后重试
        // 这里使用 sleep 来实现，主要还是希望实现比较简单。因为，同一时间，不会存在大量失败的 Job。
        if (retryInterval > 0) {
            ThreadUtil.sleep(retryInterval);
        }
        // 第二个参数，refireImmediately = true，表示立即重试
        throw new JobExecutionException(exception, true);
    }

}
