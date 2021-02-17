package cn.iocoder.dashboard.framework.quartz.core.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.dashboard.framework.quartz.core.enums.JobDataKeyEnum;
import cn.iocoder.dashboard.framework.quartz.core.service.JobLogFrameworkService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.Date;

import static cn.iocoder.dashboard.util.date.DateUtils.diff;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

/**
 * 基础 Job 调用者，负责调用 {@link JobHandler#execute(String)} 执行任务
 *
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
        String jobHandlerName = getJobHandlerName(executionContext);
        String jobHandlerParam = executionContext.getMergedJobDataMap().getString(JobDataKeyEnum.JOB_HANDLER_PARAM.name());

        // 第二步，执行任务
        Long jobLogId = null;
        Date startTime = new Date();
        String data = null;
        Throwable exception = null;
        try {
            // 记录 Job 日志（初始）
            jobLogId = jobLogFrameworkService.createJobLog(jobId, startTime, jobHandlerName, jobHandlerParam);
            // 执行任务
            data = this.executeInternal(jobHandlerName, jobHandlerParam);
        } catch (Throwable ex) {
            exception = ex;
        }

        // 第三步，记录执行日志
        this.updateJobLogResultAsync(jobLogId, startTime, data, exception, executionContext);

        // 最终还是抛出异常，用于停止任务
        if (exception != null) {
            throw new JobExecutionException(exception);
        }
    }

    private static String getJobHandlerName(JobExecutionContext executionContext) {
        String jobHandlerName = executionContext.getMergedJobDataMap().getString(JobDataKeyEnum.JOB_HANDLER_NAME.name());
        if (StrUtil.isEmpty(jobHandlerName)) {
            log.error("[executeInternal][Job({}) 获取不到正确的 jobHandlerName({})]",
                    executionContext.getJobDetail().getKey(), jobHandlerName);
            throw new IllegalStateException(StrUtil.format("Job({}) 获取不到正确的 jobHandlerName({})",
                    executionContext.getJobDetail().getKey(), jobHandlerName));
        }
        return jobHandlerName;
    }

    private String executeInternal(String jobHandlerName, String jobHandlerParam) throws Exception {
        // 获得 JobHandler 对象
        JobHandler jobHandler = applicationContext.getBean(jobHandlerName, JobHandler.class);
        Assert.notNull(jobHandler, "JobHandler 不会为空");
        // 执行任务
        return jobHandler.execute(jobHandlerParam);
    }

    private void updateJobLogResultAsync(Long jobLogId, Date startTime, String data, Throwable exception,
                                         JobExecutionContext executionContext) {
        Date endTime = new Date();
        try {
            if (data != null) { // 成功
                jobLogFrameworkService.updateJobLogSuccessAsync(jobLogId, endTime, (int) diff(endTime, startTime), data);
            } else { // 失败
                jobLogFrameworkService.updateJobLogErrorAsync(jobLogId, endTime, (int) diff(endTime, startTime),
                        getRootCauseMessage(exception));
            }
        } catch (Exception ex) {
            log.error("[executeInternal][Job({}) logId({}) 记录执行日志失败({})]",
                    executionContext.getJobDetail().getKey(), jobLogId,
                    data != null ? data : getRootCauseMessage(exception));
        }
    }

}
