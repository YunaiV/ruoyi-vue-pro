package cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.listener;

import cn.iocoder.yudao.module.bpm.dal.dataobject.task.BpmTaskExtDO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.task.BpmTaskService;
import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.events.TaskRuntimeEvent;
import org.activiti.api.task.runtime.events.listener.TaskRuntimeEventListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 监听 {@link Task} 的开始与完成，创建与更新对应的 {@link BpmTaskExtDO} 记录
 *
 * @author 芋道源码
 */
@Component
public class BpmTaskEventListener<T extends RuntimeEvent<?, ?>>
        implements TaskRuntimeEventListener<T> {

    @Resource
    @Lazy // 解决循环依赖
    private BpmTaskService taskService;

    @Override
    @SuppressWarnings("unchecked")
    public void onEvent(T rawEvent) {
        // 由于 TaskRuntimeEventListener 无法保证只监听 TaskRuntimeEvent 事件，所以通过这样的方式
        if (!(rawEvent instanceof TaskRuntimeEvent)) {
            return;
        }
        TaskRuntimeEvent<Task> event = (TaskRuntimeEvent<Task>) rawEvent;

        // 创建时，插入拓展表
        if (event.getEventType() == TaskRuntimeEvent.TaskEvents.TASK_CREATED) {
            taskService.createTaskExt(event.getEntity());
            return;
        }

        // 取消时，更新拓展表为取消
        if (event.getEventType() == TaskRuntimeEvent.TaskEvents.TASK_CANCELLED) {
            taskService.updateTaskExtCancel(event.getEntity());
            return;
        }
        // 完成时，更新拓展表为已完成。要注意，在调用 delete ProcessInstance 才会触发该逻辑
        if (event.getEventType() == TaskRuntimeEvent.TaskEvents.TASK_COMPLETED) {
            taskService.updateTaskExtComplete(event.getEntity());
            return;
        }

        // 审核人修改时，进行拓展表，并额外发送通知
        if (event.getEventType() == TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED) {
            taskService.updateTaskExtAssign(event.getEntity());
            return;
        }

        // 其它事件，进行更新拓展表
        taskService.updateTaskExt(event.getEntity());
    }

}
