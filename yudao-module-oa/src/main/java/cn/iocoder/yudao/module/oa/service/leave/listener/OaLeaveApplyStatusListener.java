package cn.iocoder.yudao.module.oa.service.leave.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.oa.service.leave.OaLeaveApplyService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 请假申请审批结果监听器
 *
 * @author 芋道源码
 */
@Component
public class OaLeaveApplyStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private OaLeaveApplyService leaveApplyService;

    @Override
    protected String getProcessDefinitionKey() {
        return BpmModelConstants.LEAVE_APPLY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        leaveApplyService.updateLeaveApplyStatus(Long.valueOf(event.getBusinessKey()), event.getStatus());
    }

}
