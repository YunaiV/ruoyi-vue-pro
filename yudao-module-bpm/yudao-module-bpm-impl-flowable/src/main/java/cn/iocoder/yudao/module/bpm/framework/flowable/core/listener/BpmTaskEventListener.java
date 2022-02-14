package cn.iocoder.yudao.module.bpm.framework.flowable.core.listener;

import cn.iocoder.yudao.module.bpm.dal.dataobject.task.BpmTaskExtDO;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import com.google.common.collect.ImmutableSet;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.task.api.Task;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

/**
 * 监听 {@link org.flowable.task.api.Task} 的开始与完成，创建与更新对应的 {@link BpmTaskExtDO} 记录
 *
 * @author jason
 */
@Component
public class BpmTaskEventListener extends AbstractFlowableEngineEventListener {

    @Resource
    @Lazy // 解决循环依赖
    private BpmTaskService taskService;

    public static final Set<FlowableEngineEventType> TASK_EVENTS = ImmutableSet.<FlowableEngineEventType>builder()
            .add(FlowableEngineEventType.TASK_CREATED)
            .add(FlowableEngineEventType.TASK_ASSIGNED)
            .add(FlowableEngineEventType.TASK_COMPLETED)
            .build();

    public BpmTaskEventListener(){
        super(TASK_EVENTS);
    }

    @Override
    protected void taskCreated(FlowableEngineEntityEvent event) {
        taskService.createTaskExt((Task) event.getEntity());
    }

    @Override
    protected void taskCompleted(FlowableEngineEntityEvent event) {
        super.taskCompleted(event);
    }
}
