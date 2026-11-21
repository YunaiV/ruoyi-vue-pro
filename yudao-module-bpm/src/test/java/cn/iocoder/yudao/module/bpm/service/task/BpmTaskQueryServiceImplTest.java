package cn.iocoder.yudao.module.bpm.service.task;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.*;
import cn.iocoder.yudao.module.bpm.enums.definition.*;
import cn.iocoder.yudao.module.bpm.enums.task.BpmAttachmentTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants;
import org.flowable.engine.TaskService;
import org.flowable.engine.impl.persistence.entity.AttachmentEntityImpl;
import org.flowable.engine.task.Attachment;
import org.flowable.task.api.Task;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link BpmTaskQueryServiceImpl} 的单元测试
 *
 * @author HUIHUI
 */
public class BpmTaskQueryServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmTaskQueryServiceImpl taskService;

    @Mock
    private TaskService flowableTaskService;
    @Mock
    private Task task;

    @Test
    public void testValidateTask_success() {
        // 准备参数：通过 spy 绕过 Flowable 查询，只验证负责人校验
        BpmTaskQueryServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).validateTaskExists("task-1");
        when(task.getAssignee()).thenReturn("1");

        // 调用
        Task result = spyService.validateTask(1L, "task-1");
        // 断言
        assertSame(task, result);
    }

    @Test
    public void testValidateTask_assigneeNotSelf() {
        // 准备参数
        BpmTaskQueryServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).validateTaskExists("task-1");
        when(task.getAssignee()).thenReturn("2");

        // 调用，并断言异常
        assertServiceException(() -> spyService.validateTask(1L, "task-1"), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
    }

    @Test
    public void testValidateTask_noAssignee_success() {
        // 准备参数
        BpmTaskQueryServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).validateTaskExists("task-1");
        when(task.getAssignee()).thenReturn(null);

        // 调用
        Task result = spyService.validateTask(null, "task-1");
        // 断言
        assertSame(task, result);
    }

    @Test
    public void testValidateTask_ownerOnly() {
        // 准备参数：加签等待中的任务，只有 owner 没有 assignee
        BpmTaskQueryServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).validateTaskExists("task-1");
        when(task.getAssignee()).thenReturn(null);
        when(task.getOwner()).thenReturn("1");

        // 调用，并断言异常：owner 自己、其它用户、无 userId 都不允许操作
        assertServiceException(() -> spyService.validateTask(1L, "task-1"), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        assertServiceException(() -> spyService.validateTask(2L, "task-1"), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        assertServiceException(() -> spyService.validateTask(null, "task-1"), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
    }

    @Test
    public void testValidateTask_waitWithoutOwner() {
        // 准备参数：被委派的任务向前加签后等待中，没有 assignee，等待人记录在任务变量中
        BpmTaskQueryServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).validateTaskExists("task-1");
        when(task.getTaskLocalVariables()).thenReturn(Map.of(BpmnVariableConstants.TASK_VARIABLE_STATUS,
                BpmTaskStatusEnum.WAIT.getStatus()));

        // 调用，并断言异常：即使 owner 为空，也不允许操作
        assertServiceException(() -> spyService.validateTask(1L, "task-1"), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        assertServiceException(() -> spyService.validateTask(null, "task-1"), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
    }

    @Test
    public void testValidateTask_approving() {
        // 准备参数：向后加签的父任务，已审批通过，等待加签任务审批
        BpmTaskQueryServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).validateTaskExists("task-1");
        when(task.getAssignee()).thenReturn("1");
        when(task.getTaskLocalVariables()).thenReturn(Map.of(BpmnVariableConstants.TASK_VARIABLE_STATUS,
                BpmTaskStatusEnum.APPROVING.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> spyService.validateTask(1L, "task-1"), TASK_OPERATE_FAIL_APPROVING);
    }

    @Test
    public void testValidateTaskExists_notExists() {
        // 准备参数
        BpmTaskQueryServiceImpl spyService = spy(taskService);
        doReturn(null).when(spyService).getTask("task-1");

        // 调用，并断言异常
        assertServiceException(() -> spyService.validateTaskExists("task-1"), TASK_NOT_EXISTS);
    }

    // ========== Query 查询相关方法 ==========

    @Test
    public void testGetAllChildrenTaskListByParentTaskId_success() {
        // 准备参数：parent -> child1 -> child2；other 属于其它父任务
        TaskEntityImpl parent = buildTaskEntity("parent", null);
        TaskEntityImpl child1 = buildTaskEntity("child1", "parent");
        TaskEntityImpl child2 = buildTaskEntity("child2", "child1");
        TaskEntityImpl other = buildTaskEntity("other", "unknown");

        // 调用
        List<TaskEntityImpl> result = taskService.getAllChildrenTaskListByParentTaskId("parent",
                List.of(parent, child1, child2, other));
        // 断言
        assertEquals(List.of("child1", "child2"), result.stream().map(TaskEntityImpl::getId).toList());
        assertTrue(taskService.getAllChildrenTaskListByParentTaskId("parent", List.of(parent)).isEmpty());
        assertTrue(taskService.getAllChildrenTaskListByParentTaskId("parent", List.<Task>of()).isEmpty());
    }

    @Test
    public void testGetAttachments_filter() {
        // 准备参数
        Attachment attachment1 = buildAttachment("task-1", BpmAttachmentTypeEnum.TASK_ATTACHMENT.getType());
        Attachment attachment2 = buildAttachment("task-2", BpmAttachmentTypeEnum.TASK_ATTACHMENT.getType());
        Attachment attachment3 = buildAttachment("task-1", "other");
        when(flowableTaskService.getProcessInstanceAttachments("process-instance-1"))
                .thenReturn(List.of(attachment1, attachment2, attachment3));

        // 调用
        List<Attachment> result = taskService.getAttachments("process-instance-1", Set.of("task-1"),
                BpmAttachmentTypeEnum.TASK_ATTACHMENT);
        // 断言
        assertEquals(List.of(attachment1), result);
        assertEquals(3, taskService.getAttachments("process-instance-1", null, null).size());
    }

    // ========== 私有方法 ==========

    private static TaskEntityImpl buildTaskEntity(String id, String parentTaskId) {
        TaskEntityImpl taskEntity = new TaskEntityImpl();
        taskEntity.setId(id);
        taskEntity.setParentTaskId(parentTaskId);
        return taskEntity;
    }

    private static Attachment buildAttachment(String taskId, String type) {
        AttachmentEntityImpl attachment = new AttachmentEntityImpl();
        attachment.setTaskId(taskId);
        attachment.setType(type);
        return attachment;
    }

}
