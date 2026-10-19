package cn.iocoder.yudao.module.oa.service.resign.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.oa.service.resign.OaResignApplyService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 离职申请审批结果监听器
 *
 * @author 芋道源码
 */
@Component
public class OaResignApplyStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private OaResignApplyService resignApplyService;

    @Override
    protected String getProcessDefinitionKey() {
        return BpmModelConstants.RESIGN_APPLY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        resignApplyService.updateResignApplyStatus(Long.valueOf(event.getBusinessKey()), event.getStatus());
    }

}
