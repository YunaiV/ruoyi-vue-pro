package cn.iocoder.yudao.module.bpm.framework.flowable.core.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmBoundaryEventTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.service.definition.BpmModelService;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import com.google.common.collect.ImmutableSet;
import jakarta.annotation.Resource;
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
            .add(FlowableEngineEventType.TASK_COMPLETED) // 由于审批通过时，已经记录了 task 的 status 为通过，这里仅处理任务后置通知。
            .add(FlowableEngineEventType.ACTIVITY_CANCELLED)
            .add(FlowableEngineEventType.TIMER_FIRED) // 监听审批超时
            .build();

    public BpmTaskEventListener() {
        super(TASK_EVENTS);
    }

    @Override
    protected void taskCreated(FlowableEngineEntityEvent event) {
        Task entity = (Task) event.getEntity();
        FlowableUtils.execute(entity.getTenantId(), () -> taskService.processTaskCreated(entity));
    }

    @Override
    protected void taskAssigned(FlowableEngineEntityEvent event) {
        Task entity = (Task) event.getEntity();
        FlowableUtils.execute(entity.getTenantId(), () -> taskService.processTaskAssigned(entity));
    }

    @Override
    protected void taskCompleted(FlowableEngineEntityEvent event) {
        Task entity = (Task) event.getEntity();
        FlowableUtils.execute(entity.getTenantId(), () -> taskService.processTaskCompleted(entity));
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
        // 特殊 from https://t.zsxq.com/h6oWr ：当 elementId 为空时，尝试从 JobHandlerConfiguration 中解析 JSON 获取
        String elementId = entity.getElementId();
        if (elementId == null && entity.getJobHandlerConfiguration() != null) {
            try {
                String handlerConfig = entity.getJobHandlerConfiguration();
                if (handlerConfig.startsWith("{") && handlerConfig.contains("activityId")) {
                    elementId = new JSONObject(handlerConfig).getStr("activityId");
                }
            } catch (Exception e) {
                log.error("[timerFired][解析 entity({}) 失败]", entity, e);
                return;
            }
        }
        if (elementId == null) {
            log.error("[timerFired][解析 entity({}) elementId 为空，跳过处理]", entity);
            return;
        }
        FlowElement element = BpmnModelUtils.getFlowElementById(bpmnModel, entity.getElementId());
        if (!(element instanceof BoundaryEvent)) {
            return;
        }
        // 1.2 判断是否为超时处理
        BoundaryEvent boundaryEvent = (BoundaryEvent) element;
        String boundaryEventType = BpmnModelUtils.parseBoundaryEventExtensionElement(boundaryEvent,
                BpmnModelConstants.BOUNDARY_EVENT_TYPE);
        BpmBoundaryEventTypeEnum bpmTimerBoundaryEventType = BpmBoundaryEventTypeEnum.typeOf(NumberUtils.parseInt(boundaryEventType));

        // 2. 处理超时
        if (ObjectUtil.equal(bpmTimerBoundaryEventType, BpmBoundaryEventTypeEnum.USER_TASK_TIMEOUT)) {
            // 2.1 用户任务超时处理
            String timeoutHandlerType = BpmnModelUtils.parseBoundaryEventExtensionElement(boundaryEvent,
                    BpmnModelConstants.USER_TASK_TIMEOUT_HANDLER_TYPE);
            String taskKey = boundaryEvent.getAttachedToRefId();
            taskService.processTaskTimeout(event.getProcessInstanceId(), taskKey, NumberUtils.parseInt(timeoutHandlerType));
            // 2.2 延迟器超时处理
        } else if (ObjectUtil.equal(bpmTimerBoundaryEventType, BpmBoundaryEventTypeEnum.DELAY_TIMER_TIMEOUT)) {
            String taskKey = boundaryEvent.getAttachedToRefId();
            taskService.triggerTask(event.getProcessInstanceId(), taskKey);
            // 2.3 子流程超时处理
        } else if (ObjectUtil.equal(bpmTimerBoundaryEventType, BpmBoundaryEventTypeEnum.CHILD_PROCESS_TIMEOUT)) {
            String taskKey = boundaryEvent.getAttachedToRefId();
            taskService.processChildProcessTimeout(event.getProcessInstanceId(), taskKey);
        }
    }

}
