package cn.iocoder.yudao.module.bpm.framework.flowable.core.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmBoundaryEventType;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.service.definition.BpmModelService;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BoundaryEvent;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.delegate.event.FlowableActivityCancelledEvent;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.job.api.Job;
import org.flowable.task.api.Task;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * 监听 {@link Task} 的开始与完成
 *
 * @author jason
 */
@Component
@Slf4j
public class BpmTaskEventListener extends AbstractFlowableEngineEventListener {

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private BpmModelService modelService;
    @Resource
    @Lazy // 解决循环依赖
    private BpmTaskService taskService;

    public static final Set<FlowableEngineEventType> TASK_EVENTS = ImmutableSet.<FlowableEngineEventType>builder()
            .add(FlowableEngineEventType.TASK_CREATED)
            .add(FlowableEngineEventType.TASK_ASSIGNED)
//            .add(FlowableEngineEventType.TASK_COMPLETED) // 由于审批通过时，已经记录了 task 的 status 为通过，所以不需要监听了。
            .add(FlowableEngineEventType.ACTIVITY_CANCELLED)
            .add(FlowableEngineEventType.TIMER_FIRED) // 监听审批超时
            .build();

    public BpmTaskEventListener() {
        super(TASK_EVENTS);
    }

    @Override
    protected void taskCreated(FlowableEngineEntityEvent event) {
        taskService.processTaskCreated((Task) event.getEntity());
    }

    @Override
    protected void taskAssigned(FlowableEngineEntityEvent event) {
        taskService.processTaskAssigned((Task) event.getEntity());
    }

    @Override
    protected void activityCancelled(FlowableActivityCancelledEvent event) {
        List<HistoricActivityInstance> activityList = taskService.getHistoricActivityListByExecutionId(event.getExecutionId());
        if (CollUtil.isEmpty(activityList)) {
            log.error("[activityCancelled][使用 executionId({}) 查找不到对应的活动实例]", event.getExecutionId());
            return;
        }
        // 遍历处理
        activityList.forEach(activity -> {
            if (StrUtil.isEmpty(activity.getTaskId())) {
                return;
            }
            taskService.processTaskCanceled(activity.getTaskId());
        });
    }

    @Override
    @SuppressWarnings("PatternVariableCanBeUsed")
    protected void timerFired(FlowableEngineEntityEvent event) {
        // 1.1 只处理 BoundaryEvent 边界计时时间
        String processDefinitionId = event.getProcessDefinitionId();
        BpmnModel bpmnModel = modelService.getBpmnModelByDefinitionId(processDefinitionId);
        Job entity = (Job) event.getEntity();
        FlowElement element = BpmnModelUtils.getFlowElementById(bpmnModel, entity.getElementId());
        if (!(element instanceof BoundaryEvent)) {
            return;
        }
        // 1.2 判断是否为超时处理
        BoundaryEvent boundaryEvent = (BoundaryEvent) element;
        String boundaryEventType = BpmnModelUtils.parseBoundaryEventExtensionElement(boundaryEvent,
                BpmnModelConstants.BOUNDARY_EVENT_TYPE);
        BpmBoundaryEventType bpmTimerBoundaryEventType = BpmBoundaryEventType.typeOf(NumberUtils.parseInt(boundaryEventType));
        if (ObjectUtil.notEqual(bpmTimerBoundaryEventType, BpmBoundaryEventType.USER_TASK_TIMEOUT)) {
            return;
        }

        // 2. 处理超时
        String timeoutHandlerType = BpmnModelUtils.parseBoundaryEventExtensionElement(boundaryEvent,
                BpmnModelConstants.USER_TASK_TIMEOUT_HANDLER_TYPE);
        String taskKey = boundaryEvent.getAttachedToRefId();
        taskService.processTaskTimeout(event.getProcessInstanceId(), taskKey, NumberUtils.parseInt(timeoutHandlerType));
    }

}
