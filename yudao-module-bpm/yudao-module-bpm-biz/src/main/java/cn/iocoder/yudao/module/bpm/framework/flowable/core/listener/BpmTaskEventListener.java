package cn.iocoder.yudao.module.bpm.framework.flowable.core.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.bpm.service.task.BpmActivityService;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import com.google.common.collect.ImmutableSet;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.delegate.event.FlowableActivityCancelledEvent;
import org.flowable.engine.history.HistoricActivityInstance;
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
    @Lazy // 解决循环依赖
    private BpmTaskService taskService;
    @Resource
    @Lazy // 解决循环依赖
    private BpmActivityService activityService;

    public static final Set<FlowableEngineEventType> TASK_EVENTS = ImmutableSet.<FlowableEngineEventType>builder()
            .add(FlowableEngineEventType.TASK_CREATED)
            .add(FlowableEngineEventType.TASK_ASSIGNED)
            //.add(FlowableEngineEventType.TASK_COMPLETED) // 由于审批通过时，已经记录了 task 的 status 为通过，所以不需要监听了。
//            .add(FlowableEngineEventType.ACTIVITY_MESSAGE_RECEIVED)
            .add(FlowableEngineEventType.ACTIVITY_CANCELLED)
            .build();

    public BpmTaskEventListener() {
        super(TASK_EVENTS);
    }

    @Override
    protected void taskCreated(FlowableEngineEntityEvent event) {
        taskService.updateTaskStatusWhenCreated((Task) event.getEntity());
    }

    @Override
    protected void taskAssigned(FlowableEngineEntityEvent event) {
        taskService.updateTaskExtAssign((Task) event.getEntity());
    }

    @Override
    protected void activityCancelled(FlowableActivityCancelledEvent event) {
        List<HistoricActivityInstance> activityList = activityService.getHistoricActivityListByExecutionId(event.getExecutionId());
        if (CollUtil.isEmpty(activityList)) {
            log.error("[activityCancelled][使用 executionId({}) 查找不到对应的活动实例]", event.getExecutionId());
            return;
        }
        // 遍历处理
        activityList.forEach(activity -> {
            if (StrUtil.isEmpty(activity.getTaskId())) {
                return;
            }
            taskService.updateTaskStatusWhenCanceled(activity.getTaskId());
        });
    }

//    @Override
//    protected void activityMessageReceived(FlowableMessageEvent event) {
//        BpmnModel bpmnModel = bpmModelService.getBpmnModelByDefinitionId(event.getProcessDefinitionId());
//        FlowElement element = BpmnModelUtils.getFlowElementById(bpmnModel, event.getActivityId());
//        if (element instanceof BoundaryEvent) {
//            BoundaryEvent boundaryEvent = (BoundaryEvent) element;
//            String boundaryEventType = parseBoundaryEventExtensionElement(boundaryEvent, BpmnModelConstants.BOUNDARY_EVENT_TYPE);
//            // 如果自定义类型为拒绝后处理，进行拒绝处理
//            if (Objects.equals(USER_TASK_REJECT_POST_PROCESS.getType(), NumberUtils.parseInt(boundaryEventType))) {
//                String rejectHandlerType = parseBoundaryEventExtensionElement((BoundaryEvent) element, BpmnModelConstants.USER_TASK_REJECT_HANDLER_TYPE);
//                rejectHandler(boundaryEvent, event.getProcessInstanceId(), boundaryEvent.getAttachedToRefId(), NumberUtils.parseInt(rejectHandlerType));
//            }
//        }
//    }
//
//    private void rejectHandler(BoundaryEvent boundaryEvent, String processInstanceId, String taskDefineKey, Integer rejectHandlerType) {
//        BpmUserTaskRejectHandlerType userTaskRejectHandlerType = BpmUserTaskRejectHandlerType.typeOf(rejectHandlerType);
//        if (userTaskRejectHandlerType != null) {
//            List<Task> taskList = taskService.getAssignedTaskListByConditions(processInstanceId, null, taskDefineKey);
//            taskList.forEach(task -> {
//                Integer taskStatus = FlowableUtils.getTaskStatus(task);
//                // 只有处于拒绝状态下才处理
//                if (Objects.equals(BpmTaskStatusEnum.REJECT.getStatus(), taskStatus)) {
//                    // 终止流程
//                    if (userTaskRejectHandlerType == BpmUserTaskRejectHandlerType.TERMINATION) {
//                        processInstanceService.updateProcessInstanceReject(task.getProcessInstanceId(), FlowableUtils.getTaskReason(task));
//                        return;
//                    }
//                    // 驳回
//                    if (userTaskRejectHandlerType == BpmUserTaskRejectHandlerType.RETURN_PRE_USER_TASK) {
//                        String returnTaskId = parseBoundaryEventExtensionElement(boundaryEvent, BpmnModelConstants.USER_TASK_REJECT_RETURN_TASK_ID);
//                        if (returnTaskId != null) {
//                            BpmTaskReturnReqVO reqVO = new BpmTaskReturnReqVO().setId(task.getId())
//                                    .setTargetTaskDefinitionKey(returnTaskId)
//                                    .setReason("任务拒绝回退");
//                            taskService.returnTask(getLoginUserId(), reqVO);
//                        }
//                    }
//                }
//            });
//        }
//    }

}
