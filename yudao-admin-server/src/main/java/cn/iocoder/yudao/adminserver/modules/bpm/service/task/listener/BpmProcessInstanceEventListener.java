package cn.iocoder.yudao.adminserver.modules.bpm.service.task.listener;

import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.task.BpmProcessInstanceExtDO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.task.BpmProcessInstanceService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 监听 {@link ProcessInstance} 的开始与完成，创建与更新对应的 {@link BpmProcessInstanceExtDO} 记录
 *
 * @author 芋道源码
 */
@Component
public class BpmProcessInstanceEventListener implements ActivitiEventListener {

    @Resource
    @Lazy // 解决循环依赖
    private BpmProcessInstanceService processInstanceService;

    @Override
    public void onEvent(ActivitiEvent event) {
        // 不处理  ActivitiEventType.PROCESS_STARTED 事件。原因：事件发布时，流程实例还没进行入库，就已经发布了 ActivitiEvent 事件

        // 正常完成
        if (event.getType() == ActivitiEventType.PROCESS_COMPLETED
            || event.getType() == ActivitiEventType.PROCESS_COMPLETED_WITH_ERROR_END_EVENT) {
            return;
        }
        // 取消
        if (event.getType() == ActivitiEventType.PROCESS_CANCELLED) {
            // TODO
            System.out.println();
        }
    }

    @Override
    public boolean isFailOnException() {
        return true;
    }

}
