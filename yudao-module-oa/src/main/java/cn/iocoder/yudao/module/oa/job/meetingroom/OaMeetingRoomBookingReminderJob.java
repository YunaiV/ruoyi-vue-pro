package cn.iocoder.yudao.module.oa.job.meetingroom;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.oa.service.meetingroom.OaMeetingRoomBookingService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * OA 会议开始提醒与过期预定处理 Job（兼容入口）
 *
 * 按原顺序先取消过期预定，再发送会议提醒。新配置请使用 {@link OaMeetingRoomBookingCancelExpiredJob}、
 * {@link OaMeetingRoomBookingSendReminderJob}；迁移时先停用本 Job 再启用两个新 Job，新旧 Job 不能同时启用
 *
 * @author 芋道源码
 */
@Component
public class OaMeetingRoomBookingReminderJob implements JobHandler {

    @Resource
    private OaMeetingRoomBookingService meetingRoomBookingService;

    @Override
    @TenantJob
    public String execute(String param) {
        int expiredCount = meetingRoomBookingService.cancelExpiredMeetingRoomBookings();
        int count = meetingRoomBookingService.sendMeetingRoomBookingReminders();
        return StrUtil.format("取消过期未开始会议 {} 条，发送会议提醒 {} 条", expiredCount, count);
    }

}
