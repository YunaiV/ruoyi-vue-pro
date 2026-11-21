package cn.iocoder.yudao.module.bpm.framework.flowable.core.listener;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTriggerTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.trigger.BpmTrigger;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link BpmTriggerTaskDelegate} 的单元测试
 *
 * @author 芋道源码
 */
public class BpmTriggerTaskDelegateTest extends BaseMockitoUnitTest {

    private BpmTriggerTaskDelegate triggerTaskDelegate;

    @Mock
    private BpmTrigger formUpdateTrigger;
    @Mock
    private BpmTrigger httpRequestTrigger;
    @Mock
    private DelegateExecution execution;

    @BeforeEach
    public void setUp() {
        // 模拟 Spring 注入 List<BpmTrigger>，并执行 @PostConstruct 按类型分组
        when(formUpdateTrigger.getType()).thenReturn(BpmTriggerTypeEnum.FORM_UPDATE);
        when(httpRequestTrigger.getType()).thenReturn(BpmTriggerTypeEnum.HTTP_REQUEST);
        triggerTaskDelegate = new BpmTriggerTaskDelegate();
        ReflectionTestUtils.setField(triggerTaskDelegate, "triggers", List.of(formUpdateTrigger, httpRequestTrigger));
        ReflectionTestUtils.invokeMethod(triggerTaskDelegate, "init");
    }

    @Test
    public void testExecute_dispatchByType() {
        // 准备参数：触发器节点配置为更新流程表单
        when(execution.getCurrentFlowElement()).thenReturn(buildTriggerTask(BpmTriggerTypeEnum.FORM_UPDATE, "param-1"));
        when(execution.getProcessInstanceId()).thenReturn("process-instance-1");

        // 调用
        triggerTaskDelegate.execute(execution);
        // 断言：只分发给类型匹配的触发器
        verify(formUpdateTrigger).execute("process-instance-1", "param-1");
        verify(httpRequestTrigger, never()).execute(any(), any());
    }

    @Test
    public void testExecute_triggerNotFound() {
        // 准备参数：触发器节点的类型，没有对应的触发器实现
        when(execution.getCurrentFlowElement()).thenReturn(buildTriggerTask(BpmTriggerTypeEnum.FORM_DELETE, "param-1"));

        // 调用
        triggerTaskDelegate.execute(execution);
        // 断言：不执行任何触发器
        verify(formUpdateTrigger, never()).execute(any(), any());
        verify(httpRequestTrigger, never()).execute(any(), any());
    }

    private static ServiceTask buildTriggerTask(BpmTriggerTypeEnum triggerType, String param) {
        ServiceTask serviceTask = new ServiceTask();
        serviceTask.setId("trigger-1");
        BpmnModelUtils.addExtensionElement(serviceTask, BpmnModelConstants.TRIGGER_TYPE, triggerType.getType());
        BpmnModelUtils.addExtensionElement(serviceTask, BpmnModelConstants.TRIGGER_PARAM, param);
        return serviceTask;
    }

}
