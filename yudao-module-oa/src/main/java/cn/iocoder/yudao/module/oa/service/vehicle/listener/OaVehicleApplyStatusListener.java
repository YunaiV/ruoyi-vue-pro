package cn.iocoder.yudao.module.oa.service.vehicle.listener;

import cn.iocoder.yudao.module.oa.service.vehicle.OaVehicleApplyService;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;

/**
 * 用车申请审批结果监听器
 *
 * @author 芋道源码
 */
@Component
public class OaVehicleApplyStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private OaVehicleApplyService vehicleApplyService;

    @Override
    protected String getProcessDefinitionKey() {
        return BpmModelConstants.VEHICLE_APPLY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        vehicleApplyService.updateVehicleApplyStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
    }

}
