package cn.iocoder.yudao.module.oa.service.supply.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.oa.service.supply.OaSupplyApplyService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

// TODO DONE @AI：审批监听器统一放在业务模块的 listener 子包
/**
 * 用品申请审批结果监听器
 *
 * @author 芋道源码
 */
@Component
public class OaSupplyApplyStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private OaSupplyApplyService supplyApplyService;

    @Override
    protected String getProcessDefinitionKey() {
        return BpmModelConstants.SUPPLY_APPLY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        supplyApplyService.updateSupplyApplyStatus(Long.parseLong(event.getBusinessKey()), event.getId(), event.getStatus());
    }

}
