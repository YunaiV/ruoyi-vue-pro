package cn.iocoder.yudao.framework.apilog.core.job;

import cn.iocoder.yudao.framework.apilog.core.service.ApiAccessLogFrameworkService;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.quartz.core.job.LogJobProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * @Author: j-sentinel
 * @Date: 2023/9/30 16:13
 */
@Slf4j
@AllArgsConstructor
public class ApiAccessLogJobHandler implements JobHandler {

    private LogJobProperties logJobProperties;

    public ApiAccessLogJobHandler(LogJobProperties logJobProperties) {
        this.logJobProperties = logJobProperties;
    }

    @Resource
    private ApiAccessLogFrameworkService apiAccessLogFrameworkService;

    @Override
    public String execute(String param) throws Exception {
        apiAccessLogFrameworkService.jobCleanAccessLog(logJobProperties.getAccessRetainDay());
        return "";
    }

}
