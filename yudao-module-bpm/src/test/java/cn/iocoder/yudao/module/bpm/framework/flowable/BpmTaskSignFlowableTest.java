package cn.iocoder.yudao.module.bpm.framework.flowable;

import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.*;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskAssignStartUserHandlerTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmCommentTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskSignTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.service.comment.BpmCommentServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceQueryServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceUpdateServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskQueryServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskUpdateServiceImpl;
import jakarta.annotation.Resource;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link BpmTaskUpdateServiceImpl} 加签、减签的 Flowable 集成测试
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
    private RuntimeService runtimeService;

    @Resource
    private BpmTaskQueryServiceImpl taskQueryService;
    @Resource
    private BpmTaskUpdateServiceImpl taskUpdateService;
    @Resource
    private BpmProcessInstanceQueryServiceImpl processInstanceQueryService;
    @Resource
    private BpmProcessInstanceUpdateServiceImpl processInstanceUpdateService;

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
        assertNull(taskQueryService.getTask(childTask.getId()).getScopeType());
        assertWaitTask(task1.getId(), "1", BpmTaskSignTypeEnum.BEFORE);

        // 调用：用户 2 审批子任务
        approveTask(2L, childTask.getId());
        // 断言：task1 恢复给用户 1 审批
        assertRunningTask(task1.getId(), "1");
        assertNull(taskQueryService.getTask(task1.getId()).getScopeType());

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
        String returnFlag = String.format(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_RETURN_FLAG, task1.getTaskDefinitionKey());
        runtimeService.setVariable(processInstanceId, returnFlag, true); // 模拟 task1 是被退回的节点
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
        assertNull(taskQueryService.getTask(childTask.getId()));
        assertEquals(BpmTaskStatusEnum.APPROVE.getStatus(), getTaskStatus(task1.getId()));
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
        // 断言：task1 自动完成时，清理退回标记
        assertFalse(runtimeService.hasVariable(processInstanceId, returnFlag));
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
        assertServiceException(() -> taskUpdateService.rejectTask(3L, new BpmTaskRejectReqVO()
                .setId(task1.getId()).setReason("不同意")), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        assertServiceException(() -> taskUpdateService.transferTask(3L, new BpmTaskTransferReqVO()
                .setId(task1.getId()).setAssigneeUserId(3L).setReason("转办")), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        assertServiceException(() -> createSignTask(1L, task1.getId(), BpmTaskSignTypeEnum.BEFORE, 3L),
                TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        // 断言：task1 仍等待子任务审批，流程未结束
        assertWaitTask(task1.getId(), "1", BpmTaskSignTypeEnum.BEFORE);
        assertNotNull(processInstanceQueryService.getProcessInstance(processInstanceId));

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
        assertServiceException(() -> taskUpdateService.rejectTask(3L, new BpmTaskRejectReqVO()
                .setId(childTask.getId()).setReason("不同意")), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        // 断言：子任务仍等待，task1 未审批，流程未结束
        assertWaitTask(childTask.getId(), "2", null);
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), getTaskStatus(task1.getId()));
        assertNotNull(processInstanceQueryService.getProcessInstance(processInstanceId));

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
        assertServiceException(() -> taskUpdateService.rejectTask(1L, new BpmTaskRejectReqVO()
                .setId(task1.getId()).setReason("不同意")), TASK_OPERATE_FAIL_APPROVING);

        // 调用：用户 2 审批不通过加签任务
        taskUpdateService.rejectTask(2L, new BpmTaskRejectReqVO().setId(childTask.getId()).setReason("不同意"));
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
        taskUpdateService.deleteSignTask(1L, new BpmTaskSignDeleteReqVO().setId(childTasks.get("4").getId())
                .setReason("减签"));
        // 断言：用户 4 的任务被取消，task1 仍处于审批通过中，等待用户 2 审批
        assertEquals(BpmTaskStatusEnum.CANCEL.getStatus(), getTaskStatus(childTasks.get("4").getId()));
        assertEquals(BpmTaskStatusEnum.APPROVING.getStatus(), getTaskStatus(task1.getId()));
        assertRunningTask(childTask.getId(), "2");
        assertWaitTask(grandchildTask.getId(), "3", null);

        // 调用：用户 2（中间节点）审批不通过
        taskUpdateService.rejectTask(2L, new BpmTaskRejectReqVO().setId(childTask.getId()).setReason("不同意"));
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
        taskUpdateService.deleteSignTask(1L, new BpmTaskSignDeleteReqVO().setId(childTasks.get("2").getId())
                .setReason("减签"));
        // 断言：用户 2 的任务被取消，task1 仍处于审批通过中，等待用户 3 审批
        assertNull(taskQueryService.getTask(childTasks.get("2").getId()));
        assertEquals(BpmTaskStatusEnum.CANCEL.getStatus(), getTaskStatus(childTasks.get("2").getId()));
        assertEquals(BpmTaskStatusEnum.APPROVING.getStatus(), getTaskStatus(task1.getId()));
        assertEquals(BpmTaskSignTypeEnum.AFTER.getType(), taskQueryService.getTask(task1.getId()).getScopeType());
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
        taskUpdateService.deleteSignTask(1L, new BpmTaskSignDeleteReqVO().setId(childTask.getId()).setReason("减签"));
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
        taskUpdateService.deleteSignTask(1L, new BpmTaskSignDeleteReqVO().setId(childTask.getId()).setReason("减签"));
        // 断言：子任务、孙任务都被取消删除，task1 恢复给用户 1 审批
        assertNull(taskQueryService.getTask(childTask.getId()));
        assertNull(taskQueryService.getTask(grandchildTask.getId()));
        assertEquals(BpmTaskStatusEnum.CANCEL.getStatus(), getTaskStatus(childTask.getId()));
        assertEquals(BpmTaskStatusEnum.CANCEL.getStatus(), getTaskStatus(grandchildTask.getId()));
        assertRunningTask(task1.getId(), "1");
        assertNull(taskQueryService.getTask(task1.getId()).getScopeType());
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
            assertServiceException(() -> taskUpdateService.deleteSignTask(userId, new BpmTaskSignDeleteReqVO()
                    .setId(childTaskId).setReason("减签")), TASK_SIGN_DELETE_FAIL_NOT_SELF);
        }
        // 断言：加签任务都还在，task1 继续等待
        assertRunningTask(childTaskId, "2");
        assertEquals(2, taskQueryService.getTaskListByParentTaskId(task1.getId()).size());
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
        taskUpdateService.deleteSignTask(2L, new BpmTaskSignDeleteReqVO().setId(grandchildTasks.get("3").getId())
                .setReason("减签"));
        // 断言：用户 3 的任务被取消，子任务继续等待用户 4
        assertEquals(BpmTaskStatusEnum.CANCEL.getStatus(), getTaskStatus(grandchildTasks.get("3").getId()));
        assertWaitTask(childTask.getId(), "2", BpmTaskSignTypeEnum.BEFORE);

        // 调用：用户 1（祖先任务的加签人）减签用户 4
        taskUpdateService.deleteSignTask(1L, new BpmTaskSignDeleteReqVO().setId(grandchildTasks.get("4").getId())
                .setReason("减签"));
        // 断言：子任务恢复给用户 2 审批，task1 继续等待
        assertEquals(BpmTaskStatusEnum.CANCEL.getStatus(), getTaskStatus(grandchildTasks.get("4").getId()));
        assertRunningTask(childTask.getId(), "2");
        assertWaitTask(task1.getId(), "1", BpmTaskSignTypeEnum.BEFORE);
    }

    // ========== 委派 + 加签 ==========

    @Test
    public void testApproveTask_delegatedBeforeSign() {
        // 准备参数：用户 1 委派给用户 2，用户 2 向前加签给用户 3
        String processInstanceId = startProcessInstance();
        String taskId = getRunningTask(processInstanceId).getId();
        delegateTask(1L, taskId, 2L);
        Task childTask = createSignTask(2L, taskId, BpmTaskSignTypeEnum.BEFORE, 3L).get(0);
        // 断言：父任务等待中，owner 仍为委派人用户 1，等待人为用户 2
        Task waitTask = taskQueryService.getTask(taskId);
        assertEquals("1", waitTask.getOwner());
        assertNull(waitTask.getAssignee());
        assertEquals(DelegationState.PENDING, waitTask.getDelegationState());
        assertEquals("2", waitTask.getTaskLocalVariables().get(BpmnVariableConstants.TASK_SIGN_WAIT_ASSIGNEE));
        assertEquals(BpmTaskStatusEnum.WAIT.getStatus(), getTaskStatus(taskId));

        // 调用：用户 3 审批加签任务
        approveTask(3L, childTask.getId());
        // 断言：恢复给用户 2 审批，仍处于委派中
        assertRunningTask(taskId, "2");
        assertEquals(DelegationState.PENDING, taskQueryService.getTask(taskId).getDelegationState());

        // 调用：用户 2 审批
        approveTask(2L, taskId);
        // 断言：归还给用户 1，节点没有推进
        Task returnedTask = getRunningTask(processInstanceId);
        assertEquals("task1", returnedTask.getTaskDefinitionKey());
        assertEquals("1", returnedTask.getAssignee());
        assertEquals(DelegationState.RESOLVED, returnedTask.getDelegationState());

        // 调用：用户 1 审批
        approveTask(1L, taskId);
        // 断言：节点完成
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testApproveTask_delegatedAfterSign() {
        // 准备参数：用户 1 委派给用户 2，用户 2 向后加签给用户 3
        String processInstanceId = startProcessInstance();
        String taskId = getRunningTask(processInstanceId).getId();
        delegateTask(1L, taskId, 2L);
        Task childTask = createSignTask(2L, taskId, BpmTaskSignTypeEnum.AFTER, 3L).get(0);

        // 调用：用户 2 审批
        approveTask(2L, taskId);
        // 断言：先激活用户 3 的加签任务，不归还委派
        assertRunningTask(childTask.getId(), "3");
        assertEquals("2", taskQueryService.getTask(taskId).getAssignee());
        assertEquals(BpmTaskStatusEnum.APPROVING.getStatus(), getTaskStatus(taskId));

        // 调用：用户 3 审批加签任务
        approveTask(3L, childTask.getId());
        // 断言：归还给用户 1，节点没有推进
        Task returnedTask = getRunningTask(processInstanceId);
        assertEquals("task1", returnedTask.getTaskDefinitionKey());
        assertEquals("1", returnedTask.getAssignee());
        assertEquals(DelegationState.RESOLVED, returnedTask.getDelegationState());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), getTaskStatus(taskId));

        // 调用：用户 1 审批
        approveTask(1L, taskId);
        // 断言：节点完成
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testDeleteSignTask_delegatedBeforeSign() {
        // 准备参数：用户 1 委派给用户 2，用户 2 向前加签给用户 3
        String processInstanceId = startProcessInstance();
        String taskId = getRunningTask(processInstanceId).getId();
        delegateTask(1L, taskId, 2L);
        Task childTask = createSignTask(2L, taskId, BpmTaskSignTypeEnum.BEFORE, 3L).get(0);
        BpmTaskSignDeleteReqVO reqVO = new BpmTaskSignDeleteReqVO().setId(childTask.getId()).setReason("减签");
        // 断言：加签人是用户 2，委派人用户 1 看不到待办，也不能减签
        assertEquals(taskId, taskQueryService.getTodoTask(2L, taskId, processInstanceId).getId());
        assertNull(taskQueryService.getTodoTask(1L, taskId, processInstanceId));
        assertServiceException(() -> taskUpdateService.deleteSignTask(1L, reqVO), TASK_SIGN_DELETE_FAIL_NOT_SELF);

        // 调用：用户 2 减签
        taskUpdateService.deleteSignTask(2L, reqVO);
        // 断言：恢复给用户 2 审批，仍处于委派中；减签日志记录被减签人用户 3
        assertRunningTask(taskId, "2");
        assertEquals(DelegationState.PENDING, taskQueryService.getTask(taskId).getDelegationState());
        verify(commentService).createComment(eq(taskId), eq(processInstanceId),
                eq(BpmCommentTypeEnum.SUB_SIGN), eq("用户2"), eq("用户3"));
    }

    @Test
    public void testDeleteSignTask_afterSignThenDelegate() {
        // 准备参数：用户 1 向后加签给用户 3，再把任务委派给用户 2
        String processInstanceId = startProcessInstance();
        String taskId = getRunningTask(processInstanceId).getId();
        Task childTask = createSignTask(1L, taskId, BpmTaskSignTypeEnum.AFTER, 3L).get(0);
        delegateTask(1L, taskId, 2L);
        // 断言：用户 1 是实际的加签人，仍可查看待办详情
        assertEquals(taskId, taskQueryService.getTodoTask(1L, taskId, processInstanceId).getId());

        // 调用：用户 1 减签
        taskUpdateService.deleteSignTask(1L, new BpmTaskSignDeleteReqVO().setId(childTask.getId()).setReason("减签"));
        // 断言：父任务仍委派给用户 2 处理
        Task delegatedTask = taskQueryService.getTask(taskId);
        assertEquals("2", delegatedTask.getAssignee());
        assertNull(delegatedTask.getScopeType());
        assertEquals(DelegationState.PENDING, delegatedTask.getDelegationState());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), getTaskStatus(taskId));

        // 调用：用户 2 审批
        approveTask(2L, taskId);
        // 断言：归还给用户 1，节点没有推进
        assertRunningTask(taskId, "1");
        assertEquals(DelegationState.RESOLVED, taskQueryService.getTask(taskId).getDelegationState());
        // 调用：用户 1 审批
        approveTask(1L, taskId);
        // 断言：节点完成
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testApproveTask_afterSignChildrenAlreadyDeleted() {
        // 准备参数：用户 1 委派给用户 2，用户 2 向后加签给用户 3
        String processInstanceId = startProcessInstance();
        String taskId = getRunningTask(processInstanceId).getId();
        delegateTask(1L, taskId, 2L);
        Task childTask = createSignTask(2L, taskId, BpmTaskSignTypeEnum.AFTER, 3L).get(0);
        // 准备参数：模拟审批读取任务后，加签任务被并发减签：子任务已删除，但读到的父任务仍是向后加签
        flowableTaskService.deleteTask(childTask.getId());
        assertEquals(BpmTaskSignTypeEnum.AFTER.getType(), taskQueryService.getTask(taskId).getScopeType());

        // 调用：用户 2 审批
        approveTask(2L, taskId);
        // 断言：不进入审批通过中（APPROVING），按委派归还给用户 1
        assertRunningTask(taskId, "1");
        assertEquals(DelegationState.RESOLVED, taskQueryService.getTask(taskId).getDelegationState());

        // 调用：用户 1 审批
        approveTask(1L, taskId);
        // 断言：按普通任务完成，节点推进
        assertEquals(BpmTaskStatusEnum.APPROVE.getStatus(), getTaskStatus(taskId));
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testCreateSignTask_delegatedBeforeSignWaitUserRepeat() {
        // 准备参数：用户 1 委派给用户 2，用户 2 向前加签给用户 3
        String taskId = getRunningTask(startProcessInstance()).getId();
        delegateTask(1L, taskId, 2L);
        Task childTask = createSignTask(2L, taskId, BpmTaskSignTypeEnum.BEFORE, 3L).get(0);

        // 调用：用户 3 加签给等待中的用户 2，并断言异常
        assertServiceException(() -> createSignTask(3L, childTask.getId(), BpmTaskSignTypeEnum.BEFORE, 2L),
                TASK_SIGN_CREATE_USER_REPEAT, "用户2");
    }

    @Test
    public void testApproveTask_delegatedAfterSignKeepReturnFlag() {
        // 准备参数：task1 配置审批人与发起人相同时自动跳过；task2 退回到 task1
        String processInstanceId = startProcessInstance();
        approveTask(1L, getRunningTask(processInstanceId).getId());
        Task task2 = getRunningTask(processInstanceId);
        UserTask task1Element = (UserTask) repositoryService.getBpmnModel(task2.getProcessDefinitionId()).getFlowElement("task1");
        BpmnModelUtils.addAssignStartUserHandlerType(BpmUserTaskAssignStartUserHandlerTypeEnum.SKIP.getType(), task1Element);
        taskUpdateService.returnTask(1L, new BpmTaskReturnReqVO().setId(task2.getId())
                .setTargetTaskDefinitionKey("task1").setReason("退回"));
        Task task1 = getRunningTask(processInstanceId);
        assertEquals("task1", task1.getTaskDefinitionKey());
        String returnFlag = String.format(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_RETURN_FLAG, task1.getTaskDefinitionKey());
        // 准备参数：用户 1（发起人）委派给用户 2，用户 2 向后加签给用户 3
        delegateTask(1L, task1.getId(), 2L);
        Task childTask = createSignTask(2L, task1.getId(), BpmTaskSignTypeEnum.AFTER, 3L).get(0);

        // 调用：用户 2 审批，用户 3 审批加签任务
        approveTask(2L, task1.getId());
        approveTask(3L, childTask.getId());

        // 断言：加签任务完成时保留退回标记，归还给用户 1 时没有被自动跳过
        assertEquals(true, runtimeService.getVariable(processInstanceId, returnFlag));
        assertRunningTask(task1.getId(), "1");
        // 调用：用户 1 审批
        approveTask(1L, task1.getId());
        // 断言：节点完成时，清理退回标记
        assertFalse(runtimeService.hasVariable(processInstanceId, returnFlag));
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testApproveTask_delegatedAfterSignCommentUser() {
        // 准备参数：评论真实写入 Flowable；用户 1 委派给用户 2，用户 2 向后加签给用户 3
        persistComments();
        String processInstanceId = startProcessInstance();
        String taskId = getRunningTask(processInstanceId).getId();
        delegateTask(1L, taskId, 2L);
        Task childTask = createSignTask(2L, taskId, BpmTaskSignTypeEnum.AFTER, 3L).get(0);

        // 调用：用户 2 审批
        approveTaskAuthenticated(2L, taskId, "用户 2 的意见");
        // 断言：用户 2 的审批意见已保存
        assertEquals("2", getComment(taskId, BpmCommentTypeEnum.APPROVE).getUserId());

        // 调用：用户 3 审批加签任务，触发归还
        approveTaskAuthenticated(3L, childTask.getId(), "同意");
        // 断言：归还记录属于用户 2，加签任务的审批意见属于用户 3
        assertEquals("2", getComment(taskId, BpmCommentTypeEnum.DELEGATE_END).getUserId());
        assertEquals("3", getComment(childTask.getId(), BpmCommentTypeEnum.APPROVE).getUserId());
    }

    @Test
    public void testRejectTask_delegatedAfterSignKeepDelegateReason() {
        // 准备参数：评论真实写入 Flowable；用户 1 委派给用户 2，用户 2 向后加签给用户 3，用户 2 已审批
        persistComments();
        String processInstanceId = startProcessInstance();
        String taskId = getRunningTask(processInstanceId).getId();
        delegateTask(1L, taskId, 2L);
        Task childTask = createSignTask(2L, taskId, BpmTaskSignTypeEnum.AFTER, 3L).get(0);
        approveTaskAuthenticated(2L, taskId, "用户 2 的意见");

        // 调用：用户 3 审批不通过
        FlowableUtils.executeAuthenticatedUserId(3L, () -> {
            taskUpdateService.rejectTask(3L, new BpmTaskRejectReqVO().setId(childTask.getId()).setReason("不同意"));
            return null;
        });

        // 断言：流程不通过，用户 2 的审批意见仍然保留
        assertProcessInstanceRejected(processInstanceId);
        Comment comment = getComment(taskId, BpmCommentTypeEnum.APPROVE);
        assertEquals("2", comment.getUserId());
        assertEquals(BpmCommentTypeEnum.APPROVE.formatComment("用户 2 的意见"), comment.getFullMessage());
    }

    // ========== 私有方法 ==========

    private void delegateTask(Long userId, String taskId, Long delegateUserId) {
        taskUpdateService.delegateTask(userId, new BpmTaskDelegateReqVO().setId(taskId)
                .setDelegateUserId(delegateUserId).setReason("委派"));
    }

    /**
     * 以指定用户作为 Flowable 认证用户审批，并断言审批后恢复原认证用户
     */
    private void approveTaskAuthenticated(Long userId, String taskId, String reason) {
        FlowableUtils.executeAuthenticatedUserId(userId, () -> {
            taskUpdateService.approveTask(userId, new BpmTaskApproveReqVO().setId(taskId).setReason(reason));
            assertEquals(String.valueOf(userId), Authentication.getAuthenticatedUserId());
            return null;
        });
    }

    /**
     * 评论 Mock 委托给真实的 {@link BpmCommentServiceImpl}，写入 Flowable 评论表
     */
    private void persistComments() {
        BpmCommentServiceImpl commentServiceImpl = new BpmCommentServiceImpl();
        ReflectionTestUtils.setField(commentServiceImpl, "taskService", flowableTaskService);
        doAnswer(delegatesTo(commentServiceImpl)).when(commentService)
                .createComment(anyString(), anyString(), any(), any(Object[].class));
    }

    private Comment getComment(String taskId, BpmCommentTypeEnum type) {
        List<Comment> comments = flowableTaskService.getTaskComments(taskId, type.getType());
        assertEquals(1, comments.size());
        return comments.get(0);
    }

    private String startProcessInstance() {
        return processInstanceUpdateService.createProcessInstance(1L, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey("twoStepApprove").setBusinessKey("business-1"));
    }

    private Task getRunningTask(String processInstanceId) {
        List<Task> tasks = taskQueryService.getRunningTaskListByProcessInstanceId(processInstanceId, null, null);
        assertEquals(1, tasks.size());
        return tasks.get(0);
    }

    private List<Task> createSignTask(Long userId, String taskId, BpmTaskSignTypeEnum type, Long... userIds) {
        taskUpdateService.createSignTask(userId, new BpmTaskSignCreateReqVO().setId(taskId)
                .setType(type.getType()).setUserIds(Set.of(userIds)).setReason("加签"));
        List<Task> childTasks = taskQueryService.getTaskListByParentTaskId(taskId);
        assertEquals(userIds.length, childTasks.size());
        return childTasks;
    }

    private void approveTask(Long userId, String taskId) {
        taskUpdateService.approveTask(userId, new BpmTaskApproveReqVO().setId(taskId).setReason("同意"));
    }

    private Integer getTaskStatus(String taskId) {
        return (Integer) taskQueryService.getHistoricTask(taskId).getTaskLocalVariables()
                .get(BpmnVariableConstants.TASK_VARIABLE_STATUS);
    }

    /**
     * 断言流程实例不通过并结束，并且没有残留的任务（包括没有 execution 的加签任务）
     */
    private void assertProcessInstanceRejected(String processInstanceId) {
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
        assertEquals(BpmProcessInstanceStatusEnum.REJECT.getStatus(), processInstanceQueryService
                .getHistoricProcessInstance(processInstanceId).getProcessVariables()
                .get(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
        assertEquals(0, flowableTaskService.createTaskQuery().deploymentId(deployment.getId()).count());
    }

    /**
     * 断言任务处于等待中：只有 owner 没有 assignee，状态为 WAIT
     */
    private void assertWaitTask(String taskId, String owner, BpmTaskSignTypeEnum signType) {
        Task task = taskQueryService.getTask(taskId);
        assertEquals(owner, task.getOwner());
        assertNull(task.getAssignee());
        assertEquals(signType != null ? signType.getType() : null, task.getScopeType());
        assertEquals(BpmTaskStatusEnum.WAIT.getStatus(), getTaskStatus(taskId));
    }

    /**
     * 断言任务可以被 assignee 审批：状态为 RUNNING
     */
    private void assertRunningTask(String taskId, String assignee) {
        Task task = taskQueryService.getTask(taskId);
        assertEquals(assignee, task.getAssignee());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), getTaskStatus(taskId));
    }

}
