package cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.strategy.dept;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.assertj.core.util.Sets;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;

import java.util.Set;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BpmTaskCandidateStartUserDeptLeaderStrategyTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmTaskCandidateStartUserDeptLeaderStrategy strategy;

    @Mock
    private BpmProcessInstanceService processInstanceService;

    @Mock
    private AdminUserApi adminUserApi;
    @Mock
    private DeptApi deptApi;

    @Test
    public void testCalculateUsersByTask() {
        // 准备参数
        String param = "2";
        // mock 方法（获得流程发起人）
        Long startUserId = 1L;
        ProcessInstance processInstance = mock(ProcessInstance.class);
        DelegateExecution execution = mock(DelegateExecution.class);
        when(processInstanceService.getProcessInstance(eq(execution.getProcessInstanceId()))).thenReturn(processInstance);
        when(processInstance.getStartUserId()).thenReturn(startUserId.toString());
        // mock 方法（获取发起人的部门负责人）
        mockGetStartUserDeptLeader(startUserId);

        // 调用
        Set<Long> userIds = strategy.calculateUsersByTask(execution, param);
        // 断言
        assertEquals(Sets.newLinkedHashSet(1001L), userIds);
    }

    @Test
    public void testGetStartUserDeptLeader() {
        // 准备参数
        String param = "2";
        // mock 方法
        Long startUserId = 1L;
        mockGetStartUserDeptLeader(startUserId);

        // 调用
        Set<Long> userIds = strategy.calculateUsersByActivity(null, null, param,
                startUserId, null, null);
        // 断言
        assertEquals(Sets.newLinkedHashSet(1001L), userIds);
    }

    private void mockGetStartUserDeptLeader(Long startUserId) {
        when(adminUserApi.getUser(eq(startUserId))).thenReturn(
                randomPojo(AdminUserRespDTO.class, o -> o.setId(startUserId).setDeptId(10L)));
        when(deptApi.getDept(any())).thenAnswer((Answer<DeptRespDTO>) invocationOnMock -> {
            Long deptId = invocationOnMock.getArgument(0);
            return randomPojo(DeptRespDTO.class, o -> o.setId(deptId).setParentId(deptId * 100).setLeaderUserId(deptId + 1));
        });
    }

}
