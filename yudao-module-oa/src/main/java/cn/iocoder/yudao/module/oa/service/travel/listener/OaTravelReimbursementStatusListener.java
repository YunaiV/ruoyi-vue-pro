package cn.iocoder.yudao.module.oa.service.travel.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.oa.service.travel.OaTravelReimbursementService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 出差报销审批结果监听器
 *
 * @author 芋道源码
 */
@Component
public class OaTravelReimbursementStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private OaTravelReimbursementService travelReimbursementService;

    @Override
    protected String getProcessDefinitionKey() {
        return BpmModelConstants.TRAVEL_REIMBURSEMENT;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        travelReimbursementService.updateTravelReimbursementStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
    }

}
