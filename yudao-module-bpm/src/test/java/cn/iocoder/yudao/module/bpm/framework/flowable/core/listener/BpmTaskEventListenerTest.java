package cn.iocoder.yudao.module.bpm.framework.flowable.core.listener;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmBoundaryEventTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskTimeoutHandlerTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.service.definition.BpmModelService;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import org.flowable.bpmn.model.BoundaryEvent;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.job.api.Job;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

/**
 * {@link BpmTaskEventListener} 的单元测试
 *
 * @author 芋道源码
 */
public class BpmTaskEventListenerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmTaskEventListener taskEventListener;

    @Mock
    private BpmModelService modelService;
    @Mock
    private BpmTaskService taskService;

    @Test
    public void testTimerFired_elementIdFromJobHandlerConfiguration() {
        // mock 流程模型：task 上挂载【超时提醒】的边界事件
        BoundaryEvent boundaryEvent = new BoundaryEvent();
        boundaryEvent.setId("timeout-boundary");
        boundaryEvent.setAttachedToRefId("task");
        BpmnModelUtils.addExtensionElement(boundaryEvent, BpmnModelConstants.BOUNDARY_EVENT_TYPE,
                BpmBoundaryEventTypeEnum.USER_TASK_TIMEOUT.getType());
        BpmnModelUtils.addExtensionElement(boundaryEvent, BpmnModelConstants.USER_TASK_TIMEOUT_HANDLER_TYPE,
                BpmUserTaskTimeoutHandlerTypeEnum.REMINDER.getType());
        Process process = new Process();
        process.addFlowElement(boundaryEvent);
        BpmnModel bpmnModel = new BpmnModel();
        bpmnModel.addProcess(process);
        when(modelService.getBpmnModelByDefinitionId("definition-1")).thenReturn(bpmnModel);
        // 准备参数：部分 Flowable Job 的 elementId 为空，需要从 handler configuration 解析 activityId
        Job job = mock(Job.class);
        when(job.getJobHandlerConfiguration()).thenReturn("{\"activityId\":\"timeout-boundary\"}");
        FlowableEngineEntityEvent event = mock(FlowableEngineEntityEvent.class);
        when(event.getProcessDefinitionId()).thenReturn("definition-1");
        when(event.getProcessInstanceId()).thenReturn("process-instance-1");
        when(event.getEntity()).thenReturn(job);

        // 调用
        taskEventListener.timerFired(event);

        // 断言：使用解析出的 activityId 查找边界事件，并触发用户任务超时处理
        verify(taskService).processTaskTimeout("process-instance-1", "task",
                BpmUserTaskTimeoutHandlerTypeEnum.REMINDER.getType());
    }

}
