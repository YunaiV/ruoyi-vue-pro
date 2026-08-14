package cn.iocoder.yudao.module.hrm.service.attendance.record.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.hrm.service.attendance.record.HrmAttendanceLeaveService;
import cn.iocoder.yudao.module.hrm.service.attendance.record.HrmAttendanceLeaveServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * HRM 请假审批状态监听器
 *
 * @author 芋道源码
 */
@Component
public class HrmAttendanceLeaveStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private HrmAttendanceLeaveService attendanceLeaveService;

    @Override
    protected String getProcessDefinitionKey() {
        return HrmAttendanceLeaveServiceImpl.LEAVE_PROCESS_KEY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        attendanceLeaveService.updateLeaveApprovalStatus(
                Long.parseLong(event.getBusinessKey()), event.getId(), event.getStatus(), event.getReason());
    }

}
