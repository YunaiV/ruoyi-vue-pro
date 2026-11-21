package cn.iocoder.yudao.module.bpm.framework.flowable;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCancelReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.*;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmAutoApproveTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelFormTypeEnum;
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
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceQueryServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceUpdateServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskQueryServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskUpdateServiceImpl;
import jakarta.annotation.Resource;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.ProcessEngineConfiguration;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
 * {@link BpmTaskQueryServiceImpl}、{@link BpmTaskUpdateServiceImpl}、{@link BpmProcessInstanceQueryServiceImpl}、{@link BpmProcessInstanceUpdateServiceImpl} 的 Flowable 集成测试
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
    private ProcessEngineConfiguration processEngineConfiguration;

    @Resource
    private BpmTaskQueryServiceImpl taskQueryService;
    @Resource
    private BpmTaskUpdateServiceImpl taskUpdateService;
    @Resource
    private BpmProcessInstanceQueryServiceImpl processInstanceQueryService;
    @Resource
    private BpmProcessInstanceUpdateServiceImpl processInstanceUpdateService;

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
        taskUpdateService.triggerTask(processInstanceId, "wait");
        // 断言：触发等待节点后，流程结束
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
        assertNotNull(processInstanceQueryService.getHistoricProcessInstance(processInstanceId).getEndTime());
    }

    @Test
    public void testTriggerTask_executionNotExists() {
        // 准备参数
        String processInstanceId = startProcessInstance();

        // 调用：触发不存在的等待节点
        taskUpdateService.triggerTask(processInstanceId, "notExists");
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
        taskUpdateService.approveTask(1L, new BpmTaskApproveReqVO().setId(task1.getId()).setReason("同意"));
        // 断言：task1 审批通过，流程流转到 task2
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.APPROVE.getStatus(), "同意");
        verify(commentService).createComment(eq(task1.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.APPROVE), eq("同意"));
        Task task2 = getRunningTask(processInstanceId);
        assertEquals("task2", task2.getTaskDefinitionKey());

        // 调用：审批 task2
        taskUpdateService.approveTask(1L, new BpmTaskApproveReqVO().setId(task2.getId()).setReason("同意"));
        // 断言：流程实例结束
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
        assertNotNull(processInstanceQueryService.getHistoricProcessInstance(processInstanceId).getEndTime());
        List<HistoricTaskInstance> tasks = taskQueryService.getTaskListByProcessInstanceId(processInstanceId, true);
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
        taskUpdateService.approveTask(1L, new BpmTaskApproveReqVO().setId(task1.getId()).setReason("同意")
                .setSignPicUrl("https://www.iocoder.cn/sign.png")
                .setAttachments(List.of("https://www.iocoder.cn/files/a.pdf"))
                .setVariables(Map.of("day", 3)));
        // 断言：签名、表单变量保存到任务上，表单变量同步到流程实例
        HistoricTaskInstance historicTask = taskQueryService.getHistoricTask(task1.getId());
        assertEquals("https://www.iocoder.cn/sign.png",
                historicTask.getTaskLocalVariables().get(BpmnVariableConstants.TASK_SIGN_PIC_URL));
        assertEquals(3, historicTask.getTaskLocalVariables().get("day"));
        assertEquals(3, runtimeService.getVariable(processInstanceId, "day"));
        // 断言：附件
        List<Attachment> attachments = taskQueryService.getAttachments(processInstanceId, Set.of(task1.getId()),
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
        assertServiceException(() -> taskUpdateService.approveTask(2L,
                new BpmTaskApproveReqVO().setId(task1.getId()).setReason("同意")), TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
    }

    @Test
    public void testRejectTask_finishProcessInstance() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用：task1 未配置驳回策略，默认结束流程
        taskUpdateService.rejectTask(1L, new BpmTaskRejectReqVO().setId(task1.getId()).setReason("不同意")
                .setAttachments(List.of("https://www.iocoder.cn/files/b.pdf")));
        // 断言：任务不通过，附件保存到任务上
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.REJECT.getStatus(), "不同意");
        List<Attachment> attachments = taskQueryService.getAttachments(processInstanceId, Set.of(task1.getId()),
                BpmAttachmentTypeEnum.TASK_ATTACHMENT);
        assertEquals(List.of("b.pdf"), attachments.stream().map(Attachment::getName).toList());
        // 断言：流程实例不通过并结束
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
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
        taskUpdateService.rejectTask(1L, new BpmTaskRejectReqVO().setId(task2.getId()).setReason("退回修改"));
        // 断言：task2 被标记为退回，流程回到 task1
        assertTaskStatus(task2.getId(), BpmTaskStatusEnum.RETURN.getStatus(), "退回修改");
        Task task1 = getRunningTask(processInstanceId);
        assertEquals("task1", task1.getTaskDefinitionKey());
        assertEquals(Boolean.TRUE, runtimeService.getVariable(processInstanceId,
                String.format(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_RETURN_FLAG, "task1")));
        assertEquals(Set.of("task2"), runtimeService.getVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_NEED_SIMULATE_TASK_IDS));

        // 调用：再次审批 task1
        taskUpdateService.approveTask(1L, new BpmTaskApproveReqVO().setId(task1.getId()).setReason("已修改"));
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
        taskUpdateService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                .setType(BpmTaskSignTypeEnum.BEFORE.getType()).setUserIds(Set.of(2L)).setReason("加签"));
        Task childTask = taskQueryService.getTaskListByParentTaskId(task1.getId()).get(0);

        // 调用：用户 2 审批不通过加签任务
        taskUpdateService.rejectTask(2L, new BpmTaskRejectReqVO().setId(childTask.getId()).setReason("不同意"));
        // 断言：加签任务、根任务都标记为不通过，流程实例不通过并结束
        assertTaskStatus(childTask.getId(), BpmTaskStatusEnum.REJECT.getStatus(), "不同意");
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.REJECT.getStatus(),
                BpmCommentTypeEnum.REJECT.formatComment("加签任务不通过"));
        verify(commentService).createComment(eq(task1.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.REJECT), eq("加签任务不通过"));
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
        assertEquals(BpmProcessInstanceStatusEnum.REJECT.getStatus(), getHistoricProcessVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
    }

    @Test
    public void testGetUserTaskListByReturn_success() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        assertTrue(taskQueryService.getUserTaskListByReturn(getRunningTask(processInstanceId).getId()).isEmpty());
        approveRunningTask(processInstanceId);
        Task task2 = getRunningTask(processInstanceId);

        // 调用
        List<UserTask> userTasks = taskQueryService.getUserTaskListByReturn(task2.getId());
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
        taskUpdateService.returnTask(1L, new BpmTaskReturnReqVO().setId(task2.getId())
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
        assertServiceException(() -> taskUpdateService.returnTask(1L, new BpmTaskReturnReqVO().setId(task1.getId())
                .setTargetTaskDefinitionKey("notExists").setReason("退回")), TASK_TARGET_NODE_NOT_EXISTS);
    }

    @Test
    public void testReturnTask_sourceTargetError() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用，并断言异常：task2 位于 task1 之后，不能退回
        assertServiceException(() -> taskUpdateService.returnTask(1L, new BpmTaskReturnReqVO().setId(task1.getId())
                .setTargetTaskDefinitionKey("task2").setReason("退回")), TASK_RETURN_FAIL_SOURCE_TARGET_ERROR);
    }

    // ========== 退回修改相关（退回后【从当前节点审批】，见 revision-approve.bpmn20.xml） ==========

    @Test
    public void testReturnTask_resumeSource_resubmitByStartUser() {
        // 准备参数：task2 待用户 3 审批
        String processInstanceId = startRevisionProcessInstance(BpmModelFormTypeEnum.NORMAL);
        String taskId = getRunningTask(processInstanceId).getId();

        // 调用：用户 3 退回到发起人节点
        taskUpdateService.returnTask(3L, new BpmTaskReturnReqVO().setId(taskId)
                .setTargetTaskDefinitionKey(BpmnModelConstants.START_USER_NODE_ID).setReason("请补充明细"));
        // 断言：任务不跳转，标记为退回修改中，修改人为发起人
        assertEquals(taskId, getRunningTask(processInstanceId).getId());
        assertTaskStatus(taskId, BpmTaskStatusEnum.WAIT_REVISION.getStatus(), "请补充明细");
        verify(commentService).createComment(eq(taskId), eq(processInstanceId),
                eq(BpmCommentTypeEnum.RETURN), eq("请补充明细"));
        // 断言：不在审批人的待办中，只在发起人的【待我修改】中
        assertEquals(0, taskQueryService.getTaskTodoPage(3L, new BpmTaskPageReqVO()).getTotal());
        assertNull(taskQueryService.getTodoTask(3L, taskId, processInstanceId));
        assertEquals(List.of(taskId), getRevisionTaskIds(1L));
        assertEquals(List.of(), getRevisionTaskIds(3L));
        // 断言：超出范围的分页，没有数据，但总数仍然正确
        BpmTaskPageReqVO pageReqVO = new BpmTaskPageReqVO();
        pageReqVO.setPageNo(2);
        PageResult<Task> emptyPage = taskQueryService.getRevisionTaskPage(1L, pageReqVO);
        assertEquals(1L, emptyPage.getTotal());
        assertEquals(List.of(), emptyPage.getList());
        // 断言：审批详情中，修改人使用退回目标节点（发起人节点）的表单权限，与工作流保留变量同名的字段降级为只读；
        // 审批人既没有待办，也不能修改
        BpmApprovalDetailRespVO detail = processInstanceQueryService.getApprovalDetail(1L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId).setTaskId(taskId));
        assertEquals(taskId, detail.getRevisionTaskId());
        String revisionId = detail.getRevisionId();
        assertEquals(getRevisionId(taskId), revisionId);
        assertNull(detail.getTodoTask());
        assertEquals(Map.of("amount", "2", "remark", "1", "items", "2", "items.price", "1",
                "tags", "2", "tags.tag", "1", "tags.note", "1", "PROCESS_STATUS", "1",
                "NEED_SIMULATE_TASK_IDS", "1", "task1_assignees", "1"), detail.getFormFieldsPermission());
        BpmApprovalDetailRespVO approverDetail = processInstanceQueryService.getApprovalDetail(3L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId).setTaskId(taskId));
        assertNull(approverDetail.getRevisionTaskId());
        assertNull(approverDetail.getTodoTask());

        // 调用，并断言异常：修改期间，审批人不能审批；只有修改人可以重新提交
        assertServiceException(() -> taskUpdateService.approveTask(3L, new BpmTaskApproveReqVO().setId(taskId)
                .setReason("同意")), TASK_WAIT_REVISION);
        assertServiceException(() -> taskUpdateService.resubmitTask(3L, new BpmTaskResubmitReqVO().setTaskId(taskId)),
                TASK_RESUBMIT_NOT_ALLOWED);
        assertServiceException(() -> taskUpdateService.resubmitTask(2L, new BpmTaskResubmitReqVO().setTaskId(taskId)),
                TASK_RESUBMIT_NOT_ALLOWED);
        // 调用，并断言：其它租户下，同一用户编号既查询不到，也不能重新提交
        assertEquals(0L, TenantUtils.execute(2L, () ->
                taskQueryService.getRevisionTaskPage(1L, new BpmTaskPageReqVO()).getTotal()));
        TenantUtils.execute(2L, () -> assertServiceException(() -> taskUpdateService.resubmitTask(1L,
                new BpmTaskResubmitReqVO().setTaskId(taskId)), TASK_RESUBMIT_NOT_ALLOWED));

        // 调用：发起人修改后，重新提交
        taskUpdateService.resubmitTask(1L, new BpmTaskResubmitReqVO().setTaskId(taskId).setRevisionId(revisionId)
                .setVariables(Map.of("amount", 200)));
        // 断言：表单变量更新；任务恢复为审批中，回到审批人的待办
        assertEquals(200, runtimeService.getVariable(processInstanceId, "amount"));
        Task resumedTask = taskQueryService.getTask(taskId);
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), FlowableUtils.getTaskStatus(resumedTask));
        assertFalse(resumedTask.getTaskLocalVariables().containsKey(BpmnVariableConstants.TASK_VARIABLE_REVISION_ASSIGNEE));
        verify(commentService).createComment(eq(taskId), eq(processInstanceId), eq(BpmCommentTypeEnum.RESUBMIT));
        assertEquals(List.of(taskId), taskQueryService.getTaskTodoPage(3L, new BpmTaskPageReqVO()).getList()
                .stream().map(Task::getId).toList());
        assertEquals(List.of(), getRevisionTaskIds(1L));
        // 调用，并断言异常：不能重复提交
        assertServiceException(() -> taskUpdateService.resubmitTask(1L, new BpmTaskResubmitReqVO().setTaskId(taskId)),
                TASK_RESUBMIT_NOT_ALLOWED);

        // 调用：审批人继续审批，使用待办中的批次
        BpmTaskRespVO todoTask = taskQueryService.getTodoTask(3L, taskId, processInstanceId);
        assertEquals(revisionId, todoTask.getRevisionId());
        taskUpdateService.approveTask(3L, new BpmTaskApproveReqVO().setId(taskId).setReason("同意")
                .setRevisionId(todoTask.getRevisionId()));
        // 断言：流程结束
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
    }

    @Test
    public void testResubmitTask_fieldNotWritable() {
        // 准备参数：用户 3 将 task2 退回到发起人节点
        String processInstanceId = startRevisionProcessInstance(BpmModelFormTypeEnum.NORMAL);
        String taskId = getRunningTask(processInstanceId).getId();
        taskUpdateService.returnTask(3L, new BpmTaskReturnReqVO().setId(taskId)
                .setTargetTaskDefinitionKey(BpmnModelConstants.START_USER_NODE_ID).setReason("请修改"));

        // 调用，并断言异常：只读字段，以及不是表单字段的变量、子表单的子字段，不能修改
        assertResubmitFieldNotWritable(taskId, "remark", "已修改", "remark");
        assertResubmitFieldNotWritable(taskId, "notExists", 1, "notExists");
        assertResubmitFieldNotWritable(taskId, "price", 99, "price");
        assertResubmitFieldNotWritable(taskId, "items.price", 99, "items.price");
        // 调用，并断言异常：与工作流保留变量同名的表单字段，即使配置为可编辑，也不能修改
        assertResubmitFieldNotWritable(taskId, BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS,
                BpmProcessInstanceStatusEnum.APPROVE.getStatus(), BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS);
        assertResubmitFieldNotWritable(taskId, BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_NEED_SIMULATE_TASK_IDS,
                Set.of("task2"), BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_NEED_SIMULATE_TASK_IDS);
        assertResubmitFieldNotWritable(taskId, "task1_assignees", List.of(5L), "task1_assignees");
        // 调用，并断言异常：子表单中，已有行的只读子字段不能修改，新增行的只读子字段不能填写
        assertResubmitFieldNotWritable(taskId, "items", List.of(Map.of("name", "a", "price", 99)), "items.price");
        assertResubmitFieldNotWritable(taskId, "items", List.of(Map.of("name", "a", "price", 10),
                Map.of("name", "b", "price", 99)), "items.price");
        // 调用，并断言异常：子表单的值，与组件生成的格式不一致：未知的子字段、行不是对象、不是数组
        assertResubmitFieldNotWritable(taskId, "items", List.of(Map.of("name", "a", "price", 10, "unknown", 1)), "items");
        assertResubmitFieldNotWritable(taskId, "items", List.of("a"), "items");
        assertResubmitFieldNotWritable(taskId, "items", Map.of("name", "a"), "items");
        assertResubmitFieldNotWritable(taskId, "items", null, "items");
        // 调用，并断言异常：子表单配置 props.field 时，每行是 tag 的值，只读的 tag 同样不能修改，也不能是包含 tag 的对象
        assertResubmitFieldNotWritable(taskId, "tags", List.of("a", "y"), "tags.tag");
        assertResubmitFieldNotWritable(taskId, "tags", List.of(Map.of("tag", "a")), "tags");
        // 断言：校验不通过时，不修改变量，任务仍等待修改
        assertEquals(BpmTaskStatusEnum.WAIT_REVISION.getStatus(), getTaskStatus(taskId));
        assertEquals("备注", runtimeService.getVariable(processInstanceId, "remark"));
        assertEquals(BpmProcessInstanceStatusEnum.RUNNING.getStatus(), runtimeService.getVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));

        // 调用：修改可编辑的字段；子表单中，修改可编辑的子字段，删除行、新增行
        // 其中，只读的 price 序列化为 10.0、键的顺序不同，与修改前的 10 一致；tags 新增行为空、或者 props.defaultValue
        Map<String, Object> itemB = new LinkedHashMap<>();
        itemB.put("price", 10.0);
        itemB.put("name", "b");
        List<Map<String, Object>> items = List.of(itemB, Map.of("name", "c"));
        List<String> tags = Arrays.asList("a", null, "x");
        taskUpdateService.resubmitTask(1L, new BpmTaskResubmitReqVO().setTaskId(taskId).setRevisionId(getRevisionId(taskId))
                .setVariables(Map.of("amount", 200, "items", items, "tags", tags)));
        // 断言
        assertEquals(200, runtimeService.getVariable(processInstanceId, "amount"));
        assertEquals(items, runtimeService.getVariable(processInstanceId, "items"));
        assertEquals(tags, runtimeService.getVariable(processInstanceId, "tags"));
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), getTaskStatus(taskId));
    }

    @Test
    public void testRejectTask_resumeSource_resubmitByApprovedAssignee() {
        // 准备参数：task1 或签，用户 2 审批通过、用户 4 的任务被取消；task2 待用户 3 审批
        String processInstanceId = startRevisionProcessInstance(BpmModelFormTypeEnum.NORMAL);
        String taskId = getRunningTask(processInstanceId).getId();

        // 调用：用户 3 审批不通过，按配置驳回到 task1
        taskUpdateService.rejectTask(3L, new BpmTaskRejectReqVO().setId(taskId).setReason("金额有误")
                .setAttachments(List.of("https://www.iocoder.cn/files/c.pdf")));
        // 断言：任务不标记为不通过，由 task1 审批通过的用户 2 修改，而不是被取消的用户 4；附件仍保存到任务上
        assertTaskStatus(taskId, BpmTaskStatusEnum.WAIT_REVISION.getStatus(), "金额有误");
        assertEquals(List.of("c.pdf"), taskQueryService.getAttachments(processInstanceId, Set.of(taskId),
                BpmAttachmentTypeEnum.TASK_ATTACHMENT).stream().map(Attachment::getName).toList());
        assertEquals(taskId, getRunningTask(processInstanceId).getId());
        assertEquals(List.of(taskId), getRevisionTaskIds(2L));
        assertEquals(List.of(), getRevisionTaskIds(4L));
        // 调用，并断言异常：task1 隐藏、未配置权限（默认只读）的字段，不能修改
        String revisionId = getRevisionId(taskId);
        assertServiceException(() -> taskUpdateService.resubmitTask(2L, new BpmTaskResubmitReqVO().setTaskId(taskId)
                .setRevisionId(revisionId).setVariables(Map.of("remark", "已修改"))), TASK_RESUBMIT_FIELD_NOT_WRITABLE, "remark");
        assertServiceException(() -> taskUpdateService.resubmitTask(2L, new BpmTaskResubmitReqVO().setTaskId(taskId)
                .setRevisionId(revisionId).setVariables(Map.of("items", List.of()))), TASK_RESUBMIT_FIELD_NOT_WRITABLE, "items");
        // 调用，并断言异常：配置 props.field 的子表单可编辑时，也不能是包含 tag 的对象
        assertServiceException(() -> taskUpdateService.resubmitTask(2L, new BpmTaskResubmitReqVO().setTaskId(taskId)
                .setRevisionId(revisionId).setVariables(Map.of("tags", List.of(Map.of("tag", "c"))))), TASK_RESUBMIT_FIELD_NOT_WRITABLE, "tags");
        // 调用，并断言异常：tag 是普通输入框，每行的值不能是对象、数组
        assertServiceException(() -> taskUpdateService.resubmitTask(2L, new BpmTaskResubmitReqVO().setTaskId(taskId)
                .setRevisionId(revisionId).setVariables(Map.of("tags", List.of(Map.of("note", "c"))))), TASK_RESUBMIT_FIELD_NOT_WRITABLE, "tags");
        assertServiceException(() -> taskUpdateService.resubmitTask(2L, new BpmTaskResubmitReqVO().setTaskId(taskId)
                .setRevisionId(revisionId).setVariables(Map.of("tags", List.of(List.of("c"))))), TASK_RESUBMIT_FIELD_NOT_WRITABLE, "tags");

        // 调用：用户 2 修改后，重新提交
        taskUpdateService.resubmitTask(2L, new BpmTaskResubmitReqVO().setTaskId(taskId).setRevisionId(revisionId)
                .setVariables(Map.of("amount", 300)));
        // 断言：不重新经过 task1，回到 task2 审批
        assertEquals(300, runtimeService.getVariable(processInstanceId, "amount"));
        assertEquals(taskId, getRunningTask(processInstanceId).getId());
        taskUpdateService.approveTask(3L, new BpmTaskApproveReqVO().setId(taskId).setReason("同意").setRevisionId(revisionId));
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
    }

    @Test
    public void testRejectTask_resumeSource_customFormFallback() {
        // 准备参数：业务表单，修改人无法在流程详情中修改表单
        String processInstanceId = startRevisionProcessInstance(BpmModelFormTypeEnum.CUSTOM);
        String taskId = getRunningTask(processInstanceId).getId();

        // 调用：用户 3 审批不通过
        taskUpdateService.rejectTask(3L, new BpmTaskRejectReqVO().setId(taskId).setReason("不同意"));
        // 断言：与【重新依次审批】一致，task2 标记为退回，流程回到 task1，重新生成用户 2、用户 4 的或签任务
        assertTaskStatus(taskId, BpmTaskStatusEnum.RETURN.getStatus(), "不同意");
        assertEquals(List.of("2", "4"), taskQueryService.getRunningTaskListByProcessInstanceId(processInstanceId, null, "task1")
                .stream().map(Task::getAssignee).sorted().toList());
        assertEquals(List.of(), getRevisionTaskIds(2L));
    }

    @Test
    public void testReturnTask_resumeSource_delegatedTask() {
        // 准备参数：用户 3 将 task2 委派给用户 5
        String processInstanceId = startRevisionProcessInstance(BpmModelFormTypeEnum.NORMAL);
        String taskId = getRunningTask(processInstanceId).getId();
        taskUpdateService.delegateTask(3L, new BpmTaskDelegateReqVO().setId(taskId)
                .setDelegateUserId(5L).setReason("请帮忙审批"));

        // 调用：委派中（PENDING），被委派人退回到发起人节点，发起人重新提交
        taskUpdateService.returnTask(5L, new BpmTaskReturnReqVO().setId(taskId)
                .setTargetTaskDefinitionKey(BpmnModelConstants.START_USER_NODE_ID).setReason("请修改"));
        assertEquals(BpmTaskStatusEnum.WAIT_REVISION.getStatus(), getTaskStatus(taskId));
        String revisionId = getRevisionId(taskId);
        taskUpdateService.resubmitTask(1L, new BpmTaskResubmitReqVO().setTaskId(taskId).setRevisionId(revisionId));
        // 断言：仍由被委派人审批
        Task delegatedTask = taskQueryService.getTask(taskId);
        assertEquals("5", delegatedTask.getAssignee());
        assertEquals(DelegationState.PENDING, delegatedTask.getDelegationState());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), FlowableUtils.getTaskStatus(delegatedTask));

        // 调用：被委派人审批，归还给用户 3
        taskUpdateService.approveTask(5L, new BpmTaskApproveReqVO().setId(taskId).setReason("已处理").setRevisionId(revisionId));
        Task resolvedTask = taskQueryService.getTask(taskId);
        assertEquals("3", resolvedTask.getAssignee());
        assertEquals(DelegationState.RESOLVED, resolvedTask.getDelegationState());

        // 调用：委派已归还（RESOLVED），用户 3 驳回到 task1
        taskUpdateService.rejectTask(3L, new BpmTaskRejectReqVO().setId(taskId).setReason("金额有误").setRevisionId(revisionId));
        // 断言：同样退回修改，由 task1 的用户 2 修改；重新提交后，用户 3 审批通过，流程结束
        assertEquals(BpmTaskStatusEnum.WAIT_REVISION.getStatus(), getTaskStatus(taskId));
        String revisionId2 = getRevisionId(taskId);
        taskUpdateService.resubmitTask(2L, new BpmTaskResubmitReqVO().setTaskId(taskId).setRevisionId(revisionId2));
        taskUpdateService.approveTask(3L, new BpmTaskApproveReqVO().setId(taskId).setReason("同意").setRevisionId(revisionId2));
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
    }

    @Test
    public void testProcessTaskTimeout_waitRevision() {
        // 准备参数：用户 3 将 task2 退回到发起人节点
        String processInstanceId = startRevisionProcessInstance(BpmModelFormTypeEnum.NORMAL);
        String taskId = getRunningTask(processInstanceId).getId();
        taskUpdateService.returnTask(3L, new BpmTaskReturnReqVO().setId(taskId)
                .setTargetTaskDefinitionKey(BpmnModelConstants.START_USER_NODE_ID).setReason("请修改"));

        // 调用：超时提醒
        taskUpdateService.processTaskTimeout(processInstanceId, "task2", BpmUserTaskTimeoutHandlerTypeEnum.REMINDER.getType());
        // 断言：等待修改期间，审批人不能处理，所以不提醒
        verify(messageService, never()).sendMessageWhenTaskTimeout(any());
        assertEquals(BpmTaskStatusEnum.WAIT_REVISION.getStatus(), getTaskStatus(taskId));

        // 调用：超时自动通过
        taskUpdateService.processTaskTimeout(processInstanceId, "task2", BpmUserTaskTimeoutHandlerTypeEnum.APPROVE.getType());
        // 断言：先结束退回修改，再自动通过，流程结束；修改人不能再重新提交
        assertTaskStatus(taskId, BpmTaskStatusEnum.APPROVE.getStatus(), BpmReasonEnum.TIMEOUT_APPROVE.getReason());
        assertFalse(taskQueryService.getHistoricTask(taskId).getTaskLocalVariables()
                .containsKey(BpmnVariableConstants.TASK_VARIABLE_REVISION_ASSIGNEE));
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
        assertEquals(List.of(), getRevisionTaskIds(1L));
        assertServiceException(() -> taskUpdateService.resubmitTask(1L, new BpmTaskResubmitReqVO().setTaskId(taskId)),
                TASK_RESUBMIT_NOT_ALLOWED);
    }

    @Test
    public void testProcessTaskTimeout_waitRevisionReject() {
        // 准备参数：用户 3 将 task2 退回到发起人节点
        String processInstanceId = startRevisionProcessInstance(BpmModelFormTypeEnum.NORMAL);
        String taskId = getRunningTask(processInstanceId).getId();
        taskUpdateService.returnTask(3L, new BpmTaskReturnReqVO().setId(taskId)
                .setTargetTaskDefinitionKey(BpmnModelConstants.START_USER_NODE_ID).setReason("请修改"));

        // 调用：超时自动拒绝
        taskUpdateService.processTaskTimeout(processInstanceId, "task2", BpmUserTaskTimeoutHandlerTypeEnum.REJECT.getType());
        // 断言：结束退回修改后，按常规的驳回处理：task2 标记为退回，流程回到驳回节点 task1，不再等待修改
        assertTaskStatus(taskId, BpmTaskStatusEnum.RETURN.getStatus(), BpmReasonEnum.REJECT_TASK.getReason());
        assertFalse(taskQueryService.getHistoricTask(taskId).getTaskLocalVariables()
                .containsKey(BpmnVariableConstants.TASK_VARIABLE_REVISION_ASSIGNEE));
        assertEquals(List.of("2", "4"), taskQueryService.getRunningTaskListByProcessInstanceId(processInstanceId, null, "task1")
                .stream().map(Task::getAssignee).sorted().toList());
        assertEquals(List.of(), getRevisionTaskIds(1L));
        assertEquals(List.of(), getRevisionTaskIds(2L));
    }

    @Test
    public void testResubmitAndApprove_expiredRevision() {
        // 准备参数：从未退回修改的任务，没有批次
        String processInstanceId = startRevisionProcessInstance(BpmModelFormTypeEnum.NORMAL);
        String taskId = getRunningTask(processInstanceId).getId();
        assertNull(taskQueryService.getTodoTask(3L, taskId, processInstanceId).getRevisionId());
        // 准备参数：第一次退回修改，发起人重新提交；重新提交后保留批次，审批人的待办返回该批次
        taskUpdateService.returnTask(3L, new BpmTaskReturnReqVO().setId(taskId)
                .setTargetTaskDefinitionKey(BpmnModelConstants.START_USER_NODE_ID).setReason("第一次修改"));
        String revisionId1 = getRevisionId(taskId);
        assertNotNull(revisionId1);
        taskUpdateService.resubmitTask(1L, new BpmTaskResubmitReqVO().setTaskId(taskId).setRevisionId(revisionId1)
                .setVariables(Map.of("amount", 200)));
        assertEquals(revisionId1, taskQueryService.getTodoTask(3L, taskId, processInstanceId).getRevisionId());

        // 调用，并断言异常：审批人使用退回修改前的页面（没有批次）审批，不能覆盖修改后的表单
        assertServiceException(() -> taskUpdateService.approveTask(3L, new BpmTaskApproveReqVO().setId(taskId)
                .setReason("同意").setVariables(Map.of("amount", 100))), TASK_REVISION_EXPIRED);
        assertServiceException(() -> taskUpdateService.rejectTask(3L, new BpmTaskRejectReqVO().setId(taskId)
                .setReason("不同意")), TASK_REVISION_EXPIRED);
        assertEquals(200, runtimeService.getVariable(processInstanceId, "amount"));

        // 准备参数：第二次退回修改，生成新的批次
        taskUpdateService.returnTask(3L, new BpmTaskReturnReqVO().setId(taskId)
                .setTargetTaskDefinitionKey(BpmnModelConstants.START_USER_NODE_ID).setReason("第二次修改"));
        String revisionId2 = getRevisionId(taskId);
        assertNotEquals(revisionId1, revisionId2);
        // 调用，并断言异常：第一次退回修改的页面，不能提交到第二次
        assertServiceException(() -> taskUpdateService.resubmitTask(1L, new BpmTaskResubmitReqVO().setTaskId(taskId)
                .setRevisionId(revisionId1).setVariables(Map.of("amount", 300))), TASK_REVISION_EXPIRED);
        assertEquals(200, runtimeService.getVariable(processInstanceId, "amount"));
        assertEquals(BpmTaskStatusEnum.WAIT_REVISION.getStatus(), getTaskStatus(taskId));
        // 调用：使用第二次的批次，重新提交
        taskUpdateService.resubmitTask(1L, new BpmTaskResubmitReqVO().setTaskId(taskId).setRevisionId(revisionId2)
                .setVariables(Map.of("amount", 300)));
        // 调用，并断言异常：审批人使用第一次的页面审批
        assertServiceException(() -> taskUpdateService.approveTask(3L, new BpmTaskApproveReqVO().setId(taskId)
                .setReason("同意").setRevisionId(revisionId1)), TASK_REVISION_EXPIRED);

        // 调用：审批人使用第二次的批次审批
        taskUpdateService.approveTask(3L, new BpmTaskApproveReqVO().setId(taskId).setReason("同意").setRevisionId(revisionId2));
        // 断言：流程结束，表单为第二次修改的值
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
        assertEquals(300, getHistoricProcessVariable(processInstanceId, "amount"));
    }

    @Test
    public void testRejectTask_resumeSource_multiInstanceFallback() {
        // 准备参数：task2 会签，用户 3 已经审批通过，用户 5 待审批
        String processInstanceId = startRevisionProcessInstance(BpmModelFormTypeEnum.NORMAL,
                new LinkedHashSet<>(List.of(2L, 4L)), new LinkedHashSet<>(List.of(3L, 5L)));
        Map<String, Task> task2Map = taskQueryService.getRunningTaskListByProcessInstanceId(processInstanceId, null, "task2")
                .stream().collect(Collectors.toMap(Task::getAssignee, Function.identity()));
        assertEquals(Set.of("3", "5"), task2Map.keySet());
        taskUpdateService.approveTask(3L, new BpmTaskApproveReqVO().setId(task2Map.get("3").getId()).setReason("同意"));
        String taskId = task2Map.get("5").getId();

        // 调用：用户 5 审批不通过
        taskUpdateService.rejectTask(5L, new BpmTaskRejectReqVO().setId(taskId).setReason("金额有误"));
        // 断言：多人审批的节点，不退回修改，与【重新依次审批】一致：task2 标记为退回，流程回到 task1，重新生成或签任务
        assertTaskStatus(taskId, BpmTaskStatusEnum.RETURN.getStatus(), "金额有误");
        assertEquals(List.of("2", "4"), taskQueryService.getRunningTaskListByProcessInstanceId(processInstanceId, null, "task1")
                .stream().map(Task::getAssignee).sorted().toList());
        assertEquals(List.of(), getRevisionTaskIds(2L));
    }

    @Test
    public void testReturnTask_resumeSource_targetWithoutAssigneeFallback() {
        // 准备参数：task1 配置为【自动通过】，没有处理人
        mockUserTaskExtensionElement("task1", BpmnModelConstants.USER_TASK_APPROVE_TYPE,
                BpmUserTaskApproveTypeEnum.AUTO_APPROVE.getType());
        String processInstanceId = startRevisionProcessInstance(BpmModelFormTypeEnum.NORMAL, Set.of(), Set.of(3L));
        HistoricTaskInstance task1 = getHistoricTask(processInstanceId, "task1");
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.APPROVE.getStatus(),
                BpmReasonEnum.APPROVE_TYPE_AUTO_APPROVE.getReason());
        assertNull(task1.getAssignee());
        String taskId = getRunningTask(processInstanceId).getId();

        // 调用：用户 3 退回到 task1
        taskUpdateService.returnTask(3L, new BpmTaskReturnReqVO().setId(taskId)
                .setTargetTaskDefinitionKey("task1").setReason("请修改"));
        // 断言：目标节点没有修改人，与【重新依次审批】一致：task2 标记为退回，流程跳转，不进入退回修改
        assertTaskStatus(taskId, BpmTaskStatusEnum.RETURN.getStatus(), "请修改");
        assertNull(taskQueryService.getTask(taskId));
        assertFalse(taskQueryService.getHistoricTask(taskId).getTaskLocalVariables()
                .containsKey(BpmnVariableConstants.TASK_VARIABLE_REVISION_ASSIGNEE));
    }

    @Test
    public void testGetApprovalDetail_revisionWithoutTaskIdAndExpiredLink() {
        // 准备参数：用户 3 驳回到 task1，由用户 2 修改
        String processInstanceId = startRevisionProcessInstance(BpmModelFormTypeEnum.NORMAL);
        String taskId = getRunningTask(processInstanceId).getId();
        taskUpdateService.rejectTask(3L, new BpmTaskRejectReqVO().setId(taskId).setReason("金额有误"));

        // 调用：不传 taskId，例如说从【我的流程】进入
        BpmApprovalDetailRespVO detail = processInstanceQueryService.getApprovalDetail(2L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId));
        // 断言：找到用户 2 在该流程实例中待修改的任务，使用 task1 的表单权限
        assertEquals(taskId, detail.getRevisionTaskId());
        assertEquals(getRevisionId(taskId), detail.getRevisionId());
        assertEquals(Map.of("amount", "2", "remark", "3", "tags", "2"), detail.getFormFieldsPermission());

        // 调用：重新提交后，使用过期的修改链接（taskId 是审批人的任务）
        taskUpdateService.resubmitTask(2L, new BpmTaskResubmitReqVO().setTaskId(taskId).setRevisionId(detail.getRevisionId())
                .setVariables(Map.of("amount", 300)));
        BpmApprovalDetailRespVO expiredDetail = processInstanceQueryService.getApprovalDetail(2L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId).setTaskId(taskId));
        // 断言：不能再修改，使用用户 2 办理过的 task1 的表单权限：可编辑降级为只读，隐藏的字段仍然隐藏
        assertNull(expiredDetail.getRevisionTaskId());
        assertNull(expiredDetail.getRevisionId());
        assertNull(expiredDetail.getTodoTask());
        assertEquals(Map.of("amount", "1", "remark", "3", "tags", "1"), expiredDetail.getFormFieldsPermission());
        // 调用 + 断言：不传 taskId（不是发起人），同样使用用户 2 审批通过的 task1 的表单权限，隐藏的字段仍然隐藏
        assertEquals(Map.of("amount", "1", "remark", "3", "tags", "1"), processInstanceQueryService.getApprovalDetail(2L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId)).getFormFieldsPermission());
    }

    @Test
    public void testDelegateTask_thenApprove() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用：委派给用户 2
        taskUpdateService.delegateTask(1L, new BpmTaskDelegateReqVO().setId(task1.getId())
                .setDelegateUserId(2L).setReason("请帮忙审批"));
        // 断言
        Task delegatedTask = taskQueryService.getTask(task1.getId());
        assertEquals("1", delegatedTask.getOwner());
        assertEquals("2", delegatedTask.getAssignee());
        assertEquals(DelegationState.PENDING, delegatedTask.getDelegationState());
        verify(commentService).createComment(eq(task1.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.DELEGATE_START), eq("用户1"), eq("用户2"), eq("请帮忙审批"));

        // 调用：被委派人审批，任务归还给原审批人
        taskUpdateService.approveTask(2L, new BpmTaskApproveReqVO().setId(task1.getId()).setReason("已处理"));
        // 断言：任务仍在 task1，审批人恢复为用户 1
        Task resolvedTask = taskQueryService.getTask(task1.getId());
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
        taskUpdateService.delegateTask(1L, new BpmTaskDelegateReqVO().setId(task1.getId())
                .setDelegateUserId(2L).setReason("请帮忙审批"));

        // 调用：用户 2 再委派给用户 3
        taskUpdateService.delegateTask(2L, new BpmTaskDelegateReqVO().setId(task1.getId())
                .setDelegateUserId(3L).setReason("转给用户 3"));
        // 断言：owner 仍为最初的审批人
        Task delegatedTask = taskQueryService.getTask(task1.getId());
        assertEquals("1", delegatedTask.getOwner());
        assertEquals("3", delegatedTask.getAssignee());
        verify(commentService).createComment(eq(task1.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.DELEGATE_START), eq("用户2"), eq("用户3"), eq("转给用户 3"));

        // 调用：用户 3 审批，任务先归还给直接委派人用户 2
        taskUpdateService.approveTask(3L, new BpmTaskApproveReqVO().setId(task1.getId()).setReason("用户 3 已处理"));
        // 断言：仍处于委派中，节点没有推进
        Task returnedTask = taskQueryService.getTask(task1.getId());
        assertEquals("2", returnedTask.getAssignee());
        assertEquals("1", returnedTask.getOwner());
        assertEquals(DelegationState.PENDING, returnedTask.getDelegationState());
        verify(commentService).createComment(eq(task1.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.DELEGATE_END), eq("用户3"), eq("用户2"), eq("用户 3 已处理"));

        // 调用：用户 2 审批，任务再归还给用户 1
        taskUpdateService.approveTask(2L, new BpmTaskApproveReqVO().setId(task1.getId()).setReason("用户 2 已处理"));
        // 断言：委派结束，节点仍未推进
        Task resolvedTask = taskQueryService.getTask(task1.getId());
        assertEquals("1", resolvedTask.getAssignee());
        assertEquals(DelegationState.RESOLVED, resolvedTask.getDelegationState());
        assertFalse(resolvedTask.getTaskLocalVariables().containsKey(BpmnVariableConstants.TASK_DELEGATE_RETURN_USERS));
        verify(commentService).createComment(eq(task1.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.DELEGATE_END), eq("用户2"), eq("用户1"), eq("用户 2 已处理"));

        // 调用：用户 1 审批，节点完成
        taskUpdateService.approveTask(1L, new BpmTaskApproveReqVO().setId(task1.getId()).setReason("同意"));
        // 断言
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testDelegateTask_legacyWithoutReturnUsers() {
        // 准备参数：历史数据，委派时没有记录归还顺序
        String processInstanceId = startProcessInstance();
        String taskId = getRunningTask(processInstanceId).getId();
        flowableTaskService.delegateTask(taskId, "2");

        // 调用：用户 2 审批
        taskUpdateService.approveTask(2L, new BpmTaskApproveReqVO().setId(taskId).setReason("已处理"));
        // 断言：沿用 owner 归还给用户 1
        Task resolvedTask = taskQueryService.getTask(taskId);
        assertEquals("1", resolvedTask.getAssignee());
        assertEquals(DelegationState.RESOLVED, resolvedTask.getDelegationState());
    }

    @Test
    public void testDelegateTask_afterTransfer() {
        // 准备参数：用户 1 转办给用户 2，用户 2 再委派给用户 3
        String processInstanceId = startProcessInstance();
        String taskId = getRunningTask(processInstanceId).getId();
        taskUpdateService.transferTask(1L, new BpmTaskTransferReqVO().setId(taskId).setAssigneeUserId(2L).setReason("转办"));
        taskUpdateService.delegateTask(2L, new BpmTaskDelegateReqVO().setId(taskId).setDelegateUserId(3L).setReason("委派"));

        // 调用：用户 3 审批
        taskUpdateService.approveTask(3L, new BpmTaskApproveReqVO().setId(taskId).setReason("已处理"));
        // 断言：归还给直接委派人用户 2，owner 仍为最初的审批人用户 1
        Task resolvedTask = taskQueryService.getTask(taskId);
        assertEquals("2", resolvedTask.getAssignee());
        assertEquals("1", resolvedTask.getOwner());
        assertEquals(DelegationState.RESOLVED, resolvedTask.getDelegationState());
    }

    @Test
    public void testDelegateTask_userRepeat() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用，并断言异常
        assertServiceException(() -> taskUpdateService.delegateTask(1L, new BpmTaskDelegateReqVO().setId(task1.getId())
                .setDelegateUserId(1L).setReason("委派")), TASK_DELEGATE_FAIL_USER_REPEAT);
    }

    @Test
    public void testTransferTask_success() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用
        taskUpdateService.transferTask(1L, new BpmTaskTransferReqVO().setId(task1.getId())
                .setAssigneeUserId(2L).setReason("转办"));
        // 断言
        Task transferredTask = taskQueryService.getTask(task1.getId());
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
        taskUpdateService.transferTask(1L, new BpmTaskTransferReqVO().setId(task1.getId())
                .setAssigneeUserId(2L).setReason("转办"));
        // 断言：用户 2 成为审批人
        Task transferredTask = taskQueryService.getTask(task1.getId());
        assertEquals("2", transferredTask.getAssignee());
        assertNull(transferredTask.getOwner());
        // 调用 + 断言：用户 2 审批通过，流程流转到 task2
        when(taskCandidateInvoker.calculateUsersByTask(any())).thenReturn(Set.of(1L));
        taskUpdateService.approveTask(2L, new BpmTaskApproveReqVO().setId(task1.getId()).setReason("同意"));
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testCreateSignTask_before_thenApproveChild() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用：向前加签给用户 2
        taskUpdateService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                .setType(BpmTaskSignTypeEnum.BEFORE.getType()).setUserIds(Set.of(2L)).setReason("加签"));
        // 断言：父任务等待子任务审批，子任务分配给用户 2
        Task parentTask = taskQueryService.getTask(task1.getId());
        assertNull(parentTask.getAssignee());
        assertEquals("1", parentTask.getOwner());
        assertEquals(BpmTaskSignTypeEnum.BEFORE.getType(), parentTask.getScopeType());
        assertEquals(BpmTaskStatusEnum.WAIT.getStatus(),
                parentTask.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_STATUS));
        List<Task> childTasks = taskQueryService.getTaskListByParentTaskId(task1.getId());
        assertEquals(1, childTasks.size());
        assertEquals("2", childTasks.get(0).getAssignee());
        verify(commentService).createComment(eq(task1.getId()), eq(processInstanceId),
                eq(BpmCommentTypeEnum.ADD_SIGN), eq("用户1"), eq("向前加签"), eq("用户2"), eq("加签"));

        // 调用：用户 2 审批子任务
        taskUpdateService.approveTask(2L, new BpmTaskApproveReqVO().setId(childTasks.get(0).getId()).setReason("同意"));
        // 断言：父任务恢复给用户 1 审批
        Task resolvedTask = taskQueryService.getTask(task1.getId());
        assertEquals("1", resolvedTask.getAssignee());
        assertNull(resolvedTask.getScopeType());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(),
                resolvedTask.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_STATUS));
        assertTrue(taskQueryService.getTaskListByParentTaskId(task1.getId()).isEmpty());
    }

    @Test
    public void testCreateSignTask_after_thenApprove() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用：向后加签给用户 2
        taskUpdateService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                .setType(BpmTaskSignTypeEnum.AFTER.getType()).setUserIds(Set.of(2L)).setReason("加签"));
        // 断言：子任务暂由用户 2 拥有，等待父任务审批
        Task childTask = taskQueryService.getTaskListByParentTaskId(task1.getId()).get(0);
        assertEquals("2", childTask.getOwner());
        assertNull(childTask.getAssignee());
        assertEquals(BpmTaskStatusEnum.WAIT.getStatus(), getTaskStatus(childTask.getId()));

        // 调用：用户 1 审批父任务，激活子任务
        taskUpdateService.approveTask(1L, new BpmTaskApproveReqVO().setId(task1.getId()).setReason("同意"));
        // 断言：父任务处于审批通过中，子任务分配给用户 2
        assertEquals(BpmTaskStatusEnum.APPROVING.getStatus(), getTaskStatus(task1.getId()));
        assertEquals("2", taskQueryService.getTask(childTask.getId()).getAssignee());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), getTaskStatus(childTask.getId()));

        // 调用：用户 2 审批子任务
        taskUpdateService.approveTask(2L, new BpmTaskApproveReqVO().setId(childTask.getId()).setReason("同意"));
        // 断言：父任务自动完成，流程流转到 task2
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.APPROVE.getStatus(), "同意");
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testCreateSignTask_typeError() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        taskUpdateService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                .setType(BpmTaskSignTypeEnum.AFTER.getType()).setUserIds(Set.of(2L)).setReason("加签"));

        // 调用，并断言异常
        assertServiceException(() -> taskUpdateService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                        .setType(BpmTaskSignTypeEnum.BEFORE.getType()).setUserIds(Set.of(3L)).setReason("加签")),
                TASK_SIGN_CREATE_TYPE_ERROR, "向后加签", "向前加签");
    }

    @Test
    public void testCreateSignTask_userRepeat() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用，并断言异常：用户 1 已经是当前节点的审批人
        assertServiceException(() -> taskUpdateService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                        .setType(BpmTaskSignTypeEnum.BEFORE.getType()).setUserIds(Set.of(1L)).setReason("加签")),
                TASK_SIGN_CREATE_USER_REPEAT, "用户1");
    }

    @Test
    public void testDeleteSignTask_success() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        taskUpdateService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                .setType(BpmTaskSignTypeEnum.BEFORE.getType()).setUserIds(Set.of(2L)).setReason("加签"));
        Task childTask = taskQueryService.getTaskListByParentTaskId(task1.getId()).get(0);

        // 调用
        taskUpdateService.deleteSignTask(1L, new BpmTaskSignDeleteReqVO().setId(childTask.getId()).setReason("减签"));
        // 断言：子任务被取消删除，父任务恢复给用户 1 审批
        assertNull(taskQueryService.getTask(childTask.getId()));
        assertEquals(BpmTaskStatusEnum.CANCEL.getStatus(), getTaskStatus(childTask.getId()));
        Task parentTask = taskQueryService.getTask(task1.getId());
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
        taskUpdateService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                .setType(BpmTaskSignTypeEnum.BEFORE.getType()).setUserIds(Set.of(2L, 3L)).setReason("加签"));
        Map<String, Task> childTasks = taskQueryService.getTaskListByParentTaskId(task1.getId()).stream()
                .collect(Collectors.toMap(Task::getAssignee, Function.identity()));

        // 调用：减签用户 2
        taskUpdateService.deleteSignTask(1L, new BpmTaskSignDeleteReqVO().setId(childTasks.get("2").getId())
                .setReason("减签"));
        // 断言：用户 3 的加签任务仍在，父任务继续等待
        assertEquals(BpmTaskStatusEnum.CANCEL.getStatus(), getTaskStatus(childTasks.get("2").getId()));
        assertEquals(List.of(childTasks.get("3").getId()), taskQueryService.getTaskListByParentTaskId(task1.getId())
                .stream().map(Task::getId).toList());
        Task parentTask = taskQueryService.getTask(task1.getId());
        assertNull(parentTask.getAssignee());
        assertEquals(BpmTaskSignTypeEnum.BEFORE.getType(), parentTask.getScopeType());

        // 调用 + 断言：用户 3 审批通过后，父任务恢复给用户 1 审批
        taskUpdateService.approveTask(3L, new BpmTaskApproveReqVO().setId(childTasks.get("3").getId()).setReason("同意"));
        assertEquals("1", taskQueryService.getTask(task1.getId()).getAssignee());
    }

    @Test
    public void testDeleteSignTask_afterSignNotApproved() {
        // 准备参数：task1 向后加签给用户 2，但 task1 尚未审批
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        taskUpdateService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                .setType(BpmTaskSignTypeEnum.AFTER.getType()).setUserIds(Set.of(2L)).setReason("加签"));
        Task childTask = taskQueryService.getTaskListByParentTaskId(task1.getId()).get(0);

        // 调用
        taskUpdateService.deleteSignTask(1L, new BpmTaskSignDeleteReqVO().setId(childTask.getId()).setReason("减签"));
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
        assertServiceException(() -> taskUpdateService.deleteSignTask(1L,
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
        taskUpdateService.withdrawTask(1L, task1.getId());
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
        assertServiceException(() -> taskUpdateService.withdrawTask(1L, task1.getId()), TASK_WITHDRAW_FAIL_TASK_NOT_EXISTS);
    }

    @Test
    public void testWithdrawTask_nextTaskApproved() {
        // 准备参数：task1、task2 均已审批，流程已经结束
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        approveRunningTask(processInstanceId);
        approveRunningTask(processInstanceId);

        // 调用，并断言异常
        assertServiceException(() -> taskUpdateService.withdrawTask(1L, task1.getId()),
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
        assertServiceException(() -> taskUpdateService.withdrawTask(1L, task1.getId()), TASK_WITHDRAW_FAIL_NOT_ALLOW);
    }

    @Test
    public void testWithdrawTask_nextTaskApprovedInSameMillisecond() {
        // 准备参数：固定 Flowable 时钟，使 task1 的结束时间、task2 的创建时间相同
        processEngineConfiguration.getClock().setCurrentTime(new Date());
        try {
            // 用户 2 审批通过 task1，流转到 task2 会签（用户 3、用户 5）
            String processInstanceId = startRevisionProcessInstance(BpmModelFormTypeEnum.NORMAL,
                    Set.of(2L), new LinkedHashSet<>(List.of(3L, 5L)));
            doAnswer(invocation -> new BpmProcessDefinitionInfoDO().setProcessDefinitionId(invocation.getArgument(0))
                    .setModelType(BpmModelTypeEnum.BPMN.getType()).setAllowWithdrawTask(true))
                    .when(processDefinitionService).getProcessDefinitionInfo(anyString());
            HistoricTaskInstance task1 = getHistoricTask(processInstanceId, "task1");
            // 用户 3 审批通过 task2，用户 5 的任务仍在审批中
            Task task2 = taskQueryService.getRunningTaskListByProcessInstanceId(processInstanceId, null, "task2").stream()
                    .filter(task -> "3".equals(task.getAssignee())).findFirst().orElseThrow();
            taskUpdateService.approveTask(3L, new BpmTaskApproveReqVO().setId(task2.getId()).setReason("同意"));
            assertEquals(task1.getEndTime().getTime(), taskQueryService.getHistoricTask(task2.getId()).getCreateTime().getTime());

            // 调用，并断言异常：下一个节点已经有人审批，不允许撤回
            assertServiceException(() -> taskUpdateService.withdrawTask(2L, task1.getId()),
                    TASK_WITHDRAW_FAIL_NEXT_TASK_NOT_ALLOW);
        } finally {
            processEngineConfiguration.getClock().reset();
        }
    }

    @Test
    public void testMoveTaskToEnd_success() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用
        taskUpdateService.moveTaskToEnd(processInstanceId, "结束流程");
        // 断言：运行中的任务被取消，流程实例结束
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.CANCEL.getStatus(), "系统自动取消");
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
    }

    @Test
    public void testMoveTaskToEnd_withSignTask() {
        // 准备参数：task1 向前加签给用户 2，子任务未审批
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        taskUpdateService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                .setType(BpmTaskSignTypeEnum.BEFORE.getType()).setUserIds(Set.of(2L)).setReason("加签"));
        Task childTask = taskQueryService.getTaskListByParentTaskId(task1.getId()).get(0);

        // 调用
        taskUpdateService.moveTaskToEnd(processInstanceId, "结束流程");
        // 断言：父任务、子任务都被取消，流程实例结束
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.CANCEL.getStatus(), "系统自动取消");
        assertTaskStatus(childTask.getId(), BpmTaskStatusEnum.CANCEL.getStatus(), "系统自动取消");
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
        assertNull(taskQueryService.getTask(childTask.getId()));
    }

    @Test
    public void testProcessTaskTimeout_approve() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用：task1 超时自动通过
        taskUpdateService.processTaskTimeout(processInstanceId, "task1",
                BpmUserTaskTimeoutHandlerTypeEnum.APPROVE.getType());
        // 断言
        assertTaskStatus(task1.getId(), BpmTaskStatusEnum.APPROVE.getStatus(), BpmReasonEnum.TIMEOUT_APPROVE.getReason());
        assertEquals("task2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testProcessTaskTimeout_approveWithBeforeSignTask() {
        // 准备参数：task1 向前加签给用户 2，父任务等待中（没有审批人）
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        taskUpdateService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(task1.getId())
                .setType(BpmTaskSignTypeEnum.BEFORE.getType()).setUserIds(Set.of(2L)).setReason("加签"));
        Task childTask = taskQueryService.getTaskListByParentTaskId(task1.getId()).get(0);

        // 调用：task1 超时自动通过
        taskUpdateService.processTaskTimeout(processInstanceId, "task1",
                BpmUserTaskTimeoutHandlerTypeEnum.APPROVE.getType());
        // 断言：加签子任务自动通过，父任务恢复给用户 1 继续审批，流程仍停留在 task1
        assertTaskStatus(childTask.getId(), BpmTaskStatusEnum.APPROVE.getStatus(), BpmReasonEnum.TIMEOUT_APPROVE.getReason());
        Task parentTask = getRunningTask(processInstanceId);
        assertEquals(task1.getId(), parentTask.getId());
        assertEquals("1", parentTask.getAssignee());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(),
                parentTask.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_STATUS));
    }

    @Test
    public void testProcessTaskTimeout_reject() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用：task1 超时自动拒绝
        taskUpdateService.processTaskTimeout(processInstanceId, "task1",
                BpmUserTaskTimeoutHandlerTypeEnum.REJECT.getType());
        // 断言：任务不通过，流程实例不通过并结束
        assertEquals(BpmTaskStatusEnum.REJECT.getStatus(), getTaskStatus(task1.getId()));
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
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
                () -> processInstanceUpdateService.createProcessInstance(1L, new BpmProcessInstanceCreateReqVO()
                        .setProcessDefinitionId(processDefinitionId).setVariables(new HashMap<>(Map.of("day", 3)))));
        // 断言
        ProcessInstance processInstance = processInstanceQueryService.getProcessInstance(processInstanceId);
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
        processInstanceUpdateService.cancelProcessInstanceByStartUser(1L, new BpmProcessInstanceCancelReqVO()
                .setId(processInstanceId).setReason("不想请了"));
        // 断言：流程实例取消并结束，运行中的任务被取消
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
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
        assertServiceException(() -> processInstanceUpdateService.cancelProcessInstanceByStartUser(2L,
                new BpmProcessInstanceCancelReqVO().setId(processInstanceId).setReason("取消")),
                PROCESS_INSTANCE_CANCEL_FAIL_NOT_SELF);
        assertNotNull(processInstanceQueryService.getProcessInstance(processInstanceId));
    }

    @Test
    public void testCancelProcessInstanceByAdmin_success() {
        // 准备参数：task1 已审批，流程处于 task2
        String processInstanceId = startProcessInstance();
        approveRunningTask(processInstanceId);

        // 调用
        processInstanceUpdateService.cancelProcessInstanceByAdmin(100L, new BpmProcessInstanceCancelReqVO()
                .setId(processInstanceId).setReason("流程配置错误"));
        // 断言
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
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
        processInstanceUpdateService.updateProcessInstanceVariables(processInstanceId, Map.of("day", 5));
        assertEquals(5, runtimeService.getVariable(processInstanceId, "day"));
        // 调用 + 断言：删除变量
        processInstanceUpdateService.removeProcessInstanceVariables(processInstanceId, Set.of("day"));
        assertFalse(runtimeService.hasVariable(processInstanceId, "day"));
    }

    @Test
    public void testGetTodoTask_success() {
        // 准备参数
        String processInstanceId = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用：不传 taskId 时，按流程实例查询首个待办
        BpmTaskRespVO todoTask = taskQueryService.getTodoTask(1L, null, processInstanceId);
        // 断言
        assertEquals(task1.getId(), todoTask.getId());
        assertEquals("一级审批", todoTask.getName());
        assertFalse(todoTask.getSignEnable());
        assertFalse(todoTask.getReasonRequire());
        // 断言：其他用户没有待办
        assertNull(taskQueryService.getTodoTask(2L, task1.getId(), processInstanceId));
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
        List<Task> todoTasks = taskQueryService.getTaskTodoPage(1L, pageReqVO).getList();
        assertEquals(List.of("task2"), todoTasks.stream().map(Task::getTaskDefinitionKey).toList());
        // 调用 + 断言：已办为 task1
        pageReqVO.setStatus(BpmTaskStatusEnum.APPROVE.getStatus());
        List<HistoricTaskInstance> doneTasks = taskQueryService.getTaskDonePage(1L, pageReqVO).getList();
        assertEquals(List.of("task1"), doneTasks.stream().map(HistoricTaskInstance::getTaskDefinitionKey).toList());
        // 调用 + 断言：其它用户没有待办
        assertEquals(0, taskQueryService.getTaskTodoPage(2L, new BpmTaskPageReqVO()).getTotal());
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
        taskUpdateService.rejectTask(1L, new BpmTaskRejectReqVO().setId(task1.getId()).setReason("不同意"));
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
        processInstanceUpdateService.cancelProcessInstanceByStartUser(1L, new BpmProcessInstanceCancelReqVO()
                .setId(processInstanceId).setReason("不想请了"));
        // 断言：取消时只发送状态事件，不发送通过、不通过消息
        BpmProcessInstanceStatusEvent event = getProcessInstanceStatusEvent(processInstanceId);
        assertEquals(BpmProcessInstanceStatusEnum.CANCEL.getStatus(), event.getStatus());
        assertEquals(BpmReasonEnum.CANCEL_PROCESS_INSTANCE_BY_START_USER.format("不想请了"), event.getReason());
        verify(messageService, never()).sendMessageWhenProcessInstanceApprove(any());
        verify(messageService, never()).sendMessageWhenProcessInstanceReject(any());
    }

    @Test
    public void testProcessCallback_cancelTriggerTask() {
        // 准备参数：流程停留在 receiveTask 等待节点，没有运行中的任务
        deploy("bpmn/trigger-task.bpmn20.xml");
        String processInstanceId = startProcessInstance("triggerTask");

        // 调用
        processInstanceUpdateService.cancelProcessInstanceByStartUser(1L, new BpmProcessInstanceCancelReqVO()
                .setId(processInstanceId).setReason("不想请了"));
        // 断言：流程实例结束，并发送取消的状态事件
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
        assertEquals(0, runtimeService.createExecutionQuery().processInstanceId(processInstanceId).count());
        assertEquals(BpmProcessInstanceStatusEnum.CANCEL.getStatus(), getHistoricProcessVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
        BpmProcessInstanceStatusEvent event = getProcessInstanceStatusEvent(processInstanceId);
        assertEquals(BpmProcessInstanceStatusEnum.CANCEL.getStatus(), event.getStatus());
        assertEquals(BpmReasonEnum.CANCEL_PROCESS_INSTANCE_BY_START_USER.format("不想请了"), event.getReason());
    }

    @Test
    public void testProcessCallback_cancelDelayTimer() {
        // 准备参数：流程停留在延迟器，没有运行中的任务
        deploy("bpmn/delay-timer.bpmn20.xml");
        String processInstanceId = startProcessInstance("delayTimer");

        // 调用
        processInstanceUpdateService.cancelProcessInstanceByAdmin(100L, new BpmProcessInstanceCancelReqVO()
                .setId(processInstanceId).setReason("流程配置错误"));
        // 断言：流程实例结束，延迟器的定时任务一并删除，不会再推进流程
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
        assertEquals(0, managementService.createTimerJobQuery().processInstanceId(processInstanceId).count());
        BpmProcessInstanceStatusEvent event = getProcessInstanceStatusEvent(processInstanceId);
        assertEquals(BpmProcessInstanceStatusEnum.CANCEL.getStatus(), event.getStatus());
        assertEquals(BpmReasonEnum.CANCEL_PROCESS_INSTANCE_BY_ADMIN.format("用户100", "流程配置错误"), event.getReason());
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
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
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
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
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
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
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
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
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
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
        assertEquals(BpmProcessInstanceStatusEnum.REJECT.getStatus(), getHistoricProcessVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
    }

    @Test
    public void testTimerFired_userTaskTimeoutAutoJumpNotSimple() {
        // 准备参数：BPMN 设计器的流程，超时处理方式改为【自动跳转】，没有经过 SIMPLE 模型保存的校验
        deploy("bpmn/timeout-approve.bpmn20.xml");
        mockBoundaryEventTimeoutHandlerType("task1-timeout", BpmUserTaskTimeoutHandlerTypeEnum.AUTO_JUMP);
        String processInstanceId = startProcessInstance("timeoutApprove");
        Task task1 = getRunningTask(processInstanceId);

        // 调用
        executeTimerJob(processInstanceId);
        // 断言：自动跳转仅支持 SIMPLE 设计器，不进行跳转，任务保持审批中
        assertEquals(task1.getId(), getRunningTask(processInstanceId).getId());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), getTaskStatus(task1.getId()));
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
        assertNull(processInstanceQueryService.getProcessInstance(processInstanceId));
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
        assertNull(processInstanceQueryService.getProcessInstance(child.getId()));
        assertNull(processInstanceQueryService.getProcessInstance(parentId));
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
        taskUpdateService.rejectTask(1L, new BpmTaskRejectReqVO().setId(childTask.getId()).setReason("不同意"));
        // 断言：子流程不通过时，主流程同样标记为不通过并结束
        assertNull(processInstanceQueryService.getProcessInstance(child.getId()));
        assertNull(processInstanceQueryService.getProcessInstance(parentId));
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
        processInstanceUpdateService.cancelProcessInstanceByStartUser(1L, new BpmProcessInstanceCancelReqVO()
                .setId(parentId).setReason("不想请了"));
        // 断言：主流程、子流程都被取消
        assertNull(processInstanceQueryService.getProcessInstance(parentId));
        assertNull(processInstanceQueryService.getProcessInstance(child.getId()));
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
        assertServiceException(() -> processInstanceUpdateService.cancelProcessInstanceByStartUser(1L,
                new BpmProcessInstanceCancelReqVO().setId(child.getId()).setReason("取消")),
                PROCESS_INSTANCE_CANCEL_CHILD_FAIL_NOT_ALLOW);
    }

    // ========== 私有方法 ==========

    private String startProcessInstance() {
        return startProcessInstance("twoStepApprove");
    }

    private String startProcessInstance(String processDefinitionKey) {
        return processInstanceUpdateService.createProcessInstance(1L, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey(processDefinitionKey).setBusinessKey("business-1"));
    }

    private void deploy(String resource) {
        extraDeploymentIds.add(repositoryService.createDeployment().addClasspathResource(resource).deploy().getId());
    }

    /**
     * 发起 revision-approve.bpmn20.xml 流程，并由用户 2 审批通过 task1，流转到 task2
     *
     * <p>审批人：发起人节点为用户 1，task1 或签为用户 2、用户 4，task2 为用户 3。
     * 表单：金额 amount、备注 remark、子表单 items（名称 name、价格 price）、配置 props.field 的子表单 tags（默认值 x），
     * 以及与工作流保留变量同名的字段</p>
     *
     * @param formType 表单类型
     * @return 流程实例编号
     */
    private String startRevisionProcessInstance(BpmModelFormTypeEnum formType) {
        return startRevisionProcessInstance(formType, new LinkedHashSet<>(List.of(2L, 4L)), Set.of(3L));
    }

    /**
     * 发起 revision-approve.bpmn20.xml 流程，见 {@link #startRevisionProcessInstance(BpmModelFormTypeEnum)}
     *
     * @param formType     表单类型
     * @param task1UserIds task1 的审批人。包含用户 2 时，由用户 2 审批通过 task1
     * @param task2UserIds task2 的审批人
     * @return 流程实例编号
     */
    private String startRevisionProcessInstance(BpmModelFormTypeEnum formType, Set<Long> task1UserIds, Set<Long> task2UserIds) {
        deploy("bpmn/revision-approve.bpmn20.xml");
        when(taskCandidateInvoker.calculateUsersByTask(any())).thenAnswer(invocation ->
                switch (((DelegateExecution) invocation.getArgument(0)).getCurrentActivityId()) {
                    case "task1" -> task1UserIds;
                    case "task2" -> task2UserIds;
                    default -> Set.of(1L);
                });
        doAnswer(invocation -> new BpmProcessDefinitionInfoDO().setProcessDefinitionId(invocation.getArgument(0))
                .setModelType(BpmModelTypeEnum.BPMN.getType()).setFormType(formType.getType())
                .setFormFields(List.of("{\"type\":\"input\",\"field\":\"amount\",\"title\":\"金额\"}",
                        "{\"type\":\"input\",\"field\":\"remark\",\"title\":\"备注\"}",
                        "{\"type\":\"group\",\"field\":\"items\",\"title\":\"明细\",\"props\":{\"rule\":["
                                + "{\"type\":\"input\",\"field\":\"name\",\"title\":\"名称\"},"
                                + "{\"type\":\"inputNumber\",\"field\":\"price\",\"title\":\"价格\"}]}}",
                        "{\"type\":\"group\",\"field\":\"tags\",\"title\":\"标签\",\"props\":{\"field\":\"tag\",\"defaultValue\":\"x\",\"rule\":["
                                + "{\"type\":\"input\",\"field\":\"tag\",\"title\":\"标签\"},"
                                + "{\"type\":\"input\",\"field\":\"note\",\"title\":\"说明\"}]}}",
                        "{\"type\":\"input\",\"field\":\"PROCESS_STATUS\",\"title\":\"状态\"}",
                        "{\"type\":\"input\",\"field\":\"NEED_SIMULATE_TASK_IDS\",\"title\":\"预测节点\"}",
                        "{\"type\":\"input\",\"field\":\"task1_assignees\",\"title\":\"审批人\"}")))
                .when(processDefinitionService).getProcessDefinitionInfo(anyString());
        Map<String, Object> variables = new HashMap<>();
        variables.put("amount", 100);
        variables.put("remark", "备注");
        variables.put("items", List.of(Map.of("name", "a", "price", 10), Map.of("name", "z", "price", 20)));
        variables.put("tags", List.of("a", "b"));
        String processInstanceId = processInstanceUpdateService.createProcessInstance(1L, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey("revisionApprove").setBusinessKey("business-1").setVariables(variables));
        // 用户 2 审批通过 task1，用户 4 的或签任务被取消
        if (task1UserIds.contains(2L)) {
            Task task1 = taskQueryService.getRunningTaskListByProcessInstanceId(processInstanceId, null, "task1").stream()
                    .filter(task -> "2".equals(task.getAssignee())).findFirst().orElseThrow();
            taskUpdateService.approveTask(2L, new BpmTaskApproveReqVO().setId(task1.getId()).setReason("同意"));
        }
        return processInstanceId;
    }

    private List<String> getRevisionTaskIds(Long userId) {
        return taskQueryService.getRevisionTaskPage(userId, new BpmTaskPageReqVO()).getList().stream()
                .map(Task::getId).toList();
    }

    /**
     * 获得任务当前的退回修改批次，即审批详情、待办任务返回给前端的 revisionId
     */
    private String getRevisionId(String taskId) {
        return (String) flowableTaskService.getVariableLocal(taskId, BpmnVariableConstants.TASK_VARIABLE_REVISION_ID);
    }

    private void assertResubmitFieldNotWritable(String taskId, String field, Object value, String errorField) {
        assertServiceException(() -> taskUpdateService.resubmitTask(1L, new BpmTaskResubmitReqVO().setTaskId(taskId)
                .setRevisionId(getRevisionId(taskId)).setVariables(Collections.singletonMap(field, value))), // 允许 value 为 null
                TASK_RESUBMIT_FIELD_NOT_WRITABLE, errorField);
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
        List<Task> tasks = taskQueryService.getRunningTaskListByProcessInstanceId(processInstanceId, null, null);
        assertEquals(1, tasks.size());
        return tasks.get(0);
    }

    private void approveRunningTask(String processInstanceId) {
        Task task = getRunningTask(processInstanceId);
        taskUpdateService.approveTask(1L, new BpmTaskApproveReqVO().setId(task.getId()).setReason("同意"));
    }

    private Integer getTaskStatus(String taskId) {
        return (Integer) taskQueryService.getHistoricTask(taskId).getTaskLocalVariables()
                .get(BpmnVariableConstants.TASK_VARIABLE_STATUS);
    }

    private void assertTaskStatus(String taskId, Integer status, String reason) {
        HistoricTaskInstance task = taskQueryService.getHistoricTask(taskId);
        assertEquals(status, task.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_STATUS));
        assertEquals(reason, task.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_REASON));
    }

    private Object getHistoricProcessVariable(String processInstanceId, String name) {
        return processInstanceQueryService.getHistoricProcessInstance(processInstanceId).getProcessVariables().get(name);
    }

}
