package cn.iocoder.yudao.module.bpm.framework.flowable.core.listener;

import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import com.google.common.collect.ImmutableSet;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.delegate.event.FlowableCancelledEvent;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

/**
 * 监听 {@link ProcessInstance} 的状态变更，更新其对应的 status 状态
 *
 * @author jason
 */
@Component
public class BpmProcessInstanceEventListener extends AbstractFlowableEngineEventListener {

    public static final Set<FlowableEngineEventType> PROCESS_INSTANCE_EVENTS = ImmutableSet.<FlowableEngineEventType>builder()
            .add(FlowableEngineEventType.PROCESS_CREATED)
            .add(FlowableEngineEventType.PROCESS_COMPLETED)
            .add(FlowableEngineEventType.PROCESS_CANCELLED)
            .build();

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private BpmProcessInstanceService processInstanceService;

    public BpmProcessInstanceEventListener(){
        super(PROCESS_INSTANCE_EVENTS);
    }

    @Override
    protected void processCreated(FlowableEngineEntityEvent event) {
        processInstanceService.processProcessInstanceCreated((ProcessInstance)event.getEntity());
    }

    @Override
    protected void processCompleted(FlowableEngineEntityEvent event) {
        processInstanceService.processProcessInstanceCompleted((ProcessInstance)event.getEntity());
    }

    @Override // 特殊情况：当跳转到 EndEvent 流程实例未结束, 会执行 deleteProcessInstance 方法
    protected void processCancelled(FlowableCancelledEvent event) {
        ProcessInstance processInstance = processInstanceService.getProcessInstance(event.getProcessInstanceId());
        processInstanceService.processProcessInstanceCompleted(processInstance);
    }

}
