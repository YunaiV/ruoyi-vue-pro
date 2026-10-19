package cn.iocoder.yudao.module.oa.service.meetingroom.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.oa.service.meetingroom.OaMeetingRoomBookingService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 会议室预定审批结果监听器
 *
 * @author 芋道源码
 */
@Component
public class OaMeetingRoomBookingStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private OaMeetingRoomBookingService meetingRoomBookingService;

    @Override
    protected String getProcessDefinitionKey() {
        return BpmModelConstants.MEETING_ROOM_BOOKING;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        meetingRoomBookingService.updateMeetingRoomBookingStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
    }
}
