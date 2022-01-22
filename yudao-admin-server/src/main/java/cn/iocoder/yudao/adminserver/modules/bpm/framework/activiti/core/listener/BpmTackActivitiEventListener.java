package cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.listener;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmProcessDefinitionService;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.task.runtime.events.listener.TaskEventListener;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 监听 {@link TaskEntity} 相关的事件，设置相关属性。
 * 目的：解决 {@link TaskEventListener} 无法解决的场景
 *
 * @author 芋道源码
 */
@Component
public class BpmTackActivitiEventListener implements ActivitiEventListener {

    @Resource
    @Lazy // 解决循环依赖
    private BpmProcessDefinitionService processDefinitionService;

    @Override
    public void onEvent(ActivitiEvent event) {
        // Task 创建时，设置其分类，解决 TaskService 未提供 name 的设置方法
        if (ActivitiEventType.TASK_CREATED == event.getType()) {
            TaskEntity task = ((TaskEntity) ((ActivitiEntityEventImpl) event).getEntity());
            if (StrUtil.isNotEmpty(task.getCategory())) {
                return;
            }
            // 设置 name
            ProcessDefinition processDefinition = processDefinitionService.getProcessDefinition2(task.getProcessDefinitionId());
            if (processDefinition == null) {
                return;
            }
            task.setCategory(processDefinition.getCategory());
        }
    }

    @Override
    public boolean isFailOnException() {
        return true;
    }

}
