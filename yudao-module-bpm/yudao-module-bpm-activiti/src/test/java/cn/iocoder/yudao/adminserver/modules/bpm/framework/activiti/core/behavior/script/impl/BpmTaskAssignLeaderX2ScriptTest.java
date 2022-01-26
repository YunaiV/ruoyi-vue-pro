package cn.iocoder.yudao.adminserver.modules.bpm.framework.activiti.core.behavior.script.impl;

import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.coreservice.modules.system.service.dept.SysDeptCoreService;
import cn.iocoder.yudao.coreservice.modules.system.service.user.SysUserCoreService;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

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
    private SysUserCoreService userCoreService;
    @Mock
    private SysDeptCoreService deptCoreService;

    @Test
    public void testCalculateTaskCandidateUsers_noDept() {
        // 准备参数
        TaskEntity task = buildTaskEntity(1L);
        // mock 方法(startUser)
        SysUserDO startUser = randomPojo(SysUserDO.class, o -> o.setDeptId(10L));
        when(userCoreService.getUser(eq(1L))).thenReturn(startUser);

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
        SysUserDO startUser = randomPojo(SysUserDO.class, o -> o.setDeptId(10L));
        when(userCoreService.getUser(eq(1L))).thenReturn(startUser);
        SysDeptDO startUserDept = randomPojo(SysDeptDO.class, o -> o.setId(10L).setParentId(100L)
                .setLeaderUserId(20L));
        when(deptCoreService.getDept(eq(10L))).thenReturn(startUserDept);

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
        SysUserDO startUser = randomPojo(SysUserDO.class, o -> o.setDeptId(10L));
        when(userCoreService.getUser(eq(1L))).thenReturn(startUser);
        SysDeptDO startUserDept = randomPojo(SysDeptDO.class, o -> o.setId(10L).setParentId(100L)
                .setLeaderUserId(20L));
        when(deptCoreService.getDept(eq(10L))).thenReturn(startUserDept);
        // mock 方法（父 dept）
        SysDeptDO parentDept = randomPojo(SysDeptDO.class, o -> o.setId(100L).setParentId(1000L)
                .setLeaderUserId(200L));
        when(deptCoreService.getDept(eq(100L))).thenReturn(parentDept);

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
