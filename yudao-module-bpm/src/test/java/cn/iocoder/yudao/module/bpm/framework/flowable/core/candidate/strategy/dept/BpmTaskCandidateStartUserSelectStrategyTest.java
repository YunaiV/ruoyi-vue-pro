package cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.strategy.dept;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import org.assertj.core.util.Sets;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BpmTaskCandidateStartUserSelectStrategyTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmTaskCandidateStartUserSelectStrategy strategy;

    @Mock
    private BpmProcessInstanceService processInstanceService;

    @Test
    public void testCalculateUsersByTask() {
        // 准备参数
        String param = "2";
        // mock 方法（获得流程发起人）
        ProcessInstance processInstance = mock(ProcessInstance.class);
        DelegateExecution execution = mock(DelegateExecution.class);
        when(processInstanceService.getProcessInstance(eq(execution.getProcessInstanceId()))).thenReturn(processInstance);
        when(execution.getCurrentActivityId()).thenReturn("activity_001");
        // mock 方法（FlowableUtils）
        Map<String, Object> processVariables = new HashMap<>();
        processVariables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_SELECT_ASSIGNEES,
                MapUtil.of("activity_001", ListUtil.of(1L, 2L)));
        when(processInstance.getProcessVariables()).thenReturn(processVariables);

        // 调用
        Set<Long> userIds = strategy.calculateUsersByTask(execution, param);
        // 断言
        assertEquals(Sets.newLinkedHashSet(1L, 2L), userIds);
    }

    @Test
    public void testCalculateUsersByActivity() {
        // 准备参数
        String activityId = "activity_001";
        Map<String, Object> processVariables = new HashMap<>();
        processVariables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_SELECT_ASSIGNEES,
                MapUtil.of("activity_001", ListUtil.of(1L, 2L)));

        // 调用
        Set<Long> userIds = strategy.calculateUsersByActivity(null, activityId, null,
                null, null, processVariables);
        // 断言
        assertEquals(Sets.newLinkedHashSet(1L, 2L), userIds);
    }

}
