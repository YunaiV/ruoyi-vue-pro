package cn.iocoder.yudao.module.oa.service.travel.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.oa.service.travel.OaTravelApplyService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 出差申请审批结果监听器
 *
 * @author 芋道源码
 */
@Component
public class OaTravelApplyStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private OaTravelApplyService travelApplyService;

    @Override
    protected String getProcessDefinitionKey() {
        return BpmModelConstants.TRAVEL_APPLY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        travelApplyService.updateTravelApplyStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
    }

}
