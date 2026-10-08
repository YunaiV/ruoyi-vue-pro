package cn.iocoder.yudao.module.bpm.service.task.listener;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmHttpRequestUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.service.delegate.DelegateTask;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link BpmUserTaskListener} 的单元测试
 *
 * @author HUIHUI
 */
public class BpmUserTaskListenerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmUserTaskListener userTaskListener;

    @Mock
    private BpmProcessInstanceService processInstanceService;
    @Mock
    private Expression listenerConfig;

    @Test
    @SuppressWarnings("unchecked")
    public void testNotify_bodyNull() {
        // mock 方法：监听器未配置 body
        ProcessInstance processInstance = mock(ProcessInstance.class);
        when(processInstanceService.getProcessInstance("process-instance-1")).thenReturn(processInstance);
        BpmSimpleModelNodeVO.ListenerHandler listenerHandler = new BpmSimpleModelNodeVO.ListenerHandler()
                .setPath("http://127.0.0.1/listener");
        // 准备参数
        DelegateTask delegateTask = mock(DelegateTask.class);
        when(delegateTask.getProcessInstanceId()).thenReturn("process-instance-1");
        when(delegateTask.getAssignee()).thenReturn("1");
        when(delegateTask.getTaskDefinitionKey()).thenReturn("task1");
        when(delegateTask.getId()).thenReturn("task-1");

        try (MockedStatic<BpmnModelUtils> bpmnModelUtilsMockedStatic = mockStatic(BpmnModelUtils.class);
             MockedStatic<BpmHttpRequestUtils> httpRequestUtilsMockedStatic = mockStatic(BpmHttpRequestUtils.class)) {
            bpmnModelUtilsMockedStatic.when(() -> BpmnModelUtils.parseListenerConfig(listenerConfig))
                    .thenReturn(listenerHandler);

            // 调用
            userTaskListener.notify(delegateTask);

            // 断言：body 为空时不会空指针，并追加任务相关的固定参数
            ArgumentCaptor<List<BpmSimpleModelNodeVO.HttpRequestParam>> bodyCaptor = ArgumentCaptor.forClass(List.class);
            httpRequestUtilsMockedStatic.verify(() -> BpmHttpRequestUtils.executeBpmHttpRequest(same(processInstance),
                    eq("http://127.0.0.1/listener"), isNull(), bodyCaptor.capture(), eq(false), isNull()));
            List<BpmSimpleModelNodeVO.HttpRequestParam> body = bodyCaptor.getValue();
            assertEquals(List.of("processInstanceId", "assignee", "taskDefinitionKey", "taskId"),
                    convertList(body, BpmSimpleModelNodeVO.HttpRequestParam::getKey));
            assertEquals(List.of("process-instance-1", "1", "task1", "task-1"),
                    convertList(body, BpmSimpleModelNodeVO.HttpRequestParam::getValue));
        }
    }

}
