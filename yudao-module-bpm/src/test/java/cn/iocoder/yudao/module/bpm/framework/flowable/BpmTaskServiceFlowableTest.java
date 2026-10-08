package cn.iocoder.yudao.module.bpm.framework.flowable;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCancelReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.*;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmAutoApproveTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskApproveTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskTimeoutHandlerTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmAttachmentTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmCommentTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmReasonEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskSignTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskServiceImpl;
import jakarta.annotation.Resource;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Attachment;
import org.flowable.job.api.Job;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link BpmTaskServiceImpl}、{@link BpmProcessInstanceServiceImpl} 的 Flowable 集成测试
 *
 * @author HUIHUI
 */
@RecordApplicationEvents
public class BpmTaskServiceFlowableTest extends BaseFlowableUnitTest {

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private HistoryService historyService;
    @Resource
    private TaskService flowableTaskService;
    @Resource
    private ManagementService managementService;

    @Resource
    private BpmTaskServiceImpl bpmTaskService;
    @Resource
    private BpmProcessInstanceServiceImpl processInstanceService;

    @Resource
    private ApplicationEvents applicationEvents;

    private Deployment deployment;
    /**
     * 单个测试额外部署的流程，在 {@link #tearDown()} 中先于 {@link #deployment} 删除
     */
    private final List<String> extraDeploymentIds = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        // 部署两级审批流程：task1 -> task2，task2 驳回时退回到 task1；审批人统一由测试上下文计算为用户 1
        deployment = repositoryService.createDeployment()
                .addClasspathResource("bpmn/two-step-approve.bpmn20.xml")
                .deploy();

        // 依赖的业务 Service 使用 Mock（委托给真实 Flowable 引擎），Flowable 相关 Service、BPM 任务/流程实例 Service 使用 Spring 中的真实 Bean
        mockFlowableBackedServices(repositoryService);
    }

    @AfterEach
    public void tearDown() {
        // 清理测试数据：加签子任务没有 execution，需要先删除，否则 Flowable 级联删除流程实例时会空指针
        List<String> signTaskIds = flowableTaskService.createTaskQuery().deploymentId(deployment.getId()).list()
                .stream().filter(task -> task.getExecutionId() == null).map(Task::getId).toList();
        flowableTaskService.deleteTasks(signTaskIds);
        extraDeploymentIds.forEach(id -> repositoryService.deleteDeployment(id, true));
        repositoryService.deleteDeployment(deployment.getId(), true);
    }

    @Test
    public void testTriggerTask_success() {
        // 准备参数：流程停留在 receiveTask 等待节点
        deploy("bpmn/trigger-task.bpmn20.xml");
        String processInstanceId = startProcessInstance("triggerTask");
        assertNotNull(runtimeService.createExecutionQuery().processInstanceId(processInstanceId)
                .activityId("wait").singleResult());

        // 调用
        bpmTaskService.triggerTask(processInstanceId, "wait");
        // 断言：触发等待节点后，流程结束
        assertNull(processInstanceService.getProcessInstance(processInstanceId));
        assertNotNull(processInstanceService.getHistoricProcessInstance(processInstanceId).getEndTime());
    }

    @Test
    public void testTriggerTask_executionNotExists() {
        // 准备参数
        String processInstanceId = startProcessInstance();

        // 调用：触发不存在的等待节点
        bpmTaskService.triggerTask(processInstanceId, "notExists");
        // 断言：流程实例不受影响
        assertEquals("task1", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testApproveTask_success() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        assertEquals("1", task1.getAssignee());

        // 调用：审批 task1
        bpmTaskService.approveTask(1L, new BpmTaskApproveReqVO().setId(task1.getId()).setReason("同意"));
        // 断言：task1 审批通过，流程流转到 task2
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.APPROVE.getStatus(), "同意");
        verify(commentService).createComment(eq(task1.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.APPROVE), eq("同意"));
        Task task2 = getRunningTask(processInstanceId);
        assertEquals("task2", task2.getTaskDefinitionKey());

        // 调用：审批 task2
        bpmTaskService.approveTask(1L, new BpmTaskApproveReqVO().setId(task2.getId()).setReason("同意"));
        // 断言：流程实例结束
        assertNull(processInstanceService.getProcessInstance(processInstanceId));
        assertNotNull(processInstanceService.getHistoricProcessInstance(processInstanceId).getEndTime());
        List<HistoricTaskInstance> tasks = bpmTaskService.getTaskListByProcessInstanceId(processInstanceId, true);
        assertEquals(List.of("task1", "task2"), tasks.stream().map(HistoricTaskInstance::getTaskDefinitionKey).toList());
    }

    @Test
    public void testApproveTask_withSignAndAttachment() {
        // 准备参数：task1 开启签名
        doAnswer(invocation -> {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(invocation.getArgument(0));
            BpmnModelUtils.addExtensionElement(bpmnModel.getFlowElement("task1"), BpmnModelConstants.SIGN_ENABLE, "true");
            return bpmnModel;
        }).when(modelService).getBpmnModelByDefinitionId(anyString());
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用
        bpmTaskService.approveTask(1L, new BpmTaskApproveReqVO().setId(task1.getId()).setReason("同意")
                .setSignPicUrl("https://www.iocoder.cn/sign.png")
                .setAttachments(List.of("https://www.iocoder.cn/files/a.pdf"))
                .setVariables(Map.of("day", 3)));
        // 断言：签名、表单变量保存到任务上，表单变量同步到流程实例
        HistoricTaskInstance historicTask = bpmTaskService.getHistoricTask(task1.getId());
        assertEquals("https://www.iocoder.cn/sign.png",
                historicTask.getTaskLocalVariables().get(BpmnVariableConstants.TASK_SIGN_PIC_URL));
        assertEquals(3, historicTask.getTaskLocalVariables().get("day"));
        assertEquals(3, runtimeService.getVariable(processInstanceId, "day"));
        // 断言：附件
        List<Attachment> attachments = bpmTaskService.getAttachments(processInstanceId, Set.of(task1.getId()),
                BpmAttachmentTypeEnum.TASK_ATTACHMENT);
        assertEquals(1, attachments.size());
        assertEquals("a.pdf", attachments.get(0).getName());
        assertEquals("https://www.iocoder.cn/files/a.pdf", attachments.get(0).getUrl());
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testApproveTask_assigneeNotSelf() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用，并断言异常
        assertServiceException(() -> bpmTaskService.approveTask(2L,
                new BpmTaskApproveReqVO().setId(task1.getId()).setReason("同意")), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
    }

    @Test
    public void testRejectTask_finishProcessInstance() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用：task1 未配置驳回策略，默认结束流程
        bpmTaskService.rejectTask(1L, new BpmTaskRejectReqVO().setId(task1.getId()).setReason("不同意")
                .setAttachments(List.of("https://www.iocoder.cn/files/b.pdf")));
        // 断言：任务不通过，附件保存到任务上
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.REJECT.getStatus(), "不同意");
        List<Attachment> attachments = bpmTaskService.getAttachments(processInstanceId, Set.of(task1.getId()),
                BpmAttachmentTypeEnum.TASK_ATTACHMENT);
        assertEquals(List.of("b.pdf"), attachments.stream().map(Attachment::getName).toList());
        // 断言：流程实例不通过并结束
        assertNull(processInstanceService.getProcessInstance(processInstanceId));
        assertEquals(BpmProcessInstanceStatusEnum.REJECT.getStatus(), getHistoricProcessVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
        assertEquals("审批不通过任务，原因：不同意", getHistoricProcessVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_REASON));
    }

    @Test
    public void testRejectTask_returnUserTask() {
        // 准备参数：task1 审批通过后，流转到 task2
        String processInstanceId = startProcessInstance();
        approveRunningTask(processInstanceId);
        Task task2 = getRunningTask(processInstanceId);

        // 调用：task2 配置驳回到 task1
        bpmTaskService.rejectTask(1L, new BpmTaskRejectReqVO().setId(task2.getId()).setReason("退回修改"));
        // 断言：task2 被标记为退回，流程回到 task1
        assertTaskStatus(task2.getId(), BpmTaskStatusEnum.RETURN.getStatus(), "退回修改");
        Task task1 = getRunningTask(processInstanceId);
        assertEquals("task1", task1.getTaskDefinitionKey());
        assertEquals(Boolean.TRUE, runtimeService.getVariable(processInstanceId,
                String.format(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_RETURN_FLAG, "task1")));
        assertEquals(Set.of("task2"), runtimeService.getVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_NEED_SIMULATE_TASK_IDS));

        // 调用：再次审批 task1
        bpmTaskService.approveTask(1L, new BpmTaskApproveReqVO().setId(task1.getId()).setReason("已修改"));
        // 断言：退回标记被清理，流程再次流转到 task2
        assertFalse(runtimeService.hasVariable(processInstanceId,
                String.format(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_RETURN_FLAG, "task1")));
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testRejectTask_signChildTask() {
        // 准备参数：task1 向前加签给用户 2
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        bpmTaskService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                .setType(BpmTaskSignTypeEnum.BEFORE.getType()).setUserIds(Set.of(2L)).setReason("加签"));
        Task childTask = bpmTaskService.getTaskListByParentTaskId(task1.getId()).get(0);

        // 调用：用户 2 审批不通过加签任务
        bpmTaskService.rejectTask(2L, new BpmTaskRejectReqVO().setId(childTask.getId()).setReason("不同意"));
        // 断言：加签任务、根任务都标记为不通过，流程实例不通过并结束
        assertTaskStatus(childTask.getId(), BpmTaskStatusEnum.REJECT.getStatus(), "不同意");
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.REJECT.getStatus(),
                BpmCommentTypeEnum.REJECT.formatComment("加签任务不通过"));
        verify(commentService).createComment(eq(task1.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.REJECT), eq("加签任务不通过"));
        assertNull(processInstanceService.getProcessInstance(processInstanceId));
        assertEquals(BpmProcessInstanceStatusEnum.REJECT.getStatus(), getHistoricProcessVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
    }

    @Test
    public void testGetUserTaskListByReturn_success() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        assertTrue(bpmTaskService.getUserTaskListByReturn(getRunningTask(processInstanceId).getId()).isEmpty());
        approveRunningTask(processInstanceId);
        Task task2 = getRunningTask(processInstanceId);

        // 调用
        List<UserTask> userTasks = bpmTaskService.getUserTaskListByReturn(task2.getId());
        // 断言
        assertEquals(List.of("task1"), userTasks.stream().map(UserTask::getId).toList());
    }

    @Test
    public void testReturnTask_success() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        approveRunningTask(processInstanceId);
        Task task2 = getRunningTask(processInstanceId);

        // 调用
        bpmTaskService.returnTask(1L, new BpmTaskReturnReqVO().setId(task2.getId())
                .setTargetTaskDefinitionKey("task1").setReason("退回"));
        // 断言
        assertTaskStatus(task2.getId(), BpmTaskStatusEnum.RETURN.getStatus(), "退回");
        verify(commentService).createComment(eq(task2.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.RETURN), eq("退回"));
        assertEquals("task1", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testReturnTask_targetNotExists() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用，并断言异常
        assertServiceException(() -> bpmTaskService.returnTask(1L, new BpmTaskReturnReqVO().setId(task1.getId())
                .setTargetTaskDefinitionKey("notExists").setReason("退回")), TASK_TARGET_NODE_NOT_EXISTS);
    }

    @Test
    public void testReturnTask_sourceTargetError() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用，并断言异常：task2 位于 task1 之后，不能退回
        assertServiceException(() -> bpmTaskService.returnTask(1L, new BpmTaskReturnReqVO().setId(task1.getId())
                .setTargetTaskDefinitionKey("task2").setReason("退回")), TASK_RETURN_FAIL_SOURCE_TARGET_ERROR);
    }

    @Test
    public void testDelegateTask_thenApprove() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用：委派给用户 2
        bpmTaskService.delegateTask(1L, new BpmTaskDelegateReqVO().setId(task1.getId())
                .setDelegateUserId(2L).setReason("请帮忙审批"));
        // 断言
        Task delegatedTask = bpmTaskService.getTask(task1.getId());
        assertEquals("1", delegatedTask.getOwner());
        assertEquals("2", delegatedTask.getAssignee());
        assertEquals(DelegationState.PENDING, delegatedTask.getDelegationState());
        verify(commentService).createComment(eq(task1.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.DELEGATE_START), eq("用户1"), eq("用户2"), eq("请帮忙审批"));

        // 调用：被委派人审批，任务归还给原审批人
        bpmTaskService.approveTask(2L, new BpmTaskApproveReqVO().setId(task1.getId()).setReason("已处理"));
        // 断言：任务仍在 task1，审批人恢复为用户 1
        Task resolvedTask = bpmTaskService.getTask(task1.getId());
        assertEquals("1", resolvedTask.getAssignee());
        assertEquals(DelegationState.RESOLVED, resolvedTask.getDelegationState());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(),
                resolvedTask.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_STATUS));
    }

    @Test
    public void testDelegateTask_redelegate() {
        // 准备参数：用户 1 委派给用户 2
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        bpmTaskService.delegateTask(1L, new BpmTaskDelegateReqVO().setId(task1.getId())
                .setDelegateUserId(2L).setReason("请帮忙审批"));

        // 调用：用户 2 再委派给用户 3
        bpmTaskService.delegateTask(2L, new BpmTaskDelegateReqVO().setId(task1.getId())
                .setDelegateUserId(3L).setReason("转给用户 3"));
        // 断言：owner 仍为最初的审批人
        Task delegatedTask = bpmTaskService.getTask(task1.getId());
        assertEquals("1", delegatedTask.getOwner());
        assertEquals("3", delegatedTask.getAssignee());
        verify(commentService).createComment(eq(task1.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.DELEGATE_START), eq("用户2"), eq("用户3"), eq("转给用户 3"));

        // 调用：用户 3 审批，任务归还给用户 1
        bpmTaskService.approveTask(3L, new BpmTaskApproveReqVO().setId(task1.getId()).setReason("已处理"));
        // 断言
        assertEquals("1", bpmTaskService.getTask(task1.getId()).getAssignee());
        verify(commentService).createComment(eq(task1.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.DELEGATE_END), any(), eq("用户1"), eq("已处理"));
    }

    @Test
    public void testDelegateTask_userRepeat() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用，并断言异常
        assertServiceException(() -> bpmTaskService.delegateTask(1L, new BpmTaskDelegateReqVO().setId(task1.getId())
                .setDelegateUserId(1L).setReason("委派")), TASK_DELEGATE_FAIL_USER_REPEAT);
    }

    @Test
    public void testTransferTask_success() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用
        bpmTaskService.transferTask(1L, new BpmTaskTransferReqVO().setId(task1.getId())
                .setAssigneeUserId(2L).setReason("转办"));
        // 断言
        Task transferredTask = bpmTaskService.getTask(task1.getId());
        assertEquals("1", transferredTask.getOwner());
        assertEquals("2", transferredTask.getAssignee());
        assertNull(transferredTask.getDelegationState());
        verify(commentService).createComment(eq(task1.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.TRANSFER), eq("用户1"), eq("用户2"), eq("转办"));
    }

    @Test
    public void testTransferTask_emptyAssignee() {
        // 准备参数：task1 计算不到审批人，且未配置自动处理
        when(taskCandidateInvoker.calculateUsersByTask(any())).thenReturn(Set.of());
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        assertNull(task1.getAssignee());

        // 调用：转办给用户 2
        bpmTaskService.transferTask(1L, new BpmTaskTransferReqVO().setId(task1.getId())
                .setAssigneeUserId(2L).setReason("转办"));
        // 断言：用户 2 成为审批人
        Task transferredTask = bpmTaskService.getTask(task1.getId());
        assertEquals("2", transferredTask.getAssignee());
        assertNull(transferredTask.getOwner());
        // 调用 + 断言：用户 2 审批通过，流程流转到 task2
        when(taskCandidateInvoker.calculateUsersByTask(any())).thenReturn(Set.of(1L));
        bpmTaskService.approveTask(2L, new BpmTaskApproveReqVO().setId(task1.getId()).setReason("同意"));
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testCreateSignTask_before_thenApproveChild() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用：向前加签给用户 2
        bpmTaskService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                .setType(BpmTaskSignTypeEnum.BEFORE.getType()).setUserIds(Set.of(2L)).setReason("加签"));
        // 断言：父任务等待子任务审批，子任务分配给用户 2
        Task parentTask = bpmTaskService.getTask(task1.getId());
        assertNull(parentTask.getAssignee());
        assertEquals("1", parentTask.getOwner());
        assertEquals(BpmTaskSignTypeEnum.BEFORE.getType(), parentTask.getScopeType());
        assertEquals(BpmTaskStatusEnum.WAIT.getStatus(),
                parentTask.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_STATUS));
        List<Task> childTasks = bpmTaskService.getTaskListByParentTaskId(task1.getId());
        assertEquals(1, childTasks.size());
        assertEquals("2", childTasks.get(0).getAssignee());
        verify(commentService).createComment(eq(task1.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.ADD_SIGN), eq("用户1"), eq("向前加签"), eq("用户2"), eq("加签"));

        // 调用：用户 2 审批子任务
        bpmTaskService.approveTask(2L, new BpmTaskApproveReqVO().setId(childTasks.get(0).getId()).setReason("同意"));
        // 断言：父任务恢复给用户 1 审批
        Task resolvedTask = bpmTaskService.getTask(task1.getId());
        assertEquals("1", resolvedTask.getAssignee());
        assertNull(resolvedTask.getScopeType());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(),
                resolvedTask.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_STATUS));
        assertTrue(bpmTaskService.getTaskListByParentTaskId(task1.getId()).isEmpty());
    }

    @Test
    public void testCreateSignTask_after_thenApprove() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用：向后加签给用户 2
        bpmTaskService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                .setType(BpmTaskSignTypeEnum.AFTER.getType()).setUserIds(Set.of(2L)).setReason("加签"));
        // 断言：子任务暂由用户 2 拥有，等待父任务审批
        Task childTask = bpmTaskService.getTaskListByParentTaskId(task1.getId()).get(0);
        assertEquals("2", childTask.getOwner());
        assertNull(childTask.getAssignee());
        assertEquals(BpmTaskStatusEnum.WAIT.getStatus(), getTaskStatus(childTask.getId()));

        // 调用：用户 1 审批父任务，激活子任务
        bpmTaskService.approveTask(1L, new BpmTaskApproveReqVO().setId(task1.getId()).setReason("同意"));
        // 断言：父任务处于审批通过中，子任务分配给用户 2
        assertEquals(BpmTaskStatusEnum.APPROVING.getStatus(), getTaskStatus(task1.getId()));
        assertEquals("2", bpmTaskService.getTask(childTask.getId()).getAssignee());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), getTaskStatus(childTask.getId()));

        // 调用：用户 2 审批子任务
        bpmTaskService.approveTask(2L, new BpmTaskApproveReqVO().setId(childTask.getId()).setReason("同意"));
        // 断言：父任务自动完成，流程流转到 task2
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.APPROVE.getStatus(), "同意");
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testCreateSignTask_typeError() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        bpmTaskService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                .setType(BpmTaskSignTypeEnum.AFTER.getType()).setUserIds(Set.of(2L)).setReason("加签"));

        // 调用，并断言异常
        assertServiceException(() -> bpmTaskService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                        .setType(BpmTaskSignTypeEnum.BEFORE.getType()).setUserIds(Set.of(3L)).setReason("加签")),
                TASK_SIGN_CREATE_TYPE_ERROR, "向后加签", "向前加签");
    }

    @Test
    public void testCreateSignTask_userRepeat() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用，并断言异常：用户 1 已经是当前节点的审批人
        assertServiceException(() -> bpmTaskService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                        .setType(BpmTaskSignTypeEnum.BEFORE.getType()).setUserIds(Set.of(1L)).setReason("加签")),
                TASK_SIGN_CREATE_USER_REPEAT, "用户1");
    }

    @Test
    public void testDeleteSignTask_success() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        bpmTaskService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                .setType(BpmTaskSignTypeEnum.BEFORE.getType()).setUserIds(Set.of(2L)).setReason("加签"));
        Task childTask = bpmTaskService.getTaskListByParentTaskId(task1.getId()).get(0);

        // 调用
        bpmTaskService.deleteSignTask(1L, new BpmTaskSignDeleteReqVO().setId(childTask.getId()).setReason("减签"));
        // 断言：子任务被取消删除，父任务恢复给用户 1 审批
        assertNull(bpmTaskService.getTask(childTask.getId()));
        assertEquals(BpmTaskStatusEnum.CANCEL.getStatus(), getTaskStatus(childTask.getId()));
        Task parentTask = bpmTaskService.getTask(task1.getId());
        assertEquals("1", parentTask.getAssignee());
        assertNull(parentTask.getScopeType());
        verify(commentService).createComment(eq(task1.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.SUB_SIGN), eq("用户1"), eq("用户2"));
    }

    @Test
    public void testDeleteSignTask_partial() {
        // 准备参数：task1 向前加签给用户 2、用户 3
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        bpmTaskService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                .setType(BpmTaskSignTypeEnum.BEFORE.getType()).setUserIds(Set.of(2L, 3L)).setReason("加签"));
        Map<String, Task> childTasks = bpmTaskService.getTaskListByParentTaskId(task1.getId()).stream()
                .collect(Collectors.toMap(Task::getAssignee, Function.identity()));

        // 调用：减签用户 2
        bpmTaskService.deleteSignTask(1L, new BpmTaskSignDeleteReqVO().setId(childTasks.get("2").getId())
                .setReason("减签"));
        // 断言：用户 3 的加签任务仍在，父任务继续等待
        assertEquals(BpmTaskStatusEnum.CANCEL.getStatus(), getTaskStatus(childTasks.get("2").getId()));
        assertEquals(List.of(childTasks.get("3").getId()), bpmTaskService.getTaskListByParentTaskId(task1.getId())
                .stream().map(Task::getId).toList());
        Task parentTask = bpmTaskService.getTask(task1.getId());
        assertNull(parentTask.getAssignee());
        assertEquals(BpmTaskSignTypeEnum.BEFORE.getType(), parentTask.getScopeType());

        // 调用 + 断言：用户 3 审批通过后，父任务恢复给用户 1 审批
        bpmTaskService.approveTask(3L, new BpmTaskApproveReqVO().setId(childTasks.get("3").getId()).setReason("同意"));
        assertEquals("1", bpmTaskService.getTask(task1.getId()).getAssignee());
    }

    @Test
    public void testDeleteSignTask_afterSignNotApproved() {
        // 准备参数：task1 向后加签给用户 2，但 task1 尚未审批
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        bpmTaskService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                .setType(BpmTaskSignTypeEnum.AFTER.getType()).setUserIds(Set.of(2L)).setReason("加签"));
        Task childTask = bpmTaskService.getTaskListByParentTaskId(task1.getId()).get(0);

        // 调用
        bpmTaskService.deleteSignTask(1L, new BpmTaskSignDeleteReqVO().setId(childTask.getId()).setReason("减签"));
        // 断言：父任务不会因为加签任务被减签而自动完成，仍由用户 1 审批
        Task parentTask = getRunningTask(processInstanceId);
        assertEquals(task1.getId(), parentTask.getId());
        assertEquals("1", parentTask.getAssignee());
        assertNull(parentTask.getScopeType());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), getTaskStatus(task1.getId()));
    }

    @Test
    public void testDeleteSignTask_noParent() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用，并断言异常
        assertServiceException(() -> bpmTaskService.deleteSignTask(1L,
                new BpmTaskSignDeleteReqVO().setId(task1.getId()).setReason("减签")), TASK_SIGN_DELETE_NO_PARENT);
    }

    @Test
    public void testWithdrawTask_success() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        approveRunningTask(processInstanceId);
        Task task2 = getRunningTask(processInstanceId);

        // 调用
        bpmTaskService.withdrawTask(1L, task1.getId());
        // 断言：task2 被取消，流程回到 task1
        assertTaskStatus(task2.getId(), BpmTaskStatusEnum.CANCEL.getStatus(), "前一任务撤回，系统自动取消");
        verify(commentService).createComment(eq(task2.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.CANCEL), eq("前一节点撤回"));
        assertEquals("task1", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testWithdrawTask_taskNotExists() {
        // 准备参数：task1 尚未审批
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用，并断言异常
        assertServiceException(() -> bpmTaskService.withdrawTask(1L, task1.getId()), TASK_WITHDRAW_FAIL_TASK_NOT_EXISTS);
    }

    @Test
    public void testWithdrawTask_nextTaskApproved() {
        // 准备参数：task1、task2 均已审批，流程已经结束
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        approveRunningTask(processInstanceId);
        approveRunningTask(processInstanceId);

        // 调用，并断言异常
        assertServiceException(() -> bpmTaskService.withdrawTask(1L, task1.getId()),
                TASK_WITHDRAW_FAIL_PROCESS_NOT_RUNNING);
    }

    @Test
    public void testWithdrawTask_notAllow() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        approveRunningTask(processInstanceId);
        when(processDefinitionService.getProcessDefinitionInfo(anyString()))
                .thenReturn(new BpmProcessDefinitionInfoDO().setAllowWithdrawTask(false));

        // 调用，并断言异常
        assertServiceException(() -> bpmTaskService.withdrawTask(1L, task1.getId()), TASK_WITHDRAW_FAIL_NOT_ALLOW);
    }

    @Test
    public void testMoveTaskToEnd_success() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用
        bpmTaskService.moveTaskToEnd(processInstanceId, "结束流程");
        // 断言：运行中的任务被取消，流程实例结束
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.CANCEL.getStatus(), "系统自动取消");
        assertNull(processInstanceService.getProcessInstance(processInstanceId));
    }

    @Test
    public void testMoveTaskToEnd_withSignTask() {
        // 准备参数：task1 向前加签给用户 2，子任务未审批
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        bpmTaskService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                .setType(BpmTaskSignTypeEnum.BEFORE.getType()).setUserIds(Set.of(2L)).setReason("加签"));
        Task childTask = bpmTaskService.getTaskListByParentTaskId(task1.getId()).get(0);

        // 调用
        bpmTaskService.moveTaskToEnd(processInstanceId, "结束流程");
        // 断言：父任务、子任务都被取消，流程实例结束
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.CANCEL.getStatus(), "系统自动取消");
        assertTaskStatus(childTask.getId(), BpmTaskStatusEnum.CANCEL.getStatus(), "系统自动取消");
        assertNull(processInstanceService.getProcessInstance(processInstanceId));
        assertNull(bpmTaskService.getTask(childTask.getId()));
    }

    @Test
    public void testProcessTaskTimeout_approve() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用：task1 超时自动通过
        bpmTaskService.processTaskTimeout(processInstanceId, "task1",
                BpmUserTaskTimeoutHandlerTypeEnum.APPROVE.getType());
        // 断言
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.APPROVE.getStatus(), BpmReasonEnum.TIMEOUT_APPROVE.getReason());
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testProcessTaskTimeout_reject() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用：task1 超时自动拒绝
        bpmTaskService.processTaskTimeout(processInstanceId, "task1",
                BpmUserTaskTimeoutHandlerTypeEnum.REJECT.getType());
        // 断言：任务不通过，流程实例不通过并结束
        assertEquals(BpmTaskStatusEnum.REJECT.getStatus(), getTaskStatus(task1.getId()));
        assertNull(processInstanceService.getProcessInstance(processInstanceId));
        assertEquals(BpmProcessInstanceStatusEnum.REJECT.getStatus(), getHistoricProcessVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
    }

    // ========== 流程实例相关 ==========

    @Test
    public void testCreateProcessInstance_withTitle() {
        // 准备参数：开启标题设置
        BpmModelMetaInfoVO.TitleSetting titleSetting = new BpmModelMetaInfoVO.TitleSetting().setEnable(true)
                .setTitle("{" + BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_ID + "}的"
                        + "{" + BpmnVariableConstants.PROCESS_DEFINITION_NAME + "}，请假{day}天");
        doAnswer(invocation -> new BpmProcessDefinitionInfoDO().setProcessDefinitionId(invocation.getArgument(0))
                .setModelType(BpmModelTypeEnum.BPMN.getType()).setTitleSetting(titleSetting))
                .when(processDefinitionService).getProcessDefinitionInfo(anyString());
        String processDefinitionId = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId()).singleResult().getId();

        // 调用：模拟 FlowableWebFilter 设置发起人
        String processInstanceId = FlowableUtils.executeAuthenticatedUserId(1L,
                () -> processInstanceService.createProcessInstance(1L, new BpmProcessInstanceCreateReqVO()
                        .setProcessDefinitionId(processDefinitionId).setVariables(new HashMap<>(Map.of("day", 3)))));
        // 断言
        ProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
        assertEquals("用户1的两级审批，请假3天", processInstance.getName());
        assertEquals(3, runtimeService.getVariable(processInstanceId, "day"));
        assertEquals(1L, runtimeService.getVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_ID));
        assertEquals(BpmProcessInstanceStatusEnum.RUNNING.getStatus(), runtimeService.getVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
        assertEquals("task1", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testCancelProcessInstanceByStartUser_success() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用
        processInstanceService.cancelProcessInstanceByStartUser(1L, new BpmProcessInstanceCancelReqVO()
                .setId(processInstanceId).setReason("不想请了"));
        // 断言：流程实例取消并结束，运行中的任务被取消
        assertNull(processInstanceService.getProcessInstance(processInstanceId));
        assertEquals(BpmProcessInstanceStatusEnum.CANCEL.getStatus(), getHistoricProcessVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
        assertEquals(BpmReasonEnum.CANCEL_PROCESS_INSTANCE_BY_START_USER.format("不想请了"),
                getHistoricProcessVariable(processInstanceId, BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_REASON));
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.CANCEL.getStatus(), BpmReasonEnum.CANCEL_BY_SYSTEM.getReason());
    }

    @Test
    public void testCancelProcessInstanceByStartUser_notSelf() {
        // 准备参数
        String processInstanceId = startProcessInstance();

        // 调用，并断言异常
        assertServiceException(() -> processInstanceService.cancelProcessInstanceByStartUser(2L,
                new BpmProcessInstanceCancelReqVO().setId(processInstanceId).setReason("取消")),
                PROCESS_INSTANCE_CANCEL_FAIL_NOT_SELF);
        assertNotNull(processInstanceService.getProcessInstance(processInstanceId));
    }

    @Test
    public void testCancelProcessInstanceByAdmin_success() {
        // 准备参数：task1 已审批，流程处于 task2
        String processInstanceId = startProcessInstance();
        approveRunningTask(processInstanceId);

        // 调用
        processInstanceService.cancelProcessInstanceByAdmin(100L, new BpmProcessInstanceCancelReqVO()
                .setId(processInstanceId).setReason("流程配置错误"));
        // 断言
        assertNull(processInstanceService.getProcessInstance(processInstanceId));
        assertEquals(BpmProcessInstanceStatusEnum.CANCEL.getStatus(), getHistoricProcessVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
        assertEquals(BpmReasonEnum.CANCEL_PROCESS_INSTANCE_BY_ADMIN.format("用户100", "流程配置错误"),
                getHistoricProcessVariable(processInstanceId, BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_REASON));
    }

    @Test
    public void testUpdateAndRemoveProcessInstanceVariables_success() {
        // 准备参数
        String processInstanceId = startProcessInstance();

        // 调用 + 断言：更新变量
        processInstanceService.updateProcessInstanceVariables(processInstanceId, Map.of("day", 5));
        assertEquals(5, runtimeService.getVariable(processInstanceId, "day"));
        // 调用 + 断言：删除变量
        processInstanceService.removeProcessInstanceVariables(processInstanceId, Set.of("day"));
        assertFalse(runtimeService.hasVariable(processInstanceId, "day"));
    }

    @Test
    public void testGetTodoTask_success() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用：不传 taskId 时，按流程实例查询首个待办
        BpmTaskRespVO todoTask = bpmTaskService.getTodoTask(1L, null, processInstanceId);
        // 断言
        assertEquals(task1.getId(), todoTask.getId());
        assertEquals("一级审批", todoTask.getName());
        assertFalse(todoTask.getSignEnable());
        assertFalse(todoTask.getReasonRequire());
        // 断言：其他用户没有待办
        assertNull(bpmTaskService.getTodoTask(2L, task1.getId(), processInstanceId));
    }

    @Test
    public void testGetTaskTodoPageAndDonePage_success() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        approveRunningTask(processInstanceId);
        BpmTaskPageReqVO pageReqVO = new BpmTaskPageReqVO();
        pageReqVO.setName("审批");
        pageReqVO.setProcessDefinitionKey("twoStepApprove");

        // 调用 + 断言：待办为 task2
        List<Task> todoTasks = bpmTaskService.getTaskTodoPage(1L, pageReqVO).getList();
        assertEquals(List.of("task2"), todoTasks.stream().map(Task::getTaskDefinitionKey).toList());
        // 调用 + 断言：已办为 task1
        pageReqVO.setStatus(BpmTaskStatusEnum.APPROVE.getStatus());
        List<HistoricTaskInstance> doneTasks = bpmTaskService.getTaskDonePage(1L, pageReqVO).getList();
        assertEquals(List.of("task1"), doneTasks.stream().map(HistoricTaskInstance::getTaskDefinitionKey).toList());
        // 调用 + 断言：其它用户没有待办
        assertEquals(0, bpmTaskService.getTaskTodoPage(2L, new BpmTaskPageReqVO()).getTotal());
    }

    // ========== 状态回调相关（依赖 BpmTaskEventListener、BpmProcessInstanceEventListener） ==========

    @Test
    public void testProcessCallback_approve() {
        // 调用：发起流程
        String processInstanceId = startProcessInstance();
        // 断言：任务创建回调将 task1 置为审批中，任务分配回调发送待办消息
        Task task1 = getRunningTask(processInstanceId);
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), getTaskStatus(task1.getId()));
        verify(messageService).sendMessageWhenTaskAssigned(argThat(reqDTO ->
                task1.getId().equals(reqDTO.getTaskId()) && reqDTO.getAssigneeUserId().equals(1L)
                        && reqDTO.getStartUserId().equals(1L) && "用户1".equals(reqDTO.getStartUserNickname())));

        // 调用：task1、task2 依次审批通过
        approveRunningTask(processInstanceId);
        approveRunningTask(processInstanceId);
        // 断言：流程完成回调将状态更新为审批通过，并发送消息、状态事件
        assertEquals(BpmProcessInstanceStatusEnum.APPROVE.getStatus(), getHistoricProcessVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
        verify(messageService, times(2)).sendMessageWhenTaskAssigned(any());
        verify(messageService).sendMessageWhenProcessInstanceApprove(argThat(reqDTO ->
                processInstanceId.equals(reqDTO.getProcessInstanceId()) && reqDTO.getStartUserId().equals(1L)));
        BpmProcessInstanceStatusEvent event = getProcessInstanceStatusEvent(processInstanceId);
        assertEquals(BpmProcessInstanceStatusEnum.APPROVE.getStatus(), event.getStatus());
        assertEquals("twoStepApprove", event.getProcessDefinitionKey());
        assertEquals("business-1", event.getBusinessKey());
    }

    @Test
    public void testProcessCallback_reject() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用
        bpmTaskService.rejectTask(1L, new BpmTaskRejectReqVO().setId(task1.getId()).setReason("不同意"));
        // 断言：流程完成回调发送不通过消息、状态事件，且不会误发通过消息
        verify(messageService).sendMessageWhenProcessInstanceReject(argThat(reqDTO ->
                processInstanceId.equals(reqDTO.getProcessInstanceId()) && "审批不通过任务，原因：不同意".equals(reqDTO.getReason())));
        verify(messageService, never()).sendMessageWhenProcessInstanceApprove(any());
        BpmProcessInstanceStatusEvent event = getProcessInstanceStatusEvent(processInstanceId);
        assertEquals(BpmProcessInstanceStatusEnum.REJECT.getStatus(), event.getStatus());
        assertEquals("审批不通过任务，原因：不同意", event.getReason());
    }

    @Test
    public void testProcessCallback_cancel() {
        // 准备参数
        String processInstanceId = startProcessInstance();

        // 调用
        processInstanceService.cancelProcessInstanceByStartUser(1L, new BpmProcessInstanceCancelReqVO()
                .setId(processInstanceId).setReason("不想请了"));
        // 断言：取消时只发送状态事件，不发送通过、不通过消息
        BpmProcessInstanceStatusEvent event = getProcessInstanceStatusEvent(processInstanceId);
        assertEquals(BpmProcessInstanceStatusEnum.CANCEL.getStatus(), event.getStatus());
        assertEquals(BpmReasonEnum.CANCEL_PROCESS_INSTANCE_BY_START_USER.format("不想请了"), event.getReason());
        verify(messageService, never()).sendMessageWhenProcessInstanceApprove(any());
        verify(messageService, never()).sendMessageWhenProcessInstanceReject(any());
    }

    // ========== 自动审批相关（依赖 getSelf() 获取 Spring 代理） ==========

    @Test
    public void testProcessTaskCreated_autoApprove() {
        // 准备参数：task1 配置为【自动通过】
        mockUserTaskExtensionElement("task1", BpmnModelConstants.USER_TASK_APPROVE_TYPE,
                BpmUserTaskApproveTypeEnum.AUTO_APPROVE.getType());
        mockTaskWithoutCandidate("task1");

        // 调用
        String processInstanceId = startProcessInstance();
        // 断言：task1 在事务结束后自动通过，流程流转到 task2
        HistoricTaskInstance task1 = getHistoricTask(processInstanceId, "task1");
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.APPROVE.getStatus(),
                BpmReasonEnum.APPROVE_TYPE_AUTO_APPROVE.getReason());
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testProcessTaskCreated_autoReject() {
        // 准备参数：task1 配置为【自动拒绝】
        mockUserTaskExtensionElement("task1", BpmnModelConstants.USER_TASK_APPROVE_TYPE,
                BpmUserTaskApproveTypeEnum.AUTO_REJECT.getType());
        mockTaskWithoutCandidate("task1");

        // 调用
        String processInstanceId = startProcessInstance();
        // 断言：task1 自动拒绝，流程实例不通过并结束
        HistoricTaskInstance task1 = getHistoricTask(processInstanceId, "task1");
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.REJECT.getStatus(),
                BpmReasonEnum.APPROVE_TYPE_AUTO_REJECT.getReason());
        assertNull(processInstanceService.getProcessInstance(processInstanceId));
        assertEquals(BpmProcessInstanceStatusEnum.REJECT.getStatus(), getHistoricProcessVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
    }

    @Test
    public void testProcessTaskAssigned_autoApproveAll() {
        // 准备参数：流程配置【仅审批一次，后续重复的审批节点均自动通过】
        mockAutoApprovalType(BpmAutoApproveTypeEnum.APPROVE_ALL);
        String processInstanceId = startProcessInstance();

        // 调用：用户 1 审批 task1
        approveRunningTask(processInstanceId);
        // 断言：task2 仍由用户 1 审批，自动通过，流程结束
        HistoricTaskInstance task2 = getHistoricTask(processInstanceId, "task2");
        assertTaskStatus(task2.getId(), BpmTaskStatusEnum.APPROVE.getStatus(), BpmAutoApproveTypeEnum.APPROVE_ALL.getName());
        assertNull(processInstanceService.getProcessInstance(processInstanceId));
        assertEquals(BpmProcessInstanceStatusEnum.APPROVE.getStatus(), getHistoricProcessVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
    }

    @Test
    public void testProcessTaskAssigned_autoApproveSequent() {
        // 准备参数：流程配置【仅针对连续审批的节点自动通过】
        mockAutoApprovalType(BpmAutoApproveTypeEnum.APPROVE_SEQUENT);
        String processInstanceId = startProcessInstance();

        // 调用：用户 1 审批 task1
        approveRunningTask(processInstanceId);
        // 断言：task2 的上一节点 task1 也由用户 1 审批，自动通过
        HistoricTaskInstance task2 = getHistoricTask(processInstanceId, "task2");
        assertTaskStatus(task2.getId(), BpmTaskStatusEnum.APPROVE.getStatus(),
                BpmAutoApproveTypeEnum.APPROVE_SEQUENT.getName());
        assertNull(processInstanceService.getProcessInstance(processInstanceId));
    }

    // ========== 定时器超时相关（依赖 BpmTaskEventListener#timerFired） ==========

    @Test
    public void testTimerFired_userTaskTimeoutApprove() {
        // 准备参数
        deploy("bpmn/timeout-approve.bpmn20.xml");
        String processInstanceId = startProcessInstance("timeoutApprove");
        Task task1 = getRunningTask(processInstanceId);

        // 调用：触发 task1 的超时定时器
        executeTimerJob(processInstanceId);
        // 断言：task1 超时自动通过，流程结束
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.APPROVE.getStatus(), BpmReasonEnum.TIMEOUT_APPROVE.getReason());
        assertNull(processInstanceService.getProcessInstance(processInstanceId));
        assertEquals(BpmProcessInstanceStatusEnum.APPROVE.getStatus(), getHistoricProcessVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
    }

    @Test
    public void testTimerFired_userTaskTimeoutReminder() {
        // 准备参数：超时处理方式改为【自动提醒】
        deploy("bpmn/timeout-approve.bpmn20.xml");
        mockBoundaryEventTimeoutHandlerType("task1-timeout", BpmUserTaskTimeoutHandlerTypeEnum.REMINDER);
        String processInstanceId = startProcessInstance("timeoutApprove");
        Task task1 = getRunningTask(processInstanceId);

        // 调用
        executeTimerJob(processInstanceId);
        // 断言：发送超时提醒，任务保持审批中
        verify(messageService).sendMessageWhenTaskTimeout(argThat(reqDTO ->
                task1.getId().equals(reqDTO.getTaskId()) && reqDTO.getAssigneeUserId().equals(1L)
                        && processInstanceId.equals(reqDTO.getProcessInstanceId())));
        assertEquals(task1.getId(), getRunningTask(processInstanceId).getId());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), getTaskStatus(task1.getId()));
    }

    @Test
    public void testTimerFired_userTaskTimeoutReject() {
        // 准备参数：超时处理方式改为【自动拒绝】
        deploy("bpmn/timeout-approve.bpmn20.xml");
        mockBoundaryEventTimeoutHandlerType("task1-timeout", BpmUserTaskTimeoutHandlerTypeEnum.REJECT);
        String processInstanceId = startProcessInstance("timeoutApprove");
        Task task1 = getRunningTask(processInstanceId);

        // 调用
        executeTimerJob(processInstanceId);
        // 断言：task1 超时自动拒绝，流程实例不通过并结束
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.REJECT.getStatus(), BpmReasonEnum.REJECT_TASK.getReason());
        assertNull(processInstanceService.getProcessInstance(processInstanceId));
        assertEquals(BpmProcessInstanceStatusEnum.REJECT.getStatus(), getHistoricProcessVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
    }

    @Test
    public void testTimerFired_delayTimer() {
        // 准备参数
        deploy("bpmn/delay-timer.bpmn20.xml");
        String processInstanceId = startProcessInstance("delayTimer");
        assertNotNull(runtimeService.createExecutionQuery().processInstanceId(processInstanceId)
                .activityId("wait").singleResult());

        // 调用：延迟器到期
        executeTimerJob(processInstanceId);
        // 断言：receiveTask 被触发，流程结束
        assertNull(processInstanceService.getProcessInstance(processInstanceId));
        assertEquals(BpmProcessInstanceStatusEnum.APPROVE.getStatus(), getHistoricProcessVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
    }

    // ========== 子流程相关 ==========

    @Test
    public void testTimerFired_childProcessTimeout() {
        // 准备参数：主流程发起 twoStepApprove 子流程
        deploy("bpmn/call-activity.bpmn20.xml");
        String parentId = startProcessInstance("callActivityParent");
        ProcessInstance child = getChildProcessInstance(parentId);
        Task childTask = getRunningTask(child.getId());

        // 调用：触发子流程的超时定时器
        executeTimerJob(parentId);
        // 断言：子流程被结束、任务被取消，主流程继续流转并结束
        assertTaskStatus(childTask.getId(), BpmTaskStatusEnum.CANCEL.getStatus(), BpmReasonEnum.CANCEL_BY_SYSTEM.getReason());
        assertNull(processInstanceService.getProcessInstance(child.getId()));
        assertNull(processInstanceService.getProcessInstance(parentId));
        assertEquals(BpmProcessInstanceStatusEnum.APPROVE.getStatus(), getHistoricProcessVariable(parentId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
    }

    @Test
    public void testRejectTask_childProcessRejectParent() {
        // 准备参数
        deploy("bpmn/call-activity.bpmn20.xml");
        String parentId = startProcessInstance("callActivityParent");
        ProcessInstance child = getChildProcessInstance(parentId);
        Task childTask = getRunningTask(child.getId());

        // 调用：子流程审批不通过
        bpmTaskService.rejectTask(1L, new BpmTaskRejectReqVO().setId(childTask.getId()).setReason("不同意"));
        // 断言：子流程不通过时，主流程同样标记为不通过并结束
        assertNull(processInstanceService.getProcessInstance(child.getId()));
        assertNull(processInstanceService.getProcessInstance(parentId));
        assertEquals(BpmProcessInstanceStatusEnum.REJECT.getStatus(), getHistoricProcessVariable(parentId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
        assertEquals(BpmReasonEnum.REJECT_TASK.format(BpmReasonEnum.REJECT_CHILD_PROCESS.getReason()),
                getHistoricProcessVariable(parentId, BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_REASON));
        assertEquals(BpmProcessInstanceStatusEnum.REJECT.getStatus(), getProcessInstanceStatusEvent(parentId).getStatus());
    }

    @Test
    public void testCancelProcessInstanceByStartUser_cancelChildProcess() {
        // 准备参数
        deploy("bpmn/call-activity.bpmn20.xml");
        String parentId = startProcessInstance("callActivityParent");
        ProcessInstance child = getChildProcessInstance(parentId);
        Task childTask = getRunningTask(child.getId());

        // 调用：取消主流程
        processInstanceService.cancelProcessInstanceByStartUser(1L, new BpmProcessInstanceCancelReqVO()
                .setId(parentId).setReason("不想请了"));
        // 断言：主流程、子流程都被取消
        assertNull(processInstanceService.getProcessInstance(parentId));
        assertNull(processInstanceService.getProcessInstance(child.getId()));
        assertEquals(BpmProcessInstanceStatusEnum.CANCEL.getStatus(), getHistoricProcessVariable(parentId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
        assertEquals(BpmProcessInstanceStatusEnum.CANCEL.getStatus(), getHistoricProcessVariable(child.getId(),
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
        assertEquals(BpmReasonEnum.CANCEL_CHILD_PROCESS_INSTANCE_BY_MAIN_PROCESS.getReason(),
                getHistoricProcessVariable(child.getId(), BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_REASON));
        assertTaskStatus(childTask.getId(), BpmTaskStatusEnum.CANCEL.getStatus(), BpmReasonEnum.CANCEL_BY_SYSTEM.getReason());
    }

    @Test
    public void testCancelProcessInstanceByStartUser_childNotAllow() {
        // 准备参数
        deploy("bpmn/call-activity.bpmn20.xml");
        String parentId = startProcessInstance("callActivityParent");
        ProcessInstance child = getChildProcessInstance(parentId);

        // 调用，并断言异常：子流程不允许单独取消
        assertServiceException(() -> processInstanceService.cancelProcessInstanceByStartUser(1L,
                new BpmProcessInstanceCancelReqVO().setId(child.getId()).setReason("取消")),
                PROCESS_INSTANCE_CANCEL_CHILD_FAIL_NOT_ALLOW);
    }

    // ========== 私有方法 ==========

    private String startProcessInstance() {
        return startProcessInstance("twoStepApprove");
    }

    private String startProcessInstance(String processDefinitionKey) {
        return processInstanceService.createProcessInstance(1L, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey(processDefinitionKey).setBusinessKey("business-1"));
    }

    private void deploy(String resource) {
        extraDeploymentIds.add(repositoryService.createDeployment().addClasspathResource(resource).deploy().getId());
    }

    private ProcessInstance getChildProcessInstance(String parentId) {
        ProcessInstance child = runtimeService.createProcessInstanceQuery().superProcessInstanceId(parentId).singleResult();
        assertNotNull(child);
        return child;
    }

    private HistoricTaskInstance getHistoricTask(String processInstanceId, String taskDefinitionKey) {
        return historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId)
                .taskDefinitionKey(taskDefinitionKey).singleResult();
    }

    /**
     * 将流程实例上唯一的定时器转为可执行任务并立即执行，模拟定时器到期
     */
    private void executeTimerJob(String processInstanceId) {
        Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstanceId).singleResult();
        assertNotNull(timerJob);
        Job job = managementService.moveTimerToExecutableJob(timerJob.getId());
        managementService.executeJob(job.getId());
    }

    private BpmProcessInstanceStatusEvent getProcessInstanceStatusEvent(String processInstanceId) {
        List<BpmProcessInstanceStatusEvent> events = applicationEvents.stream(BpmProcessInstanceStatusEvent.class)
                .filter(event -> processInstanceId.equals(event.getId())).toList();
        assertEquals(1, events.size());
        return events.get(0);
    }

    private void mockUserTaskExtensionElement(String taskDefinitionKey, String name, Object value) {
        doAnswer(invocation -> {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(invocation.getArgument(0));
            FlowElement element = bpmnModel.getFlowElement(taskDefinitionKey);
            if (element != null && !element.getExtensionElements().containsKey(name)) {
                BpmnModelUtils.addExtensionElement(element, name, String.valueOf(value));
            }
            return bpmnModel;
        }).when(modelService).getBpmnModelByDefinitionId(anyString());
    }

    private void mockBoundaryEventTimeoutHandlerType(String boundaryEventId, BpmUserTaskTimeoutHandlerTypeEnum handlerType) {
        doAnswer(invocation -> {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(invocation.getArgument(0));
            FlowElement element = bpmnModel.getFlowElement(boundaryEventId);
            if (element != null) {
                element.getExtensionElements().get(BpmnModelConstants.USER_TASK_TIMEOUT_HANDLER_TYPE).get(0)
                        .setElementText(String.valueOf(handlerType.getType()));
            }
            return bpmnModel;
        }).when(modelService).getBpmnModelByDefinitionId(anyString());
    }

    /**
     * 与 {@link cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateInvoker#calculateUsersByTask} 一致：非人工审核（自动通过、不通过）的节点不计算审批人
     */
    private void mockTaskWithoutCandidate(String taskDefinitionKey) {
        when(taskCandidateInvoker.calculateUsersByTask(any())).thenAnswer(invocation ->
                taskDefinitionKey.equals(((DelegateExecution) invocation.getArgument(0)).getCurrentActivityId())
                        ? Set.of() : Set.of(1L));
    }

    private void mockAutoApprovalType(BpmAutoApproveTypeEnum autoApproveType) {
        doAnswer(invocation -> new BpmProcessDefinitionInfoDO().setProcessDefinitionId(invocation.getArgument(0))
                .setModelType(BpmModelTypeEnum.BPMN.getType()).setAutoApprovalType(autoApproveType.getType()))
                .when(processDefinitionService).getProcessDefinitionInfo(anyString());
    }

    private Task getRunningTask(String processInstanceId) {
        List<Task> tasks = bpmTaskService.getRunningTaskListByProcessInstanceId(processInstanceId, null, null);
        assertEquals(1, tasks.size());
        return tasks.get(0);
    }

    private void approveRunningTask(String processInstanceId) {
        Task task = getRunningTask(processInstanceId);
        bpmTaskService.approveTask(1L, new BpmTaskApproveReqVO().setId(task.getId()).setReason("同意"));
    }

    private Integer getTaskStatus(String taskId) {
        return (Integer) bpmTaskService.getHistoricTask(taskId).getTaskLocalVariables()
                .get(BpmnVariableConstants.TASK_VARIABLE_STATUS);
    }

    private void assertTaskStatus(String taskId, Integer status, String reason) {
        HistoricTaskInstance task = bpmTaskService.getHistoricTask(taskId);
        assertEquals(status, task.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_STATUS));
        assertEquals(reason, task.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_REASON));
    }

    private Object getHistoricProcessVariable(String processInstanceId, String name) {
        return processInstanceService.getHistoricProcessInstance(processInstanceId).getProcessVariables().get(name);
    }

}
