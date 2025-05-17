package cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.expression;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class BpmTaskAssignLeaderExpressionTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmTaskAssignLeaderExpression expression;

    @Mock
    private AdminUserApi adminUserApi;
    @Mock
    private DeptApi deptApi;

    @Mock
    private BpmProcessInstanceService processInstanceService;

    @Test
    public void testCalculateUsers_noDept() {
        // 准备参数
        DelegateExecution execution = mockDelegateExecution(1L);
        // mock 方法(startUser)
        AdminUserRespDTO startUser = randomPojo(AdminUserRespDTO.class, o -> o.setDeptId(10L));
        when(adminUserApi.getUser(eq(1L))).thenReturn(startUser);
        // mock 方法(getStartUserDept)没有部门
        when(deptApi.getDept(eq(10L))).thenReturn(null);

        // 调用
        Set<Long> result = expression.calculateUsers(execution, 1);
        // 断言
        assertEquals(0, result.size());
    }

    @Test
    public void testCalculateUsers_noParentDept() {
        // 准备参数
        DelegateExecution execution = mockDelegateExecution(1L);
        // mock 方法(startUser)
        AdminUserRespDTO startUser = randomPojo(AdminUserRespDTO.class, o -> o.setDeptId(10L));
        when(adminUserApi.getUser(eq(1L))).thenReturn(startUser);
        DeptRespDTO startUserDept = randomPojo(DeptRespDTO.class, o -> o.setId(10L).setParentId(100L)
                .setLeaderUserId(20L));
        // mock 方法（getDept）
        when(deptApi.getDept(eq(10L))).thenReturn(startUserDept);
        when(deptApi.getDept(eq(100L))).thenReturn(null);

        // 调用
        Set<Long> result = expression.calculateUsers(execution, 2);
        // 断言
        assertEquals(asSet(20L), result);
    }

    @Test
    public void testCalculateUsers_existParentDept() {
        // 准备参数
        DelegateExecution execution = mockDelegateExecution(1L);
        // mock 方法(startUser)
        AdminUserRespDTO startUser = randomPojo(AdminUserRespDTO.class, o -> o.setDeptId(10L));
        when(adminUserApi.getUser(eq(1L))).thenReturn(startUser);
        DeptRespDTO startUserDept = randomPojo(DeptRespDTO.class, o -> o.setId(10L).setParentId(100L)
                .setLeaderUserId(20L));
        when(deptApi.getDept(eq(10L))).thenReturn(startUserDept);
        // mock 方法（父 dept）
        DeptRespDTO parentDept = randomPojo(DeptRespDTO.class, o -> o.setId(100L).setParentId(1000L)
                .setLeaderUserId(200L));
        when(deptApi.getDept(eq(100L))).thenReturn(parentDept);

        // 调用
        Set<Long> result = expression.calculateUsers(execution, 2);
        // 断言
        assertEquals(asSet(200L), result);
    }

    @SuppressWarnings("SameParameterValue")
    private DelegateExecution mockDelegateExecution(Long startUserId) {
        ExecutionEntityImpl execution = new ExecutionEntityImpl();
        execution.setProcessInstanceId(randomString());
        // mock 返回 startUserId
        ExecutionEntityImpl processInstance = new ExecutionEntityImpl();
        processInstance.setStartUserId(String.valueOf(startUserId));
        when(processInstanceService.getProcessInstance(eq(execution.getProcessInstanceId())))
                .thenReturn(processInstance);
        return execution;
    }

}
