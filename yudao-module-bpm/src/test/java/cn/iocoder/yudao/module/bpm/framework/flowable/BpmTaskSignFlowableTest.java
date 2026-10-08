package cn.iocoder.yudao.module.bpm.framework.flowable;

import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.*;
import cn.iocoder.yudao.module.bpm.enums.task.BpmCommentTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskSignTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskServiceImpl;
import jakarta.annotation.Resource;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link BpmTaskServiceImpl} 加签、减签的 Flowable 集成测试
 *
 * <p>覆盖向前/向后加签、多层加签、加签任务审批不通过、部分减签、递归减签，以及等待中（WAIT）任务、减签的权限校验</p>
 *
 * @author HUIHUI
 */
public class BpmTaskSignFlowableTest extends BaseFlowableUnitTest {

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private TaskService flowableTaskService;

    @Resource
    private BpmTaskServiceImpl bpmTaskService;
    @Resource
    private BpmProcessInstanceServiceImpl processInstanceService;

    private Deployment deployment;

    @BeforeEach
    public void setUp() {
        // 部署两级审批流程：task1 -> task2；审批人统一由测试上下文计算为用户 1
        deployment = repositoryService.createDeployment()
                .addClasspathResource("bpmn/two-step-approve.bpmn20.xml")
                .deploy();
        mockFlowableBackedServices(repositoryService);
    }

    @AfterEach
    public void tearDown() {
        // 清理测试数据：加签子任务没有 execution，需要先删除，否则 Flowable 级联删除流程实例时会空指针
        List<String> signTaskIds = flowableTaskService.createTaskQuery().deploymentId(deployment.getId()).list()
                .stream().filter(task -> task.getExecutionId() == null).map(Task::getId).toList();
        flowableTaskService.deleteTasks(signTaskIds);
        repositoryService.deleteDeployment(deployment.getId(), true);
    }

    // ========== 加签 ==========

    @Test
    public void testCreateSignTask_beforeMultiLevel_thenApprove() {
        // 准备参数：task1 向前加签给用户 2，用户 2 再向前加签给用户 3
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        Task childTask = createSignTask(1L, task1.getId(), BpmTaskSignTypeEnum.BEFORE, 2L).get(0);
        Task grandchildTask = createSignTask(2L, childTask.getId(), BpmTaskSignTypeEnum.BEFORE, 3L).get(0);
        // 断言：task1、子任务都处于等待中，只有孙任务可以审批
        assertWaitTask(task1.getId(), "1", BpmTaskSignTypeEnum.BEFORE);
        assertWaitTask(childTask.getId(), "2", BpmTaskSignTypeEnum.BEFORE);
        assertRunningTask(grandchildTask.getId(), "3");

        // 调用：用户 3 审批孙任务
        approveTask(3L, grandchildTask.getId());
        // 断言：子任务恢复给用户 2 审批，task1 继续等待
        assertRunningTask(childTask.getId(), "2");
        assertNull(bpmTaskService.getTask(childTask.getId()).getScopeType());
        assertWaitTask(task1.getId(), "1", BpmTaskSignTypeEnum.BEFORE);

        // 调用：用户 2 审批子任务
        approveTask(2L, childTask.getId());
        // 断言：task1 恢复给用户 1 审批
        assertRunningTask(task1.getId(), "1");
        assertNull(bpmTaskService.getTask(task1.getId()).getScopeType());

        // 调用：用户 1 审批 task1
        approveTask(1L, task1.getId());
        // 断言：流程流转到 task2
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testCreateSignTask_afterMultiLevel_thenApprove() {
        // 准备参数：task1 向后加签给用户 2，用户 1 审批后，用户 2 再向后加签给用户 3
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        Task childTask = createSignTask(1L, task1.getId(), BpmTaskSignTypeEnum.AFTER, 2L).get(0);
        assertWaitTask(childTask.getId(), "2", null);
        approveTask(1L, task1.getId());
        assertEquals(BpmTaskStatusEnum.APPROVING.getStatus(), getTaskStatus(task1.getId()));
        assertRunningTask(childTask.getId(), "2");
        Task grandchildTask = createSignTask(2L, childTask.getId(), BpmTaskSignTypeEnum.AFTER, 3L).get(0);
        assertWaitTask(grandchildTask.getId(), "3", null);

        // 调用：用户 2 审批子任务
        approveTask(2L, childTask.getId());
        // 断言：子任务处于审批通过中，孙任务恢复给用户 3 审批
        assertEquals(BpmTaskStatusEnum.APPROVING.getStatus(), getTaskStatus(childTask.getId()));
        assertRunningTask(grandchildTask.getId(), "3");

        // 调用：用户 3 审批孙任务
        approveTask(3L, grandchildTask.getId());
        // 断言：子任务、task1 依次递归完成，流程流转到 task2
        assertEquals(BpmTaskStatusEnum.APPROVE.getStatus(), getTaskStatus(childTask.getId()));
        assertNull(bpmTaskService.getTask(childTask.getId()));
        assertEquals(BpmTaskStatusEnum.APPROVE.getStatus(), getTaskStatus(task1.getId()));
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    // ========== 等待中（WAIT）任务的权限 ==========

    @Test
    public void testApproveTask_beforeSignParentWait() {
        // 准备参数：task1 向前加签给用户 2，task1 只有 owner 没有 assignee
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        Task childTask = createSignTask(1L, task1.getId(), BpmTaskSignTypeEnum.BEFORE, 2L).get(0);

        // 调用，并断言异常：任何人（包括 owner、无 userId 的系统调用）都不能直接操作等待中的 task1
        assertServiceException(() -> approveTask(1L, task1.getId()), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        assertServiceException(() -> approveTask(3L, task1.getId()), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        assertServiceException(() -> approveTask(null, task1.getId()), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        assertServiceException(() -> bpmTaskService.rejectTask(3L, new BpmTaskRejectReqVO()
                .setId(task1.getId()).setReason("不同意")), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        assertServiceException(() -> bpmTaskService.transferTask(3L, new BpmTaskTransferReqVO()
                .setId(task1.getId()).setAssigneeUserId(3L).setReason("转办")), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        assertServiceException(() -> createSignTask(1L, task1.getId(), BpmTaskSignTypeEnum.BEFORE, 3L),
                TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        // 断言：task1 仍等待子任务审批，流程未结束
        assertWaitTask(task1.getId(), "1", BpmTaskSignTypeEnum.BEFORE);
        assertNotNull(processInstanceService.getProcessInstance(processInstanceId));

        // 调用 + 断言：用户 2 审批子任务后，用户 1 可以正常审批 task1
        approveTask(2L, childTask.getId());
        approveTask(1L, task1.getId());
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testApproveTask_afterSignChildWait() {
        // 准备参数：task1 向后加签给用户 2，子任务只有 owner 没有 assignee
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        Task childTask = createSignTask(1L, task1.getId(), BpmTaskSignTypeEnum.AFTER, 2L).get(0);

        // 调用，并断言异常：task1 审批前，任何人（包括 owner）都不能直接操作等待中的子任务
        assertServiceException(() -> approveTask(2L, childTask.getId()), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        assertServiceException(() -> approveTask(3L, childTask.getId()), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        assertServiceException(() -> approveTask(null, childTask.getId()), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        assertServiceException(() -> bpmTaskService.rejectTask(3L, new BpmTaskRejectReqVO()
                .setId(childTask.getId()).setReason("不同意")), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        // 断言：子任务仍等待，task1 未审批，流程未结束
        assertWaitTask(childTask.getId(), "2", null);
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), getTaskStatus(task1.getId()));
        assertNotNull(processInstanceService.getProcessInstance(processInstanceId));

        // 调用 + 断言：用户 1 审批 task1 后，用户 2 可以正常审批子任务
        approveTask(1L, task1.getId());
        approveTask(2L, childTask.getId());
        assertEquals(BpmTaskStatusEnum.APPROVE.getStatus(), getTaskStatus(task1.getId()));
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testApproveTask_emptyAssigneeAndOwner_withoutUserId() {
        // 准备参数：task1 计算不到审批人，没有 assignee、owner
        when(taskCandidateInvoker.calculateUsersByTask(any())).thenReturn(Set.of());
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        assertNull(task1.getAssignee());
        assertNull(task1.getOwner());

        // 调用：保持自动审批的历史行为，userId 为 null 时允许审批
        when(taskCandidateInvoker.calculateUsersByTask(any())).thenReturn(Set.of(1L));
        approveTask(null, task1.getId());
        // 断言：流程流转到 task2
        assertEquals(BpmTaskStatusEnum.APPROVE.getStatus(), getTaskStatus(task1.getId()));
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    // ========== 审批不通过 ==========

    @Test
    public void testRejectTask_afterSignChild() {
        // 准备参数：task1 向后加签给用户 2，并且用户 1 已审批 task1
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        Task childTask = createSignTask(1L, task1.getId(), BpmTaskSignTypeEnum.AFTER, 2L).get(0);
        approveTask(1L, task1.getId());
        // 断言：审批通过中的 task1，不能再被审批不通过
        assertServiceException(() -> bpmTaskService.rejectTask(1L, new BpmTaskRejectReqVO()
                .setId(task1.getId()).setReason("不同意")), TASK_OPERATE_FAIL_APPROVING);

        // 调用：用户 2 审批不通过加签任务
        bpmTaskService.rejectTask(2L, new BpmTaskRejectReqVO().setId(childTask.getId()).setReason("不同意"));
        // 断言：加签任务、task1 都标记为不通过，流程实例不通过并结束，没有残留的任务
        assertEquals(BpmTaskStatusEnum.REJECT.getStatus(), getTaskStatus(childTask.getId()));
        assertEquals(BpmTaskStatusEnum.REJECT.getStatus(), getTaskStatus(task1.getId()));
        verify(commentService).createComment(eq(task1.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.REJECT), eq("加签任务不通过"));
        assertProcessInstanceRejected(processInstanceId);
    }

    @Test
    public void testRejectTask_afterMultiLevelMiddle_afterDeleteSign() {
        // 准备参数：task1 向后加签给用户 2、用户 4，用户 1 审批后，用户 2 再向后加签给用户 3
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        Map<String, Task> childTasks = createSignTask(1L, task1.getId(), BpmTaskSignTypeEnum.AFTER, 2L, 4L).stream()
                .collect(Collectors.toMap(Task::getOwner, Function.identity()));
        approveTask(1L, task1.getId());
        Task childTask = childTasks.get("2");
        Task grandchildTask = createSignTask(2L, childTask.getId(), BpmTaskSignTypeEnum.AFTER, 3L).get(0);

        // 调用：用户 1 减签用户 4
        bpmTaskService.deleteSignTask(1L, new BpmTaskSignDeleteReqVO().setId(childTasks.get("4").getId())
                .setReason("减签"));
        // 断言：用户 4 的任务被取消，task1 仍处于审批通过中，等待用户 2 审批
        assertEquals(BpmTaskStatusEnum.CANCEL.getStatus(), getTaskStatus(childTasks.get("4").getId()));
        assertEquals(BpmTaskStatusEnum.APPROVING.getStatus(), getTaskStatus(task1.getId()));
        assertRunningTask(childTask.getId(), "2");
        assertWaitTask(grandchildTask.getId(), "3", null);

        // 调用：用户 2（中间节点）审批不通过
        bpmTaskService.rejectTask(2L, new BpmTaskRejectReqVO().setId(childTask.getId()).setReason("不同意"));
        // 断言：中间节点、task1 标记为不通过，等待中的孙任务被取消，流程实例不通过并结束，没有残留的任务
        assertEquals(BpmTaskStatusEnum.REJECT.getStatus(), getTaskStatus(childTask.getId()));
        assertEquals(BpmTaskStatusEnum.REJECT.getStatus(), getTaskStatus(task1.getId()));
        assertEquals(BpmTaskStatusEnum.CANCEL.getStatus(), getTaskStatus(grandchildTask.getId()));
        assertProcessInstanceRejected(processInstanceId);
    }

    // ========== 减签 ==========

    @Test
    public void testDeleteSignTask_afterPartial_thenApprove() {
        // 准备参数：task1 向后加签给用户 2、用户 3，并且用户 1 已审批 task1
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        Map<String, Task> childTasks = createSignTask(1L, task1.getId(), BpmTaskSignTypeEnum.AFTER, 2L, 3L).stream()
                .collect(Collectors.toMap(Task::getOwner, Function.identity()));
        approveTask(1L, task1.getId());

        // 调用：用户 1 减签用户 2
        bpmTaskService.deleteSignTask(1L, new BpmTaskSignDeleteReqVO().setId(childTasks.get("2").getId())
                .setReason("减签"));
        // 断言：用户 2 的任务被取消，task1 仍处于审批通过中，等待用户 3 审批
        assertNull(bpmTaskService.getTask(childTasks.get("2").getId()));
        assertEquals(BpmTaskStatusEnum.CANCEL.getStatus(), getTaskStatus(childTasks.get("2").getId()));
        assertEquals(BpmTaskStatusEnum.APPROVING.getStatus(), getTaskStatus(task1.getId()));
        assertEquals(BpmTaskSignTypeEnum.AFTER.getType(), bpmTaskService.getTask(task1.getId()).getScopeType());
        assertRunningTask(childTasks.get("3").getId(), "3");
        verify(commentService).createComment(eq(task1.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.SUB_SIGN), eq("用户1"), eq("用户2"));

        // 调用：用户 3 审批
        approveTask(3L, childTasks.get("3").getId());
        // 断言：task1 审批通过，流程流转到 task2
        assertEquals(BpmTaskStatusEnum.APPROVE.getStatus(), getTaskStatus(task1.getId()));
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testDeleteSignTask_afterLast_completeApprovingParent() {
        // 准备参数：task1 向后加签给用户 2，并且用户 1 已审批 task1
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        Task childTask = createSignTask(1L, task1.getId(), BpmTaskSignTypeEnum.AFTER, 2L).get(0);
        approveTask(1L, task1.getId());

        // 调用：减签最后一个加签任务
        bpmTaskService.deleteSignTask(1L, new BpmTaskSignDeleteReqVO().setId(childTask.getId()).setReason("减签"));
        // 断言：审批通过中的 task1 直接完成，流程流转到 task2
        assertEquals(BpmTaskStatusEnum.CANCEL.getStatus(), getTaskStatus(childTask.getId()));
        assertEquals(BpmTaskStatusEnum.APPROVE.getStatus(), getTaskStatus(task1.getId()));
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testDeleteSignTask_recursive() {
        // 准备参数：task1 向前加签给用户 2，用户 2 再向前加签给用户 3
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        Task childTask = createSignTask(1L, task1.getId(), BpmTaskSignTypeEnum.BEFORE, 2L).get(0);
        Task grandchildTask = createSignTask(2L, childTask.getId(), BpmTaskSignTypeEnum.BEFORE, 3L).get(0);

        // 调用：用户 1 减签用户 2
        bpmTaskService.deleteSignTask(1L, new BpmTaskSignDeleteReqVO().setId(childTask.getId()).setReason("减签"));
        // 断言：子任务、孙任务都被取消删除，task1 恢复给用户 1 审批
        assertNull(bpmTaskService.getTask(childTask.getId()));
        assertNull(bpmTaskService.getTask(grandchildTask.getId()));
        assertEquals(BpmTaskStatusEnum.CANCEL.getStatus(), getTaskStatus(childTask.getId()));
        assertEquals(BpmTaskStatusEnum.CANCEL.getStatus(), getTaskStatus(grandchildTask.getId()));
        assertRunningTask(task1.getId(), "1");
        assertNull(bpmTaskService.getTask(task1.getId()).getScopeType());
        verify(commentService).createComment(eq(task1.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.SUB_SIGN), eq("用户1"), eq("用户2"));

        // 调用 + 断言：用户 1 审批，流程流转到 task2
        approveTask(1L, task1.getId());
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testDeleteSignTask_notSignUser() {
        // 准备参数：task1 向前加签给用户 2、用户 3
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        Map<String, Task> childTasks = createSignTask(1L, task1.getId(), BpmTaskSignTypeEnum.BEFORE, 2L, 3L).stream()
                .collect(Collectors.toMap(Task::getAssignee, Function.identity()));
        String childTaskId = childTasks.get("2").getId();

        // 调用，并断言异常：其它加签人、被减签人自己、无关用户、无 userId，都不能减签
        for (Long userId : new Long[]{3L, 2L, 99L, null}) {
            assertServiceException(() -> bpmTaskService.deleteSignTask(userId, new BpmTaskSignDeleteReqVO()
                    .setId(childTaskId).setReason("减签")), TASK_SIGN_DELETE_FAIL_NOT_SELF);
        }
        // 断言：加签任务都还在，task1 继续等待
        assertRunningTask(childTaskId, "2");
        assertEquals(2, bpmTaskService.getTaskListByParentTaskId(task1.getId()).size());
        assertWaitTask(task1.getId(), "1", BpmTaskSignTypeEnum.BEFORE);
    }

    @Test
    public void testDeleteSignTask_byParentAndAncestorSignUser() {
        // 准备参数：task1 向前加签给用户 2，用户 2 再向前加签给用户 3、用户 4
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        Task childTask = createSignTask(1L, task1.getId(), BpmTaskSignTypeEnum.BEFORE, 2L).get(0);
        Map<String, Task> grandchildTasks = createSignTask(2L, childTask.getId(), BpmTaskSignTypeEnum.BEFORE, 3L, 4L)
                .stream().collect(Collectors.toMap(Task::getAssignee, Function.identity()));

        // 调用：用户 2（父任务的加签人）减签用户 3
        bpmTaskService.deleteSignTask(2L, new BpmTaskSignDeleteReqVO().setId(grandchildTasks.get("3").getId())
                .setReason("减签"));
        // 断言：用户 3 的任务被取消，子任务继续等待用户 4
        assertEquals(BpmTaskStatusEnum.CANCEL.getStatus(), getTaskStatus(grandchildTasks.get("3").getId()));
        assertWaitTask(childTask.getId(), "2", BpmTaskSignTypeEnum.BEFORE);

        // 调用：用户 1（祖先任务的加签人）减签用户 4
        bpmTaskService.deleteSignTask(1L, new BpmTaskSignDeleteReqVO().setId(grandchildTasks.get("4").getId())
                .setReason("减签"));
        // 断言：子任务恢复给用户 2 审批，task1 继续等待
        assertEquals(BpmTaskStatusEnum.CANCEL.getStatus(), getTaskStatus(grandchildTasks.get("4").getId()));
        assertRunningTask(childTask.getId(), "2");
        assertWaitTask(task1.getId(), "1", BpmTaskSignTypeEnum.BEFORE);
    }

    // ========== 私有方法 ==========

    private String startProcessInstance() {
        return processInstanceService.createProcessInstance(1L, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey("twoStepApprove").setBusinessKey("business-1"));
    }

    private Task getRunningTask(String processInstanceId) {
        List<Task> tasks = bpmTaskService.getRunningTaskListByProcessInstanceId(processInstanceId, null, null);
        assertEquals(1, tasks.size());
        return tasks.get(0);
    }

    private List<Task> createSignTask(Long userId, String taskId, BpmTaskSignTypeEnum type, Long... userIds) {
        bpmTaskService.createSignTask(userId, new BpmTaskSignCreateReqVO().setId(taskId)
                .setType(type.getType()).setUserIds(Set.of(userIds)).setReason("加签"));
        List<Task> childTasks = bpmTaskService.getTaskListByParentTaskId(taskId);
        assertEquals(userIds.length, childTasks.size());
        return childTasks;
    }

    private void approveTask(Long userId, String taskId) {
        bpmTaskService.approveTask(userId, new BpmTaskApproveReqVO().setId(taskId).setReason("同意"));
    }

    private Integer getTaskStatus(String taskId) {
        return (Integer) bpmTaskService.getHistoricTask(taskId).getTaskLocalVariables()
                .get(BpmnVariableConstants.TASK_VARIABLE_STATUS);
    }

    /**
     * 断言流程实例不通过并结束，并且没有残留的任务（包括没有 execution 的加签任务）
     */
    private void assertProcessInstanceRejected(String processInstanceId) {
        assertNull(processInstanceService.getProcessInstance(processInstanceId));
        assertEquals(BpmProcessInstanceStatusEnum.REJECT.getStatus(), processInstanceService
                .getHistoricProcessInstance(processInstanceId).getProcessVariables()
                .get(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
        assertEquals(0, flowableTaskService.createTaskQuery().deploymentId(deployment.getId()).count());
    }

    /**
     * 断言任务处于等待中：只有 owner 没有 assignee，状态为 WAIT
     */
    private void assertWaitTask(String taskId, String owner, BpmTaskSignTypeEnum signType) {
        Task task = bpmTaskService.getTask(taskId);
        assertEquals(owner, task.getOwner());
        assertNull(task.getAssignee());
        assertEquals(signType != null ? signType.getType() : null, task.getScopeType());
        assertEquals(BpmTaskStatusEnum.WAIT.getStatus(), getTaskStatus(taskId));
    }

    /**
     * 断言任务可以被 assignee 审批：状态为 RUNNING
     */
    private void assertRunningTask(String taskId, String assignee) {
        Task task = bpmTaskService.getTask(taskId);
        assertEquals(assignee, task.getAssignee());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), getTaskStatus(taskId));
    }

}
