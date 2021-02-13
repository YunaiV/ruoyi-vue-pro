package cn.iocoder.dashboard.framework.quartz.core.handler;

import cn.iocoder.dashboard.framework.quartz.core.enums.JobDataKeyEnum;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;

/**
 * 基础 Job 调用者，负责调用 {@link JobHandler#execute(String)} 执行任务
 *
 * @author 芋道源码
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class JobHandlerInvoker extends QuartzJobBean {

    @Resource
    private ApplicationContext applicationContext;

    @Override
    protected void executeInternal(JobExecutionContext executionContext) {
        // 获得 JobHandler 对象
        String jobHandlerName = getJobData(executionContext, JobDataKeyEnum.JOB_HANDLER_NAME);
        JobHandler jobHandler = applicationContext.getBean(jobHandlerName, JobHandler.class);

        // 执行任务
        String jobParam = getJobData(executionContext, JobDataKeyEnum.JOB_HANDLER_PARAM);
        try {
            jobHandler.execute(jobParam);
        } catch (Exception e) {
            // TODO 需要后续处理
        }
    }

    private static String getJobData(JobExecutionContext executionContext, JobDataKeyEnum key) {
        return executionContext.getMergedJobDataMap().getString(key.name());
    }

}
