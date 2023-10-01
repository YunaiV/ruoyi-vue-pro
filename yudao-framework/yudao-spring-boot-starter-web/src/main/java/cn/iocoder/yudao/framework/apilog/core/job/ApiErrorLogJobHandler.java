package cn.iocoder.yudao.framework.apilog.core.job;

import cn.iocoder.yudao.framework.apilog.core.service.ApiErrorLogFrameworkService;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.quartz.core.job.LogJobProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

// TODO @j-sentinel：同 JobLogJobHandler
/**
 * @Author: j-sentinel
 * @Date: 2023/9/30 16:13
 */
@Slf4j
@AllArgsConstructor
public class ApiErrorLogJobHandler implements JobHandler {

    private LogJobProperties logJobProperties;

    public ApiErrorLogJobHandler(LogJobProperties logJobProperties) {
        this.logJobProperties = logJobProperties;
    }

    @Resource
    private ApiErrorLogFrameworkService apiErrorLogFrameworkService;

    @Override
    public String execute(String param) throws Exception {
        apiErrorLogFrameworkService.jobCleanErrorLog(logJobProperties.getErrorRetainDay());
        return "";
    }

}
