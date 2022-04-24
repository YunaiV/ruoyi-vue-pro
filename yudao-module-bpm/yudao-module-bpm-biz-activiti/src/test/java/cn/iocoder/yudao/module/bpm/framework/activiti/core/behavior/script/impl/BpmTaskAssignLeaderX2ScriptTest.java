package cn.iocoder.yudao.module.bpm.framework.activiti.core.behavior.script.impl;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class BpmTaskAssignLeaderX2ScriptTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmTaskAssignLeaderX2Script script;

    @Mock
    private AdminUserApi adminUserApi;
    @Mock
    private DeptApi deptApi;

    @Test
    public void testCalculateTaskCandidateUsers_noDept() {
        // 准备参数
        TaskEntity task = buildTaskEntity(1L);
        // mock 方法(startUser)
        AdminUserRespDTO startUser = randomPojo(AdminUserRespDTO.class, o -> o.setDeptId(10L));
        when(adminUserApi.getUser(eq(1L))).thenReturn(startUser);

        // 调用
        Set<Long> result = script.calculateTaskCandidateUsers(task);
        // 断言
        assertEquals(0, result.size());
    }

    @Test
    public void testCalculateTaskCandidateUsers_noParentDept() {
        // 准备参数
        TaskEntity task = buildTaskEntity(1L);
        // mock 方法(startUser)
        AdminUserRespDTO startUser = randomPojo(AdminUserRespDTO.class, o -> o.setDeptId(10L));
        when(adminUserApi.getUser(eq(1L))).thenReturn(startUser);
        DeptRespDTO startUserDept = randomPojo(DeptRespDTO.class, o -> o.setId(10L).setParentId(100L)
                .setLeaderUserId(20L));
        when(deptApi.getDept(eq(10L))).thenReturn(startUserDept);

        // 调用
        Set<Long> result = script.calculateTaskCandidateUsers(task);
        // 断言
        assertEquals(asSet(20L), result);
    }

    @Test
    public void testCalculateTaskCandidateUsers_existParentDept() {
        // 准备参数
        TaskEntity task = buildTaskEntity(1L);
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
        Set<Long> result = script.calculateTaskCandidateUsers(task);
        // 断言
        assertEquals(asSet(200L), result);
    }

    @SuppressWarnings("SameParameterValue")
    private TaskEntity buildTaskEntity(Long startUserId) {
        TaskEntityImpl task = new TaskEntityImpl();
        task.setProcessInstance(new ExecutionEntityImpl());
        task.getProcessInstance().setStartUserId(String.valueOf(startUserId));
        return task;
    }

}
