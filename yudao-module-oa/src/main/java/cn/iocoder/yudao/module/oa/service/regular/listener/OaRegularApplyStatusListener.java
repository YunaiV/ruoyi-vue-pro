package cn.iocoder.yudao.module.oa.service.regular.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.oa.service.regular.OaRegularApplyService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 转正申请审批结果监听器
 *
 * @author 芋道源码
 */
@Component
public class OaRegularApplyStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private OaRegularApplyService regularApplyService;

    @Override
    protected String getProcessDefinitionKey() {
        return BpmModelConstants.REGULAR_APPLY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        regularApplyService.updateRegularApplyStatus(Long.valueOf(event.getBusinessKey()), event.getStatus());
    }

}
