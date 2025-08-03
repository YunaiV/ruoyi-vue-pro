package cn.iocoder.yudao.module.infra.job.logger;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.infra.service.logger.ApiErrorLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 物理删除 N 天前的错误日志的 Job
 *
 * @author j-sentinel
 */
@Slf4j
@Component
public class ErrorLogCleanJob implements JobHandler {

    @Resource
    private ApiErrorLogService apiErrorLogService;

    /**
     * 清理超过（14）天的日志
     */
    private static final Integer JOB_CLEAN_RETAIN_DAY = 14;

    /**
     * 每次删除间隔的条数，如果值太高可能会造成数据库的压力过大
     */
    private static final Integer DELETE_LIMIT = 100;

    @Override
    @TenantIgnore
    public String execute(String param) {
        Integer count = apiErrorLogService.cleanErrorLog(JOB_CLEAN_RETAIN_DAY,DELETE_LIMIT);
        log.info("[execute][定时执行清理错误日志数量 ({}) 个]", count);
        return String.format("定时执行清理错误日志数量 %s 个", count);
    }

}
