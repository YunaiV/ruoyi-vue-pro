package cn.iocoder.yudao.module.oa.service.vehicle.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.oa.service.vehicle.OaVehicleReturnService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 还车申请审批结果监听器
 *
 * @author 芋道源码
 */
@Component
public class OaVehicleReturnStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private OaVehicleReturnService vehicleReturnService;

    @Override
    protected String getProcessDefinitionKey() {
        return BpmModelConstants.VEHICLE_RETURN;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        vehicleReturnService.updateVehicleReturnStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
    }

}
