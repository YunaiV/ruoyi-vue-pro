package cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.strategy.other;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Disabled // TODO 芋艿：临时注释
public class BpmTaskCandidateExpressionStrategyTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmTaskCandidateExpressionStrategy strategy;

    @Test
    public void testCalculateUsersByTask() {
        try (MockedStatic<FlowableUtils> flowableUtilMockedStatic = mockStatic(FlowableUtils.class)) {
            // 准备参数
            String param = "1,2";
            DelegateExecution execution = mock(DelegateExecution.class);
            // mock 方法
            flowableUtilMockedStatic.when(() -> FlowableUtils.getExpressionValue(same(execution), eq(param)))
                    .thenReturn(asSet(1L, 2L));

            // 调用
            Set<Long> results = strategy.calculateUsersByTask(execution, param);
            // 断言
            assertEquals(asSet(1L, 2L), results);
        }
    }

    @Test
    public void testCalculateUsersByActivity() {
        try (MockedStatic<FlowableUtils> flowableUtilMockedStatic = mockStatic(FlowableUtils.class)) {
            // 准备参数
            String param = "1,2";
            Map<String, Object> processVariables = new HashMap<>();
            // mock 方法
            flowableUtilMockedStatic.when(() -> FlowableUtils.getExpressionValue(same(processVariables), eq(param)))
                    .thenReturn(asSet(1L, 2L));

            // 调用
            Set<Long> results = strategy.calculateUsersByActivity(null, null, param,
                    null, null, processVariables);
            // 断言
            assertEquals(asSet(1L, 2L), results);
        }
    }

}
