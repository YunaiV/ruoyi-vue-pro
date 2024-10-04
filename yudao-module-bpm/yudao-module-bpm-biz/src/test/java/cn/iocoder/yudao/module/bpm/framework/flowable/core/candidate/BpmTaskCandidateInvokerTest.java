package cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.strategy.BpmTaskCandidateUserStrategy;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link BpmTaskCandidateInvoker} 的单元测试
 *
 * @author 芋道源码
 */
public class BpmTaskCandidateInvokerTest extends BaseMockitoUnitTest {

    private BpmTaskCandidateInvoker taskCandidateInvoker;

    @Mock
    private AdminUserApi adminUserApi;

    @Spy
    private BpmTaskCandidateStrategy strategy ;

    @Spy
    private List<BpmTaskCandidateStrategy> strategyList ;

    @BeforeEach
    public void setUp() {
        strategy = new BpmTaskCandidateUserStrategy(adminUserApi); // 创建strategy实例
        strategyList = Collections.singletonList(strategy); // 创建strategyList
        taskCandidateInvoker = new BpmTaskCandidateInvoker(strategyList, adminUserApi);
    }

    @Test
    public void testCalculateUsers() {
        // 准备参数
        String param = "1,2";
        DelegateExecution execution = mock(DelegateExecution.class);
        // mock 方法（DelegateExecution）
        UserTask userTask = mock(UserTask.class);
        when(execution.getCurrentFlowElement()).thenReturn(userTask);
        when(userTask.getAttributeValue(eq(BpmnModelConstants.NAMESPACE), eq(BpmnModelConstants.USER_TASK_CANDIDATE_STRATEGY)))
                .thenReturn(BpmTaskCandidateStrategyEnum.USER.getStrategy().toString());
        when(userTask.getAttributeValue(eq(BpmnModelConstants.NAMESPACE), eq(BpmnModelConstants.USER_TASK_CANDIDATE_PARAM)))
                .thenReturn(param);
        // mock 方法（adminUserApi）
        AdminUserRespDTO user1 = randomPojo(AdminUserRespDTO.class, o -> o.setId(1L)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        AdminUserRespDTO user2 = randomPojo(AdminUserRespDTO.class, o -> o.setId(2L)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        Map<Long, AdminUserRespDTO> userMap = MapUtil.builder(user1.getId(), user1)
                .put(user2.getId(), user2).build();
        when(adminUserApi.getUserMap(eq(asSet(1L, 2L)))).thenReturn(userMap);

        // 调用
        Set<Long> results = taskCandidateInvoker.calculateUsers(execution);
        // 断言
        assertEquals(asSet(1L, 2L), results);
    }

    @Test
    public void testRemoveDisableUsers() {
        // 准备参数. 1L 可以找到；2L 是禁用的；3L 找不到
        Set<Long> assigneeUserIds = asSet(1L, 2L, 3L);
        // mock 方法
        AdminUserRespDTO user1 = randomPojo(AdminUserRespDTO.class, o -> o.setId(1L)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        AdminUserRespDTO user2 = randomPojo(AdminUserRespDTO.class, o -> o.setId(2L)
                .setStatus(CommonStatusEnum.DISABLE.getStatus()));
        Map<Long, AdminUserRespDTO> userMap = MapUtil.builder(user1.getId(), user1)
                .put(user2.getId(), user2).build();
        when(adminUserApi.getUserMap(eq(assigneeUserIds))).thenReturn(userMap);

        // 调用
        taskCandidateInvoker.removeDisableUsers(assigneeUserIds);
        // 断言
        assertEquals(asSet(1L), assigneeUserIds);
    }

}
