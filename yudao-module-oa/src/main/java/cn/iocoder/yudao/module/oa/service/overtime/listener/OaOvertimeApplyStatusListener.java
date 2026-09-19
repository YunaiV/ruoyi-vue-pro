package cn.iocoder.yudao.module.oa.service.overtime.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.oa.service.overtime.OaOvertimeApplyService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 加班申请审批结果监听器
 *
 * @author 芋道源码
 */
@Component
public class OaOvertimeApplyStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private OaOvertimeApplyService overtimeApplyService;

    @Override
    protected String getProcessDefinitionKey() {
        return BpmModelConstants.OVERTIME_APPLY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        overtimeApplyService.updateOvertimeApplyStatus(Long.valueOf(event.getBusinessKey()), event.getStatus());
    }

}
