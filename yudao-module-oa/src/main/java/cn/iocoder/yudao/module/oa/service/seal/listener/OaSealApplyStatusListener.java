package cn.iocoder.yudao.module.oa.service.seal.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.oa.service.seal.OaSealApplyService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 用印申请审批结果监听器
 *
 * @author 芋道源码
 */
@Component
public class OaSealApplyStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private OaSealApplyService sealApplyService;

    @Override
    protected String getProcessDefinitionKey() {
        return BpmModelConstants.SEAL_APPLY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        sealApplyService.updateSealApplyStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
    }

}
