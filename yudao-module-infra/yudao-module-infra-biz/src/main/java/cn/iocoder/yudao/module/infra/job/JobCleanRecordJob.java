package cn.iocoder.yudao.module.infra.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.infra.service.job.JobLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

/**
 * 定时 物理 删除任务日志的 Job
 *
 * @author: j-sentinel
 */
@Slf4j
@Component
public class JobCleanRecordJob implements JobHandler {

    @Resource
    private JobLogService jobLogService;

    /**
     * 清理超过（14）天的日志
     */
    private static final Integer JOB_CLEAN_RETAIN_DAY = 14;

    /**
     * 每次删除间隔的条数，如果值太高可能会造成数据库的宕机
     */
    private static final Integer DELETE_LIMIT = 100;

    @Override
    @TenantIgnore
    public String execute(String param) throws Exception {
        Integer integer = jobLogService.timingJobCleanLog(JOB_CLEAN_RETAIN_DAY,DELETE_LIMIT);
        log.info("定时执行清理定时任务日志数量({})个",integer);
        return String.format("定时执行清理定时任务日志数量 %s 个", integer);
    }

}
