package cn.iocoder.yudao.adminserver.modules.bpm.service.oa.listener;

import cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.event.BpmProcessInstanceResultEvent;
import cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.event.BpmProcessInstanceResultEventListener;
import cn.iocoder.yudao.adminserver.modules.bpm.service.oa.BpmOALeaveService;
import cn.iocoder.yudao.adminserver.modules.bpm.service.oa.impl.OALeaveServiceImpl;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * OA 请假单的结果的监听器实现类
 *
 * @author 芋道源码
 */
@Component
public class BpmOALeaveResultListener extends BpmProcessInstanceResultEventListener {

    @Resource
    private BpmOALeaveService leaveService;

    @Override
    protected String getProcessDefinitionKey() {
        return OALeaveServiceImpl.PROCESS_KEY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceResultEvent event) {
        leaveService.updateLeaveResult(Long.parseLong(event.getBusinessKey()), event.getResult());
    }

}
