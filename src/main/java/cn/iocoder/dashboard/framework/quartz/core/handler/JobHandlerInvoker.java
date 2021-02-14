package cn.iocoder.dashboard.framework.quartz.core.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.dashboard.common.exception.ServiceException;
import cn.iocoder.dashboard.common.pojo.CommonResult;
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

import static cn.iocoder.dashboard.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static cn.iocoder.dashboard.util.date.DateUtils.diff;

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
        // 获得 Job 数据
        // 1. 获得 jobId 参数
        String jobIdStr = getJobData(executionContext, JobDataKeyEnum.JOB_ID);
        if (NumberUtil.isNumber(jobIdStr)) {
            log.error("[executeInternal][Job({}) 获取不到正确的 jobId({})]", executionContext.getJobDetail().getKey(), jobIdStr);
            throw new IllegalStateException(StrUtil.format("Job({}) 获取不到正确的 jobId({})",
                    executionContext.getJobDetail().getKey(), jobIdStr));
        }
        Long jobId = Long.valueOf(jobIdStr);
        // 2. 获得 jobHandlerName 参数
        String jobHandlerName = getJobData(executionContext, JobDataKeyEnum.JOB_HANDLER_NAME);
        if (StrUtil.isEmpty(jobHandlerName)) {
            log.error("[executeInternal][Job({}) 获取不到正确的 jobHandlerName({})]", executionContext.getJobDetail().getKey(), jobHandlerName);
            throw new IllegalStateException(StrUtil.format("Job({}) 获取不到正确的 jobHandlerName({})",
                    executionContext.getJobDetail().getKey(), jobHandlerName));
        }
        // 3. 获得 jobHandlerParam 参数
        String jobHandlerParam = getJobData(executionContext, JobDataKeyEnum.JOB_HANDLER_PARAM);

        Long jobLogId = null;
        Date startTime = new Date();
        try {
            // 记录 Job 日志（初始）
            jobLogId = jobLogFrameworkService.createJobLog(jobId, jobHandlerName, jobHandlerParam);
            // 执行任务
            String data = this.executeInternal(jobId, jobHandlerName, jobHandlerParam);
            // 标记 Job 日志（成功)
            Date endTime = new Date();
            jobLogFrameworkService.updateJobLogSuccessAsync(jobLogId, endTime, diff(endTime, startTime), data);
        } catch (ServiceException serviceException) {
            // 标记 Job 日志（异常)
            Date endTime = new Date();
            jobLogFrameworkService.updateJobLogErrorAsync(jobLogId, endTime, diff(endTime, startTime),
                    serviceException.getCode(), serviceException.getMessage());
            // 最终还是抛出异常，用于停止任务
            throw serviceException;
        } catch (Throwable e) {
            // 标记 Job 日志（异常)
            Date endTime = new Date();
            jobLogFrameworkService.updateJobLogErrorAsync(jobLogId, endTime, diff(endTime, startTime),
                    INTERNAL_SERVER_ERROR.getCode(), ExceptionUtil.getRootCauseMessage(e));
            // 最终还是抛出异常，用于停止任务
            throw new JobExecutionException(e);
        }
    }

    private String executeInternal(Long jobId, String jobHandlerName, String jobHandlerParam) throws Exception {
        // 获得 JobHandler 对象
        JobHandler jobHandler = applicationContext.getBean(jobHandlerName, JobHandler.class);
        Assert.isNull(jobHandler, "JobHandler 不会为空");

        // 执行任务
        CommonResult<String> result = jobHandler.execute(jobHandlerParam);
        // 如果执行失败，则抛出 ServiceException 异常，方便统一记录
        if (result.isError()) {
            throw new ServiceException(result.getCode(), result.getMsg());
        }
        return result.getData();
    }

    private static String getJobData(JobExecutionContext executionContext, JobDataKeyEnum key) {
        return executionContext.getMergedJobDataMap().getString(key.name());
    }

}
