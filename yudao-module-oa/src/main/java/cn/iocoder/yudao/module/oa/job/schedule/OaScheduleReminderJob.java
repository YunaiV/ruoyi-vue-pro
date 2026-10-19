package cn.iocoder.yudao.module.oa.job.schedule;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.oa.service.schedule.OaScheduleService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * OA 日程提醒 Job
 *
 * @author 芋道源码
 */
@Component
public class OaScheduleReminderJob implements JobHandler {

    @Resource
    private OaScheduleService scheduleService;

    @Override
    @TenantJob
    public String execute(String param) {
        int count = scheduleService.sendScheduleReminders();
        return StrUtil.format("发送日程提醒 {} 条", count);
    }

}
