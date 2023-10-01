package cn.iocoder.yudao.framework.quartz.core.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.quartz.core.service.JobLogFrameworkService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

// TODO @j-sentinel：名字和项目里其它保持统一，可以叫 JobLogCleanJob，不用带 handler 哈
/**
 * // TODO @j-sentinel：要写下类注释噢，就是这个类要干啥；然后下面两个应该是 @author 和 @since
 * @Author: j-sentinel
 * @Date: 2023/9/30 20:40
 */
@Slf4j
@AllArgsConstructor
public class JobLogJobHandler implements JobHandler {

    private LogJobProperties logJobProperties;

    public JobLogJobHandler(LogJobProperties logJobProperties) {
        this.logJobProperties = logJobProperties;
    }

    @Resource
    private JobLogFrameworkService jobLogFrameworkService;

    @Override
    public String execute(String param) throws Exception {
        Integer integer = jobLogFrameworkService.timingJobCleanLog(logJobProperties.getJobCleanRetainDay());
        log.info("定时执行清理定时任务日志数量({})个",integer);
        return String.format("定时执行清理定时任务日志数量 %s 个", integer);
    }
}
