package cn.iocoder.yudao.module.bpm.framework.flowable.core.listener;

import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRejectReqVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmBoundaryEventType;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskTimeoutHandlerType;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.mq.message.task.TodoTaskReminderMessage;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.mq.producer.task.TodoTaskReminderProducer;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
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
import org.flowable.job.api.Job;
import org.flowable.task.api.Task;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

// TODO @芋艿：这块需要仔细再瞅瞅
/**
 * 监听定时器触发事件
 *
 * @author jason
 */
@Component
@Slf4j
public class BpmTimerFiredEventListener extends AbstractFlowableEngineEventListener {

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private BpmModelService bpmModelService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private BpmTaskService bpmTaskService;

    @Resource
    private TodoTaskReminderProducer todoTaskReminderProducer;

    public static final Set<FlowableEngineEventType> TIME_EVENTS = ImmutableSet.<FlowableEngineEventType>builder()
            .add(FlowableEngineEventType.TIMER_FIRED)
            .build();

    public BpmTimerFiredEventListener() {
        super(TIME_EVENTS);
    }

    @Override
    protected void timerFired(FlowableEngineEntityEvent event) {
        String processDefinitionId = event.getProcessDefinitionId();
        BpmnModel bpmnModel = bpmModelService.getBpmnModelByDefinitionId(processDefinitionId);
        Job entity = (Job) event.getEntity();
        FlowElement element = BpmnModelUtils.getFlowElementById(bpmnModel, entity.getElementId());
        // 如果是定时器边界事件
        if (element instanceof BoundaryEvent) {
            BoundaryEvent boundaryEvent = (BoundaryEvent) element;
            String boundaryEventType = BpmnModelUtils.parseBoundaryEventExtensionElement(boundaryEvent, BpmnModelConstants.BOUNDARY_EVENT_TYPE);
            BpmBoundaryEventType bpmTimerBoundaryEventType = BpmBoundaryEventType.typeOf(NumberUtils.parseInt(boundaryEventType));
            // 类型为用户任务超时未处理的情况
            if (bpmTimerBoundaryEventType == BpmBoundaryEventType.USER_TASK_TIMEOUT) {
                String timeoutAction = BpmnModelUtils.parseBoundaryEventExtensionElement(boundaryEvent, BpmnModelConstants.USER_TASK_TIMEOUT_HANDLER_ACTION);
                userTaskTimeoutHandler(event.getProcessInstanceId(), boundaryEvent.getAttachedToRefId(), NumberUtils.parseInt(timeoutAction));
            }
        }
    }

    private void userTaskTimeoutHandler(String processInstanceId, String taskDefKey, Integer timeoutAction) {
        BpmUserTaskTimeoutHandlerType userTaskTimeoutAction = BpmUserTaskTimeoutHandlerType.typeOf(timeoutAction);
        if (userTaskTimeoutAction != null) {
            // 查询超时未处理的任务 TODO 加签的情况会不会有问题 ???
            List<Task> taskList = bpmTaskService.getRunningTaskListByProcessInstanceId(processInstanceId, true, taskDefKey);
            taskList.forEach(task -> {
                // 自动提醒
                if (userTaskTimeoutAction == BpmUserTaskTimeoutHandlerType.REMINDER) {
                    TodoTaskReminderMessage message = new TodoTaskReminderMessage().setTenantId(Long.parseLong(task.getTenantId()))
                            .setUserId(Long.parseLong(task.getAssignee())).setTaskName(task.getName());
                    todoTaskReminderProducer.sendReminderMessage(message);
                }
                // 自动同意
                if (userTaskTimeoutAction == BpmUserTaskTimeoutHandlerType.APPROVE) {
                    // TODO @芋艿 这个上下文如何清除呢？ 任务通过后, BpmProcessInstanceEventListener 会有回调
                    TenantContextHolder.setTenantId(Long.parseLong(task.getTenantId()));
                    TenantContextHolder.setIgnore(false);
                    BpmTaskApproveReqVO req = new BpmTaskApproveReqVO().setId(task.getId())
                            .setReason("超时系统自动同意");
                    bpmTaskService.approveTask(Long.parseLong(task.getAssignee()), req);
                }
                // 自动拒绝
                if (userTaskTimeoutAction == BpmUserTaskTimeoutHandlerType.REJECT) {
                    // TODO  @芋艿 这个上下文如何清除呢？ 任务拒绝后, BpmProcessInstanceEventListener 会有回调
                    TenantContextHolder.setTenantId(Long.parseLong(task.getTenantId()));
                    TenantContextHolder.setIgnore(false);
                    BpmTaskRejectReqVO req = new BpmTaskRejectReqVO().setId(task.getId()).setReason("超时系统自动拒绝");
                    bpmTaskService.rejectTask(Long.parseLong(task.getAssignee()), req);
                }
            });
        }
    }
}
