package cn.iocoder.yudao.module.bpm.service.task;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.*;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.enums.definition.*;
import cn.iocoder.yudao.module.bpm.enums.task.BpmAttachmentTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmCommentTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmReasonEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskSignTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmHttpRequestUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.service.comment.BpmCommentService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmModelService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.yudao.module.bpm.service.message.BpmMessageService;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenTaskCreatedReqDTO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.impl.persistence.entity.AttachmentEntityImpl;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Attachment;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link BpmTaskServiceImpl} 的单元测试
 *
 * @author HUIHUI
 */
public class BpmTaskServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmTaskServiceImpl taskService;

    @Mock
    private TaskService flowableTaskService;
    @Mock
    private Task task;
    @Mock
    private HistoryService historyService;
    @Mock
    private RuntimeService runtimeService;
    @Mock
    private BpmProcessInstanceService processInstanceService;
    @Mock
    private BpmProcessDefinitionService processDefinitionService;
    @Mock
    private BpmProcessInstanceCopyService processInstanceCopyService;
    @Mock
    private BpmCommentService commentService;
    @Mock
    private BpmModelService modelService;
    @Mock
    private BpmMessageService messageService;
    @Mock
    private AdminUserApi adminUserApi;
    @Mock
    private DeptApi deptApi;
    @Mock
    private ProcessInstance processInstance;

    /**
     * 自身代理对象，用于验证 getSelf() 发起的自动审批、自动拒绝、转办
     */
    @Mock
    private BpmTaskServiceImpl selfService;

    private MockedStatic<SpringUtil> springUtilMockedStatic;

    @BeforeEach
    public void setUp() {
        springUtilMockedStatic = mockStatic(SpringUtil.class);
        springUtilMockedStatic.when(() -> SpringUtil.getBean(any(Class.class))).thenReturn(selfService);
    }

    @AfterEach
    public void tearDown() {
        springUtilMockedStatic.close();
    }

    @Test
    public void testValidateTask_success() {
        // 准备参数：通过 spy 绕过 Flowable 查询，只验证负责人校验
        BpmTaskServiceImpl spyService = spy(taskService);
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
        BpmTaskServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).validateTaskExists("task-1");
        when(task.getAssignee()).thenReturn("2");

        // 调用，并断言异常
        assertServiceException(() -> spyService.validateTask(1L, "task-1"), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
    }

    @Test
    public void testValidateTask_noAssignee_success() {
        // 准备参数
        BpmTaskServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).validateTaskExists("task-1");
        when(task.getAssignee()).thenReturn(null);

        // 调用
        Task result = spyService.validateTask(null, "task-1");
        // 断言
        assertSame(task, result);
    }

    @Test
    public void testValidateTaskExists_notExists() {
        // 准备参数
        BpmTaskServiceImpl spyService = spy(taskService);
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

    // ========== Update 写入相关方法 ==========

    @Test
    public void testApproveTask_processInstanceNotExists() {
        // 准备参数
        BpmTaskServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).validateTask(1L, "task-1");
        when(task.getProcessInstanceId()).thenReturn("process-instance-1");

        // 调用，并断言异常
        assertServiceException(() -> spyService.approveTask(1L, new BpmTaskApproveReqVO().setId("task-1")),
                PROCESS_INSTANCE_NOT_EXISTS);
    }

    @Test
    public void testApproveTask_signatureNotExists() {
        // 准备参数
        BpmTaskServiceImpl spyService = spy(taskService);
        mockApproveTask(spyService, BpmnModelConstants.SIGN_ENABLE);

        // 调用，并断言异常
        assertServiceException(() -> spyService.approveTask(1L, new BpmTaskApproveReqVO().setId("task-1")
                .setReason("同意")), TASK_SIGNATURE_NOT_EXISTS);
    }

    @Test
    public void testApproveTask_reasonRequire() {
        // 准备参数
        BpmTaskServiceImpl spyService = spy(taskService);
        mockApproveTask(spyService, BpmnModelConstants.REASON_REQUIRE);

        // 调用，并断言异常
        assertServiceException(() -> spyService.approveTask(1L, new BpmTaskApproveReqVO().setId("task-1")),
                TASK_REASON_REQUIRE);
    }

    @Test
    public void testApproveTask_success() {
        // 准备参数：task1 开启签名，下一个节点 task2 为审批人自选；task1 曾被退回过
        BpmTaskServiceImpl spyService = spy(taskService);
        BpmnModel bpmnModel = mockApproveTaskSuccess(spyService, BpmTaskCandidateStrategyEnum.APPROVE_USER_SELECT,
                Map.of("reason", "事假"));
        BpmnModelUtils.addExtensionElement(bpmnModel.getFlowElement("task1"), BpmnModelConstants.SIGN_ENABLE, "true");
        when(runtimeService.getVariable("process-instance-1",
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_NEED_SIMULATE_TASK_IDS))
                .thenReturn(new HashSet<>(Set.of("task1", "task2")));
        String returnFlagKey = String.format(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_RETURN_FLAG, "task1");
        when(runtimeService.hasVariable("process-instance-1", returnFlagKey)).thenReturn(true);
        BpmTaskApproveReqVO reqVO = new BpmTaskApproveReqVO().setId("task-1").setReason("同意")
                .setSignPicUrl("https://www.iocoder.cn/sign.png")
                .setAttachments(List.of("https://www.iocoder.cn/files/a.pdf"))
                .setVariables(Map.of("day", 3))
                .setNextAssignees(Map.of("task2", List.of(2L)));

        // 调用
        spyService.approveTask(1L, reqVO);
        // 断言：任务状态、签名、评论、附件
        verify(flowableTaskService).setVariableLocal("task-1", BpmnVariableConstants.TASK_VARIABLE_STATUS,
                BpmTaskStatusEnum.APPROVE.getStatus());
        verify(flowableTaskService).setVariableLocal("task-1", BpmnVariableConstants.TASK_VARIABLE_REASON, "同意");
        verify(flowableTaskService).setVariableLocal("task-1", BpmnVariableConstants.TASK_SIGN_PIC_URL,
                "https://www.iocoder.cn/sign.png");
        verify(commentService).createComment("task-1", "process-instance-1", BpmCommentTypeEnum.APPROVE, "同意");
        verify(flowableTaskService).createAttachment(BpmAttachmentTypeEnum.TASK_ATTACHMENT.getType(), "task-1",
                "process-instance-1", "a.pdf", null, "https://www.iocoder.cn/files/a.pdf");
        // 断言：流程变量合并历史变量与前端变量，并记录下一个节点的审批人
        Map<String, Object> processVariables = captureProcessVariables();
        assertEquals("事假", processVariables.get("reason"));
        assertEquals(3, processVariables.get("day"));
        assertEquals(Map.of("task2", List.of(2L)), processVariables.get(
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_APPROVE_USER_SELECT_ASSIGNEES));
        // 断言：移除预测节点、清理退回标记，并完成任务
        verify(runtimeService).setVariable("process-instance-1",
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_NEED_SIMULATE_TASK_IDS, Set.of("task2"));
        verify(runtimeService).removeVariable("process-instance-1", returnFlagKey);
        verify(flowableTaskService).complete("task-1", Map.of("day", 3), true);
    }

    @Test
    public void testApproveTask_approveUserSelectNotConfig() {
        // 准备参数：下一个节点 task2 为审批人自选，但未选择审批人
        BpmTaskServiceImpl spyService = spy(taskService);
        mockApproveTaskSuccess(spyService, BpmTaskCandidateStrategyEnum.APPROVE_USER_SELECT, Map.of());

        // 调用，并断言异常
        assertServiceException(() -> spyService.approveTask(1L, new BpmTaskApproveReqVO().setId("task-1")
                .setReason("同意")), PROCESS_INSTANCE_APPROVE_USER_SELECT_ASSIGNEES_NOT_CONFIG, "二级审批");
        verify(flowableTaskService, never()).complete(anyString(), anyMap(), anyBoolean());
    }

    @Test
    public void testApproveTask_startUserSelectNotConfig() {
        // 准备参数：下一个节点 task2 为发起人自选，但发起时、审批时都未选择审批人
        BpmTaskServiceImpl spyService = spy(taskService);
        mockApproveTaskSuccess(spyService, BpmTaskCandidateStrategyEnum.START_USER_SELECT, Map.of());

        // 调用，并断言异常
        assertServiceException(() -> spyService.approveTask(1L, new BpmTaskApproveReqVO().setId("task-1")
                .setReason("同意")), PROCESS_INSTANCE_START_USER_SELECT_ASSIGNEES_NOT_CONFIG, "二级审批");
    }

    @Test
    public void testApproveTask_startUserSelectAlreadyConfig() {
        // 准备参数：下一个节点 task2 为发起人自选，发起时已经选择审批人
        BpmTaskServiceImpl spyService = spy(taskService);
        Map<String, List<Long>> startUserSelectAssignees = Map.of("task2", List.of(2L));
        mockApproveTaskSuccess(spyService, BpmTaskCandidateStrategyEnum.START_USER_SELECT, Map.of(
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_SELECT_ASSIGNEES, startUserSelectAssignees));

        // 调用：审批时传入的审批人，不允许覆盖发起时选择的审批人
        spyService.approveTask(1L, new BpmTaskApproveReqVO().setId("task-1").setReason("同意")
                .setNextAssignees(Map.of("task2", List.of(3L))));
        // 断言
        assertEquals(startUserSelectAssignees, captureProcessVariables().get(
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_SELECT_ASSIGNEES));
        verify(flowableTaskService).complete("task-1", Map.of(), true);
        verify(runtimeService, never()).removeVariable(anyString(), anyString());
    }

    @Test
    public void testMoveTaskToEnd_noRunningTask() {
        // 准备参数
        BpmTaskServiceImpl spyService = spy(taskService);
        doReturn(List.of()).when(spyService).getRunningTaskListByProcessInstanceId("process-instance-1", null, null);

        // 调用
        spyService.moveTaskToEnd("process-instance-1", "结束流程");
        // 断言
        verifyNoInteractions(runtimeService, modelService);
    }

    @Test
    public void testRejectTask_processInstanceNotExists() {
        // 准备参数
        BpmTaskServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).validateTask(1L, "task-1");
        when(task.getProcessInstanceId()).thenReturn("process-instance-1");

        // 调用，并断言异常
        assertServiceException(() -> spyService.rejectTask(1L, new BpmTaskRejectReqVO().setId("task-1")
                .setReason("不同意")), PROCESS_INSTANCE_NOT_EXISTS);
    }

    @Test
    public void testReturnTask_pending() {
        // 准备参数
        BpmTaskServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).validateTask(1L, "task-1");
        when(task.isSuspended()).thenReturn(true);

        // 调用，并断言异常
        assertServiceException(() -> spyService.returnTask(1L, new BpmTaskReturnReqVO().setId("task-1")
                .setTargetTaskDefinitionKey("task1").setReason("退回")), TASK_IS_PENDING);
    }

    @Test
    public void testDelegateTask_userNotExists() {
        // 准备参数
        BpmTaskServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).validateTask(1L, "task-1");
        when(task.getAssignee()).thenReturn("1");

        // 调用，并断言异常
        assertServiceException(() -> spyService.delegateTask(1L, new BpmTaskDelegateReqVO().setId("task-1")
                .setDelegateUserId(2L).setReason("委派")), TASK_DELEGATE_FAIL_USER_NOT_EXISTS);
    }

    @Test
    public void testDelegateTask_emptyAssignee_userNotExists() {
        // 准备参数：自动审批或审批人为空时，任务可能没有 assignee
        BpmTaskServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).validateTask(1L, "task-1");
        when(task.getAssignee()).thenReturn(null);

        // 调用，并断言：不应因 assignee 为空触发空指针
        assertServiceException(() -> spyService.delegateTask(1L, new BpmTaskDelegateReqVO().setId("task-1")
                .setDelegateUserId(2L).setReason("委派")), TASK_DELEGATE_FAIL_USER_NOT_EXISTS);
    }

    @Test
    public void testTransferTask_userRepeat() {
        // 准备参数
        BpmTaskServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).validateTask(1L, "task-1");
        when(task.getAssignee()).thenReturn("1");

        // 调用，并断言异常
        assertServiceException(() -> spyService.transferTask(1L, new BpmTaskTransferReqVO().setId("task-1")
                .setAssigneeUserId(1L).setReason("转办")), TASK_TRANSFER_FAIL_USER_REPEAT);
    }

    @Test
    public void testTransferTask_userNotExists() {
        // 准备参数
        BpmTaskServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).validateTask(1L, "task-1");
        when(task.getAssignee()).thenReturn("1");

        // 调用，并断言异常
        assertServiceException(() -> spyService.transferTask(1L, new BpmTaskTransferReqVO().setId("task-1")
                .setAssigneeUserId(2L).setReason("转办")), TASK_TRANSFER_FAIL_USER_NOT_EXISTS);
    }

    @Test
    public void testTransferTask_alreadyHasOwner() {
        // 准备参数
        BpmTaskServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).validateTask(1L, "task-1");
        when(task.getAssignee()).thenReturn("1");
        when(task.getOwner()).thenReturn("3");
        when(task.getProcessInstanceId()).thenReturn("process-instance-1");
        when(adminUserApi.getUser(2L)).thenReturn(new AdminUserRespDTO().setId(2L).setNickname("李四"));
        when(adminUserApi.getUser(1L)).thenReturn(new AdminUserRespDTO().setId(1L).setNickname("张三"));

        // 调用
        spyService.transferTask(1L, new BpmTaskTransferReqVO().setId("task-1").setAssigneeUserId(2L).setReason("转办"));
        // 断言：已经存在 owner 时，不覆盖 owner
        verify(flowableTaskService, never()).setOwner(anyString(), anyString());
        verify(flowableTaskService).setAssignee("task-1", "2");
        verify(commentService).createComment(eq("task-1"), eq("process-instance-1"),
                eq(BpmCommentTypeEnum.TRANSFER), eq("张三"), eq("李四"), eq("转办"));
    }

    @Test
    public void testCreateSignTask_userNotExists() {
        // 准备参数
        BpmTaskServiceImpl spyService = spy(taskService);
        TaskEntityImpl taskEntity = buildTaskEntity("task-1", null);
        taskEntity.setProcessInstanceId("process-instance-1");
        taskEntity.setTaskDefinitionKey("task1");
        doReturn(taskEntity).when(spyService).validateTask(1L, "task-1");
        TaskQuery taskQuery = mock(TaskQuery.class, RETURNS_SELF);
        when(flowableTaskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(List.of());

        // 调用，并断言异常
        assertServiceException(() -> spyService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId("task-1")
                        .setType(BpmTaskSignTypeEnum.BEFORE.getType()).setUserIds(Set.of(2L)).setReason("加签")),
                TASK_SIGN_CREATE_USER_NOT_EXIST);
    }

    @Test
    public void testDeleteSignTask_parentNotExists() {
        // 准备参数
        BpmTaskServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).validateTaskExists("task-1");
        when(task.getParentTaskId()).thenReturn("parent-1");
        doReturn(null).when(spyService).getTask("parent-1");

        // 调用，并断言异常
        assertServiceException(() -> spyService.deleteSignTask(1L, new BpmTaskSignDeleteReqVO().setId("task-1")
                .setReason("减签")), TASK_SIGN_DELETE_NO_PARENT);
    }

    @Test
    public void testDeleteSignTask_parentNotSign() {
        // 准备参数
        BpmTaskServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).validateTaskExists("task-1");
        when(task.getParentTaskId()).thenReturn("parent-1");
        doReturn(buildTaskEntity("parent-1", null)).when(spyService).getTask("parent-1");

        // 调用，并断言异常
        assertServiceException(() -> spyService.deleteSignTask(1L, new BpmTaskSignDeleteReqVO().setId("task-1")
                .setReason("减签")), TASK_SIGN_DELETE_NO_PARENT);
    }

    @Test
    public void testCopyTask_success() {
        // 准备参数
        BpmTaskCopyReqVO reqVO = new BpmTaskCopyReqVO().setId("task-1").setCopyUserIds(Set.of(2L)).setReason("抄送");

        // 调用
        taskService.copyTask(1L, reqVO);
        // 断言
        verify(processInstanceCopyService).createProcessInstanceCopy(Set.of(2L), "抄送", "task-1");
    }

    @Test
    public void testWithdrawTask_processNotRunning() {
        // 准备参数
        HistoricTaskInstance historicTask = mockWithdrawHistoricTask("task1");
        when(historicTask.getProcessInstanceId()).thenReturn("process-instance-1");

        // 调用，并断言异常
        assertServiceException(() -> taskService.withdrawTask(1L, "task-1"), TASK_WITHDRAW_FAIL_PROCESS_NOT_RUNNING);
    }

    @Test
    public void testWithdrawTask_noNextUserTask() {
        // 准备参数：task2 已经是最后一个审批节点
        HistoricTaskInstance historicTask = mockWithdrawHistoricTask("task2");
        when(historicTask.getProcessInstanceId()).thenReturn("process-instance-1");
        when(historicTask.getProcessDefinitionId()).thenReturn("definition-1");
        when(processInstanceService.getProcessInstance("process-instance-1")).thenReturn(processInstance);
        when(processInstance.getProcessDefinitionId()).thenReturn("definition-1");
        when(processDefinitionService.getProcessDefinitionInfo("definition-1"))
                .thenReturn(new BpmProcessDefinitionInfoDO().setAllowWithdrawTask(true));
        when(modelService.getBpmnModelByDefinitionId("definition-1")).thenReturn(buildTwoStepBpmnModel());

        // 调用，并断言异常
        assertServiceException(() -> taskService.withdrawTask(1L, "task-1"), TASK_WITHDRAW_FAIL_NEXT_TASK_NOT_ALLOW);
    }

    // ========== Event 事件相关方法 ==========

    @Test
    public void testProcessTaskCreated_statusExists() {
        // 准备参数
        when(task.getTaskLocalVariables()).thenReturn(Map.of(BpmnVariableConstants.TASK_VARIABLE_STATUS,
                BpmTaskStatusEnum.RUNNING.getStatus()));

        // 调用
        taskService.processTaskCreated(task);
        // 断言
        verify(flowableTaskService, never()).setVariableLocal(any(), any(), any());
        verifyNoInteractions(processInstanceService);
    }

    @Test
    public void testProcessTaskCreated_processInstanceNotExists() {
        // 准备参数
        mockTask("1");

        // 调用
        taskService.processTaskCreated(task);
        // 断言：状态更新为审批中，但不再处理后续逻辑
        verify(flowableTaskService).setVariableLocal("task-1", BpmnVariableConstants.TASK_VARIABLE_STATUS,
                BpmTaskStatusEnum.RUNNING.getStatus());
        verifyNoInteractions(processDefinitionService, modelService);
    }

    @Test
    public void testProcessTaskCreated_processDefinitionNotExists() {
        // 准备参数
        mockTask("1");
        when(processInstanceService.getProcessInstance("process-instance-1")).thenReturn(processInstance);
        when(processInstance.getProcessDefinitionId()).thenReturn("definition-1");

        // 调用
        taskService.processTaskCreated(task);
        // 断言
        verify(processDefinitionService).getProcessDefinitionInfo("definition-1");
        verifyNoInteractions(modelService);
    }

    @Test
    public void testProcessTaskCreated_userApproveHasAssignee() {
        // 准备参数：人工审批且已有审批人，不自动处理
        mockProcessTaskCreated("1", Map.of(BpmnModelConstants.USER_TASK_APPROVE_TYPE,
                BpmUserTaskApproveTypeEnum.USER.getType(), BpmnModelConstants.USER_TASK_ASSIGN_EMPTY_HANDLER_TYPE,
                BpmUserTaskAssignEmptyHandlerTypeEnum.APPROVE.getType()));

        // 调用
        executeWithSynchronization(() -> taskService.processTaskCreated(task),
                TransactionSynchronization.STATUS_COMMITTED);
        // 断言
        verifyNoInteractions(selfService);
    }

    @Test
    public void testProcessTaskCreated_rolledBack() {
        // 准备参数
        mockProcessTaskCreated("1", Map.of(BpmnModelConstants.USER_TASK_APPROVE_TYPE,
                BpmUserTaskApproveTypeEnum.AUTO_APPROVE.getType()));

        // 调用
        executeWithSynchronization(() -> taskService.processTaskCreated(task),
                TransactionSynchronization.STATUS_ROLLED_BACK);
        // 断言
        verifyNoInteractions(selfService);
    }

    @Test
    public void testProcessTaskCreated_taskBeforeTrigger() {
        // 准备参数：配置任务前置通知
        BpmModelMetaInfoVO.HttpRequestSetting setting = new BpmModelMetaInfoVO.HttpRequestSetting()
                .setUrl("http://127.0.0.1/task-before");
        mockProcessTaskCreated("1", Map.of(BpmnModelConstants.USER_TASK_APPROVE_TYPE,
                BpmUserTaskApproveTypeEnum.USER.getType()),
                new BpmProcessDefinitionInfoDO().setTaskBeforeTriggerSetting(setting));

        // 调用
        try (MockedStatic<BpmHttpRequestUtils> httpRequestUtilsMockedStatic = mockStatic(BpmHttpRequestUtils.class)) {
            executeWithSynchronization(() -> taskService.processTaskCreated(task),
                    TransactionSynchronization.STATUS_COMMITTED);
            // 断言
            httpRequestUtilsMockedStatic.verify(() -> BpmHttpRequestUtils.executeBpmHttpRequest(processInstance,
                    "http://127.0.0.1/task-before", null, null, true, null));
        }
        verifyNoInteractions(selfService);
    }

    @Test
    public void testProcessTaskCreated_unknownStatusTaskNotExists() {
        // 准备参数：事务状态未知，且任务已经不存在（例如说，已被自动审批）
        BpmTaskServiceImpl spyService = spy(taskService);
        mockProcessTaskCreated("1", Map.of(BpmnModelConstants.USER_TASK_APPROVE_TYPE,
                BpmUserTaskApproveTypeEnum.AUTO_APPROVE.getType()));
        doReturn(null).when(spyService).getTask("task-1");

        // 调用
        executeWithSynchronization(() -> spyService.processTaskCreated(task),
                TransactionSynchronization.STATUS_UNKNOWN);
        // 断言
        verifyNoInteractions(selfService);
    }

    @Test
    public void testProcessTaskCreated_unknownStatusTaskExists() {
        // 准备参数：事务状态未知，但任务仍然存在
        BpmTaskServiceImpl spyService = spy(taskService);
        mockProcessTaskCreated("1", Map.of(BpmnModelConstants.USER_TASK_APPROVE_TYPE,
                BpmUserTaskApproveTypeEnum.AUTO_APPROVE.getType()));
        doReturn(task).when(spyService).getTask("task-1");

        // 调用
        executeWithSynchronization(() -> spyService.processTaskCreated(task),
                TransactionSynchronization.STATUS_UNKNOWN);
        // 断言：仍然执行自动审批
        assertEquals(BpmReasonEnum.APPROVE_TYPE_AUTO_APPROVE.getReason(), captureApproveTaskInternal(null).getReason());
    }

    @Test
    public void testProcessTaskCanceled_notExists() {
        // 准备参数
        BpmTaskServiceImpl spyService = spy(taskService);
        doReturn(null).when(spyService).getTask("task-1");

        // 调用
        spyService.processTaskCanceled("task-1");
        // 断言
        verify(flowableTaskService, never()).setVariableLocal(any(), any(), any());
    }

    @Test
    public void testProcessTaskCanceled_endStatus() {
        // 准备参数
        BpmTaskServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).getTask("task-1");
        when(task.getTaskLocalVariables()).thenReturn(Map.of(BpmnVariableConstants.TASK_VARIABLE_STATUS,
                BpmTaskStatusEnum.APPROVE.getStatus()));

        // 调用
        spyService.processTaskCanceled("task-1");
        // 断言
        verify(flowableTaskService, never()).setVariableLocal(any(), any(), any());
    }

    @Test
    public void testProcessTaskCanceled_success() {
        // 准备参数
        BpmTaskServiceImpl spyService = spy(taskService);
        doReturn(task).when(spyService).getTask("task-1");
        when(task.getTaskLocalVariables()).thenReturn(Map.of(BpmnVariableConstants.TASK_VARIABLE_STATUS,
                BpmTaskStatusEnum.RUNNING.getStatus()));

        // 调用
        spyService.processTaskCanceled("task-1");
        // 断言
        verify(flowableTaskService).setVariableLocal("task-1", BpmnVariableConstants.TASK_VARIABLE_STATUS,
                BpmTaskStatusEnum.CANCEL.getStatus());
        verify(flowableTaskService).setVariableLocal("task-1", BpmnVariableConstants.TASK_VARIABLE_REASON,
                BpmReasonEnum.CANCEL_BY_SYSTEM.getReason());
    }

    @Test
    public void testProcessTaskAssigned_noAssignee() {
        // 准备参数
        when(task.getAssignee()).thenReturn(null);

        // 调用
        executeWithSynchronization(() -> taskService.processTaskAssigned(task),
                TransactionSynchronization.STATUS_COMMITTED);
        // 断言
        verifyNoInteractions(processInstanceService, messageService, selfService);
    }

    @Test
    public void testProcessTaskAssigned_rolledBack() {
        // 调用
        executeWithSynchronization(() -> taskService.processTaskAssigned(task),
                TransactionSynchronization.STATUS_ROLLED_BACK);
        // 断言
        verifyNoInteractions(task, processInstanceService, messageService, selfService);
    }

    @Test
    public void testProcessTaskAssigned_unknownStatusTaskNotExists() {
        // 准备参数：事务状态未知，且任务已经不存在
        BpmTaskServiceImpl spyService = spy(taskService);
        when(task.getId()).thenReturn("task-1");
        doReturn(null).when(spyService).getTask("task-1");

        // 调用
        executeWithSynchronization(() -> spyService.processTaskAssigned(task),
                TransactionSynchronization.STATUS_UNKNOWN);
        // 断言
        verifyNoInteractions(processInstanceService, messageService, selfService);
    }

    @Test
    public void testProcessTaskAssigned_processInstanceNotExists() {
        // 准备参数
        when(task.getAssignee()).thenReturn("1");
        when(task.getProcessInstanceId()).thenReturn("process-instance-1");

        // 调用
        executeWithSynchronization(() -> taskService.processTaskAssigned(task),
                TransactionSynchronization.STATUS_COMMITTED);
        // 断言
        verifyNoInteractions(processDefinitionService, messageService, selfService);
    }

    @Test
    public void testProcessTaskAssigned_startUserSkipWhenReturn() {
        // 准备参数：退回到该节点时，不自动跳过，而是发送通知
        mockProcessTaskAssigned("task2", "1", null);
        when(processInstance.getStartUserId()).thenReturn("1");
        when(processInstance.getProcessInstanceId()).thenReturn("process-instance-1");
        when(runtimeService.getVariable("process-instance-1",
                String.format(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_RETURN_FLAG, "task2"), Boolean.class))
                .thenReturn(true);
        when(modelService.getBpmnModelByDefinitionId("definition-1")).thenReturn(buildBpmnModel("task2",
                Map.of(BpmnModelConstants.USER_TASK_ASSIGN_START_USER_HANDLER_TYPE,
                        BpmUserTaskAssignStartUserHandlerTypeEnum.SKIP.getType())));
        when(adminUserApi.getUser(1L)).thenReturn(new AdminUserRespDTO().setId(1L).setNickname("张三"));

        // 调用
        executeWithSynchronization(() -> taskService.processTaskAssigned(task),
                TransactionSynchronization.STATUS_COMMITTED);
        // 断言
        verifyNoInteractions(selfService);
        verify(messageService).sendMessageWhenTaskAssigned(any(BpmMessageSendWhenTaskCreatedReqDTO.class));
    }

    @Test
    public void testProcessTaskCompleted_processInstanceNotExists() {
        // 准备参数
        when(task.getProcessInstanceId()).thenReturn("process-instance-1");

        // 调用
        taskService.processTaskCompleted(task);
        // 断言
        verifyNoInteractions(processDefinitionService);
    }

    @Test
    public void testProcessTaskCompleted_noTriggerSetting() {
        // 准备参数
        when(task.getProcessInstanceId()).thenReturn("process-instance-1");
        when(processInstanceService.getProcessInstance("process-instance-1")).thenReturn(processInstance);
        when(processInstance.getProcessDefinitionId()).thenReturn("definition-1");
        when(processDefinitionService.getProcessDefinitionInfo("definition-1"))
                .thenReturn(new BpmProcessDefinitionInfoDO());

        // 调用
        taskService.processTaskCompleted(task);
        // 断言
        verify(processDefinitionService).getProcessDefinitionInfo("definition-1");
    }

    @Test
    public void testProcessTaskCompleted_taskAfterTrigger() {
        // 准备参数：配置任务后置通知
        when(task.getProcessInstanceId()).thenReturn("process-instance-1");
        when(processInstanceService.getProcessInstance("process-instance-1")).thenReturn(processInstance);
        when(processInstance.getProcessDefinitionId()).thenReturn("definition-1");
        BpmModelMetaInfoVO.HttpRequestSetting setting = new BpmModelMetaInfoVO.HttpRequestSetting()
                .setUrl("http://127.0.0.1/task-after");
        when(processDefinitionService.getProcessDefinitionInfo("definition-1"))
                .thenReturn(new BpmProcessDefinitionInfoDO().setTaskAfterTriggerSetting(setting));

        // 调用
        try (MockedStatic<BpmHttpRequestUtils> httpRequestUtilsMockedStatic = mockStatic(BpmHttpRequestUtils.class)) {
            taskService.processTaskCompleted(task);
            // 断言
            httpRequestUtilsMockedStatic.verify(() -> BpmHttpRequestUtils.executeBpmHttpRequest(processInstance,
                    "http://127.0.0.1/task-after", null, null, true, null));
        }
    }

    @Test
    public void testProcessTaskTimeout_processInstanceNotExists() {
        // 调用
        taskService.processTaskTimeout("process-instance-1", "task1",
                BpmUserTaskTimeoutHandlerTypeEnum.REMINDER.getType());
        // 断言
        verifyNoInteractions(flowableTaskService, messageService);
    }

    @Test
    public void testProcessTaskTimeout_taskNotExists() {
        // 准备参数
        BpmTaskServiceImpl spyService = spy(taskService);
        when(processInstanceService.getProcessInstance("process-instance-1")).thenReturn(processInstance);
        doReturn(List.of()).when(spyService).getRunningTaskListByProcessInstanceId("process-instance-1", true, "task1");

        // 调用
        spyService.processTaskTimeout("process-instance-1", "task1",
                BpmUserTaskTimeoutHandlerTypeEnum.REMINDER.getType());
        // 断言
        verifyNoInteractions(messageService);
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

    /**
     * 构建 task1 -> task2 的两级审批流程模型
     */
    private static BpmnModel buildTwoStepBpmnModel() {
        return BpmnModelUtils.getBpmnModel(ResourceUtil.readBytes("bpmn/two-step-approve.bpmn20.xml"));
    }

    /**
     * 构建只有一个 UserTask 的流程模型
     */
    private static BpmnModel buildBpmnModel(String userTaskId, Map<String, Integer> extensions) {
        UserTask userTask = new UserTask();
        userTask.setId(userTaskId);
        extensions.forEach((name, value) -> BpmnModelUtils.addExtensionElement(userTask, name, value));
        Process process = new Process();
        process.setId("process");
        process.addFlowElement(userTask);
        BpmnModel bpmnModel = new BpmnModel();
        bpmnModel.addProcess(process);
        return bpmnModel;
    }

    private void mockApproveTask(BpmTaskServiceImpl spyService, String extensionName) {
        doReturn(task).when(spyService).validateTask(1L, "task-1");
        when(task.getProcessInstanceId()).thenReturn("process-instance-1");
        when(task.getProcessDefinitionId()).thenReturn("definition-1");
        when(task.getTaskDefinitionKey()).thenReturn("task1");
        when(processInstanceService.getProcessInstance("process-instance-1")).thenReturn(processInstance);
        BpmnModel bpmnModel = buildTwoStepBpmnModel();
        BpmnModelUtils.addExtensionElement(bpmnModel.getFlowElement("task1"), extensionName, "true");
        when(modelService.getBpmnModelByDefinitionId("definition-1")).thenReturn(bpmnModel);
    }

    /**
     * 模拟审批 task1：下一个节点 task2 使用指定的审批人策略
     */
    private BpmnModel mockApproveTaskSuccess(BpmTaskServiceImpl spyService, BpmTaskCandidateStrategyEnum task2Strategy,
                                             Map<String, Object> processVariables) {
        doReturn(task).when(spyService).validateTask(1L, "task-1");
        when(task.getId()).thenReturn("task-1");
        when(task.getProcessInstanceId()).thenReturn("process-instance-1");
        when(task.getProcessDefinitionId()).thenReturn("definition-1");
        when(task.getTaskDefinitionKey()).thenReturn("task1");
        when(processInstanceService.getProcessInstance("process-instance-1")).thenReturn(processInstance);
        when(processInstance.getProcessVariables()).thenReturn(processVariables);
        BpmnModel bpmnModel = buildTwoStepBpmnModel();
        FlowElement task2 = bpmnModel.getFlowElement("task2");
        task2.getExtensionElements().remove(BpmnModelConstants.USER_TASK_CANDIDATE_STRATEGY);
        BpmnModelUtils.addExtensionElement(task2, BpmnModelConstants.USER_TASK_CANDIDATE_STRATEGY,
                task2Strategy.getStrategy());
        when(modelService.getBpmnModelByDefinitionId("definition-1")).thenReturn(bpmnModel);
        return bpmnModel;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> captureProcessVariables() {
        ArgumentCaptor<Map<String, Object>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(runtimeService).setVariables(eq("process-instance-1"), variablesCaptor.capture());
        return variablesCaptor.getValue();
    }

    private HistoricTaskInstance mockWithdrawHistoricTask(String taskDefinitionKey) {
        HistoricTaskInstanceQuery historicTaskQuery = mockHistoricTaskQuery();
        HistoricTaskInstance historicTask = mock(HistoricTaskInstance.class);
        lenient().when(historicTask.getTaskDefinitionKey()).thenReturn(taskDefinitionKey);
        when(historicTaskQuery.singleResult()).thenReturn(historicTask);
        return historicTask;
    }

    private HistoricTaskInstanceQuery mockHistoricTaskQuery() {
        HistoricTaskInstanceQuery historicTaskQuery = mock(HistoricTaskInstanceQuery.class, RETURNS_SELF);
        when(historyService.createHistoricTaskInstanceQuery()).thenReturn(historicTaskQuery);
        return historicTaskQuery;
    }

    private void mockTask(String assignee) {
        when(task.getId()).thenReturn("task-1");
        when(task.getProcessInstanceId()).thenReturn("process-instance-1");
        when(task.getTaskLocalVariables()).thenReturn(new HashMap<>());
        lenient().when(task.getAssignee()).thenReturn(assignee);
    }

    private void mockProcessTaskCreated(String assignee, Map<String, Integer> extensions) {
        mockProcessTaskCreated(assignee, extensions, new BpmProcessDefinitionInfoDO());
    }

    private void mockProcessTaskCreated(String assignee, Map<String, Integer> extensions,
                                        BpmProcessDefinitionInfoDO processDefinitionInfo) {
        mockTask(assignee);
        when(task.getTaskDefinitionKey()).thenReturn("task1");
        when(processInstanceService.getProcessInstance("process-instance-1")).thenReturn(processInstance);
        when(processInstance.getProcessDefinitionId()).thenReturn("definition-1");
        when(processDefinitionService.getProcessDefinitionInfo("definition-1")).thenReturn(processDefinitionInfo);
        when(modelService.getBpmnModelByDefinitionId("definition-1")).thenReturn(buildBpmnModel("task1", extensions));
    }

    private void mockProcessTaskAssigned(String taskDefinitionKey, String assignee, Integer autoApprovalType) {
        when(task.getId()).thenReturn("task-1");
        when(task.getAssignee()).thenReturn(assignee);
        when(task.getProcessInstanceId()).thenReturn("process-instance-1");
        when(task.getProcessDefinitionId()).thenReturn("definition-1");
        lenient().when(task.getTaskDefinitionKey()).thenReturn(taskDefinitionKey);
        when(processInstanceService.getProcessInstance("process-instance-1")).thenReturn(processInstance);
        lenient().when(processInstance.getProcessDefinitionId()).thenReturn("definition-1");
        when(processDefinitionService.getProcessDefinitionInfo("definition-1"))
                .thenReturn(new BpmProcessDefinitionInfoDO().setAutoApprovalType(autoApprovalType));
    }

    /**
     * 在事务同步上下文中执行，并模拟事务完成回调
     */
    private static void executeWithSynchronization(Runnable runnable, int transactionStatus) {
        TransactionSynchronizationManager.initSynchronization();
        try {
            runnable.run();
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(synchronization -> synchronization.afterCompletion(transactionStatus));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private BpmTaskApproveReqVO captureApproveTaskInternal(Long userId) {
        ArgumentCaptor<BpmTaskApproveReqVO> reqVOCaptor = ArgumentCaptor.forClass(BpmTaskApproveReqVO.class);
        verify(selfService).approveTaskInternal(eq(userId), reqVOCaptor.capture());
        return reqVOCaptor.getValue();
    }

}
