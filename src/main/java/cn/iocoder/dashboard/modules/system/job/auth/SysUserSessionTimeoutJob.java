package cn.iocoder.dashboard.modules.system.job.auth;

import cn.iocoder.dashboard.framework.quartz.core.handler.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户 Session 超时 Job
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class SysUserSessionTimeoutJob implements JobHandler {

    @Override
    public String execute(String param) throws Exception {
//        System.out.println("执行了一次任务");
        log.info("[execute][执行任务：{}]", param);
        return null;
    }

}
