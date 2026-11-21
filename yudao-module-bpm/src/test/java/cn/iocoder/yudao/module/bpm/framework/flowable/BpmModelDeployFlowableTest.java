package cn.iocoder.yudao.module.bpm.framework.flowable;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.base.user.UserSimpleBaseVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelSaveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelUpdateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessPrintDataRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskReturnReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskSignCreateReqVO;
import cn.iocoder.yudao.module.bpm.convert.task.BpmProcessInstanceConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.definition.BpmProcessDefinitionInfoMapper;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmBoundaryEventTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelFormTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModeConditionTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModelNodeTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskApproveMethodEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskApproveTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskTimeoutHandlerTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmReasonEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskSignTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.SimpleModelUtils;
import cn.iocoder.yudao.module.bpm.service.definition.BpmModelServiceImpl;
import cn.iocoder.yudao.module.bpm.service.definition.BpmProcessDefinitionServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceQueryServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceUpdateServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskQueryServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskUpdateServiceImpl;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.flowable.bpmn.model.BoundaryEvent;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.TimerEventDefinition;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.impl.db.SuspensionState;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.job.api.Job;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.MODEL_DEPLOY_FAIL_TIMEOUT_JUMP_NOT_MATCH;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.MODEL_DEPLOY_FAIL_TIMEOUT_JUMP_NOT_SIMPLE;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.MODEL_SAVE_FAIL_TIMEOUT_JUMP_NODE_INVALID;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.MODEL_UPDATE_FAIL_NOT_MANAGER;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.PROCESS_DEFINITION_NOT_EXISTS;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.BOUNDARY_EVENT_TYPE;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.NODE_TYPE;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.USER_TASK_APPROVE_TYPE;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.USER_TASK_TIMEOUT_HANDLER_TYPE;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.USER_TASK_TIMEOUT_JUMP_NODE_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 流程模型部署、挂起、排序、清理、删除的 Flowable 集成测试
 *
 * <p>使用真实的 {@link BpmModelServiceImpl}、{@link BpmProcessDefinitionServiceImpl}（Flowable + bpm_process_definition_info 表），
 * 并让流程实例 Service 依赖的 {@link #processDefinitionService} 委托给真实实现，验证【模型部署 → 发起 → 审批】的完整闭环。</p>
 *
 * @author HUIHUI
 */
@Sql(scripts = "/sql/flowable/create_tables.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/flowable/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class BpmModelDeployFlowableTest extends BaseFlowableUnitTest {

    private static final String MODEL_KEY = "modelDeploy";
    private static final String MODEL_NAME = "模型部署";

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
    private BpmProcessDefinitionInfoMapper processDefinitionMapper;

    @Resource
    private BpmTaskQueryServiceImpl taskQueryService;
    @Resource
    private BpmTaskUpdateServiceImpl taskUpdateService;
    @Resource
    private BpmProcessInstanceQueryServiceImpl processInstanceQueryService;
    @Resource
    private BpmProcessInstanceUpdateServiceImpl processInstanceUpdateService;

    private final BpmProcessDefinitionServiceImpl processDefinitionServiceImpl = new BpmProcessDefinitionServiceImpl();
    private final BpmModelServiceImpl modelServiceImpl = new BpmModelServiceImpl();
    /**
     * 校验器工厂：测试切片未加载 ValidationAutoConfiguration（避免对其它 Service 开启方法级校验），所以自行创建，测试后关闭
     */
    private ValidatorFactory validatorFactory;

    @BeforeEach
    public void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        // 真实的流程定义、流程模型 Service，依赖的 Flowable、Mapper 使用 Spring 中的真实 Bean
        ReflectionTestUtils.setField(processDefinitionServiceImpl, "repositoryService", repositoryService);
        ReflectionTestUtils.setField(processDefinitionServiceImpl, "processDefinitionMapper", processDefinitionMapper);
        ReflectionTestUtils.setField(processDefinitionServiceImpl, "adminUserApi", adminUserApi);
        ReflectionTestUtils.setField(modelServiceImpl, "repositoryService", repositoryService);
        ReflectionTestUtils.setField(modelServiceImpl, "runtimeService", runtimeService);
        ReflectionTestUtils.setField(modelServiceImpl, "historyService", historyService);
        ReflectionTestUtils.setField(modelServiceImpl, "taskService", flowableTaskService);
        ReflectionTestUtils.setField(modelServiceImpl, "processDefinitionService", processDefinitionServiceImpl);
        ReflectionTestUtils.setField(modelServiceImpl, "bpmFormService", formService);
        ReflectionTestUtils.setField(modelServiceImpl, "taskCandidateInvoker", taskCandidateInvoker);
        ReflectionTestUtils.setField(modelServiceImpl, "processInstanceCopyService", processInstanceCopyService);
        ReflectionTestUtils.setField(modelServiceImpl, "validator", validatorFactory.getValidator());

        // 发起、审批流程时，流程定义相关的查询委托给真实实现，读取部署后写入的 bpm_process_definition_info
        mockFlowableBackedServices(repositoryService);
        doAnswer(delegatesTo(processDefinitionServiceImpl)).when(processDefinitionService)
                .getActiveProcessDefinition(anyString());
        doAnswer(delegatesTo(processDefinitionServiceImpl)).when(processDefinitionService)
                .getProcessDefinition(anyString());
        doAnswer(delegatesTo(processDefinitionServiceImpl)).when(processDefinitionService)
                .getProcessDefinitionInfo(anyString());
        doAnswer(delegatesTo(processDefinitionServiceImpl)).when(processDefinitionService)
                .canUserStartProcessDefinition(any(), any());
    }

    @AfterEach
    public void tearDown() {
        repositoryService.createDeploymentQuery().deploymentKey(MODEL_KEY).list()
                .forEach(deployment -> repositoryService.deleteDeployment(deployment.getId(), true));
        repositoryService.createModelQuery().modelKey(MODEL_KEY).list()
                .forEach(model -> repositoryService.deleteModel(model.getId()));
        validatorFactory.close();
    }

    @Test
    public void testDeployModel_redeploy() {
        // 准备参数：创建 Simple 模型
        String modelId = createModel();

        // 调用：首次部署
        modelServiceImpl.deployModel(1L, modelId);
        // 断言：生成流程定义及拓展信息，模型关联最新部署
        ProcessDefinition definition1 = getActiveProcessDefinition();
        assertEquals("模型部署", definition1.getName());
        assertEquals("OA", definition1.getCategory());
        assertEquals(definition1.getDeploymentId(), repositoryService.getModel(modelId).getDeploymentId());
        BpmProcessDefinitionInfoDO definitionInfo1 = processDefinitionMapper.selectByProcessDefinitionId(definition1.getId());
        assertEquals(modelId, definitionInfo1.getModelId());
        assertEquals(BpmModelTypeEnum.SIMPLE.getType(), definitionInfo1.getModelType());
        assertEquals(List.of(1L), definitionInfo1.getManagerUserIds());
        assertNotNull(definitionInfo1.getSimpleModel());
        // 断言：可基于该流程定义发起流程，停留在审批节点
        String processInstanceId1 = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId1);
        assertEquals(definition1.getId(), task1.getProcessDefinitionId());
        assertEquals("approve", task1.getTaskDefinitionKey());

        // 调用：再次部署
        modelServiceImpl.deployModel(1L, modelId);
        // 断言：老的流程定义被挂起，新发起的流程使用新的流程定义
        ProcessDefinition definition2 = getActiveProcessDefinition();
        assertNotEquals(definition1.getId(), definition2.getId());
        assertTrue(repositoryService.getProcessDefinition(definition1.getId()).isSuspended());
        assertEquals(definition2.getDeploymentId(), repositoryService.getModel(modelId).getDeploymentId());
        String processInstanceId2 = startProcessInstance();
        assertEquals(definition2.getId(), getRunningTask(processInstanceId2).getProcessDefinitionId());

        // 调用 + 断言：老流程定义上进行中的流程不受挂起影响，仍可审批通过
        approveTask(task1);
        assertProcessInstanceStatus(processInstanceId1, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
        approveTask(getRunningTask(processInstanceId2));
        assertProcessInstanceStatus(processInstanceId2, BpmProcessInstanceStatusEnum.APPROVE.getStatus());

        // 调用 + 断言：调整排序，同一模型的所有流程定义拓展信息同步更新
        modelServiceImpl.updateModelSortBatch(1L, List.of(modelId));
        Long sort = processDefinitionMapper.selectByProcessDefinitionId(definition2.getId()).getSort();
        assertEquals(sort, processDefinitionMapper.selectByProcessDefinitionId(definition1.getId()).getSort());
        assertTrue(sort > definitionInfo1.getSort());
    }

    @Test
    public void testDeployModel_notManager() {
        // 准备参数
        String modelId = createModel();

        // 调用，并断言异常：非模型管理员不允许部署
        assertServiceException(() -> modelServiceImpl.deployModel(2L, modelId), MODEL_UPDATE_FAIL_NOT_MANAGER, "模型部署");
        assertEquals(0, repositoryService.createProcessDefinitionQuery().processDefinitionKey(MODEL_KEY).count());
    }

    @Test
    public void testUpdateModelState_suspendAndActive() {
        // 准备参数
        String modelId = createModel();
        modelServiceImpl.deployModel(1L, modelId);
        String processDefinitionId = getActiveProcessDefinition().getId();

        // 调用 + 断言：挂起后不允许发起
        modelServiceImpl.updateModelState(1L, modelId, SuspensionState.SUSPENDED.getStateCode());
        assertTrue(repositoryService.getProcessDefinition(processDefinitionId).isSuspended());
        assertStartProcessInstanceFail(PROCESS_DEFINITION_NOT_EXISTS);
        // 调用 + 断言：激活后恢复发起
        modelServiceImpl.updateModelState(1L, modelId, SuspensionState.ACTIVE.getStateCode());
        assertFalse(repositoryService.getProcessDefinition(processDefinitionId).isSuspended());
        assertEquals("approve", getRunningTask(startProcessInstance()).getTaskDefinitionKey());
    }

    @Test
    public void testCleanModel_success() {
        // 准备参数：一个进行中的流程，一个已结束的流程
        String modelId = createModel();
        modelServiceImpl.deployModel(1L, modelId);
        String runningId = startProcessInstance();
        String finishedId = startProcessInstance();
        approveTask(getRunningTask(finishedId));
        assertProcessInstanceStatus(finishedId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());

        // 调用
        modelServiceImpl.cleanModel(1L, modelId);
        // 断言：流程实例、历史、任务、抄送全部清理，模型与流程定义保留
        assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(MODEL_KEY).count());
        assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(MODEL_KEY).count());
        assertEquals(0, flowableTaskService.createTaskQuery().processDefinitionKey(MODEL_KEY).count());
        verify(processInstanceCopyService).deleteProcessInstanceCopy(runningId);
        verify(processInstanceCopyService).deleteProcessInstanceCopy(finishedId);
        assertNotNull(repositoryService.getModel(modelId));
        assertNotNull(getActiveProcessDefinition());
    }

    @Test
    public void testDeleteModel_success() {
        // 准备参数：一个进行中的流程
        String modelId = createModel();
        modelServiceImpl.deployModel(1L, modelId);
        String processDefinitionId = getActiveProcessDefinition().getId();
        String processInstanceId = startProcessInstance();

        // 调用
        modelServiceImpl.deleteModel(1L, modelId);
        // 断言：模型被删除，流程定义被挂起，不允许再发起
        assertNull(repositoryService.getModel(modelId));
        assertTrue(repositoryService.getProcessDefinition(processDefinitionId).isSuspended());
        assertStartProcessInstanceFail(PROCESS_DEFINITION_NOT_EXISTS);
        // 断言：进行中的流程不受影响，仍可审批通过
        approveTask(getRunningTask(processInstanceId));
        assertProcessInstanceStatus(processInstanceId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
    }

    // ========== 审批超时自动跳转相关（模型保存 → 部署 → 定时器 → BpmTaskEventListener#timerFired） ==========

    @Test
    public void testTimeoutJump_sameConditionBranch() {
        // 准备参数：金额 200 走 B1 分支，B1 超时跳转到同一分支的 B2
        String processInstanceId = deployAndStartTimeoutJumpModel("approve-b1", "approve-b2", 200);
        approveTask(getRunningTask(processInstanceId)); // 审批 A
        Task taskB1 = getRunningTask(processInstanceId);
        assertEquals("approve-b1", taskB1.getTaskDefinitionKey());

        // 调用：B1 的超时定时器到期
        executeTimerJob(processInstanceId);
        // 断言：B1 标记为跳过，在原流程实例中跳转到 B2，流程变量保留，B1 的定时器已经结束
        assertTaskStatus(taskB1.getId(), BpmTaskStatusEnum.SKIP.getStatus(), BpmReasonEnum.TIMEOUT_JUMP.format("审批 B2"));
        Task taskB2 = getRunningTask(processInstanceId);
        assertEquals("approve-b2", taskB2.getTaskDefinitionKey());
        assertEquals(200, runtimeService.getVariable(processInstanceId, "amount"));
        assertEquals(0, managementService.createTimerJobQuery().processInstanceId(processInstanceId).count());
        // 断言：之后按模型继续流转 B2 -> D -> 结束
        approveTask(taskB2);
        assertEquals("approve-d", getRunningTask(processInstanceId).getTaskDefinitionKey());
        approveTask(getRunningTask(processInstanceId));
        assertProcessInstanceStatus(processInstanceId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
    }

    @Test
    public void testTimeoutJump_otherConditionBranch() {
        // 准备参数：金额 200 走 B1 分支，B1 为会签（用户 1、2），超时跳转到另一条分支的 C1
        when(taskCandidateInvoker.calculateUsersByTask(any())).thenAnswer(invocation ->
                "approve-b1".equals(((DelegateExecution) invocation.getArgument(0)).getCurrentActivityId())
                        ? new LinkedHashSet<>(List.of(1L, 2L)) : Set.of(1L));
        BpmSimpleModelNodeVO approveA = buildTimeoutJumpNodes("approve-b1", "approve-c1");
        BpmSimpleModelNodeVO approveB1 = approveA.getChildNode().getConditionNodes().get(0).getChildNode();
        approveB1.setApproveMethod(BpmUserTaskApproveMethodEnum.RATIO.getMethod()).setApproveRatio(100);
        String modelId = modelServiceImpl.createModel(buildModelSaveReqVO(approveA));
        modelServiceImpl.deployModel(1L, modelId);
        String processInstanceId = processInstanceUpdateService.createProcessInstance(1L, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey(MODEL_KEY).setBusinessKey("business-1")
                .setVariables(new HashMap<>(Map.of("amount", 200))));
        approveTask(getRunningTask(processInstanceId)); // 审批 A
        List<Task> tasksB1 = taskQueryService.getRunningTaskListByProcessInstanceId(processInstanceId, null, "approve-b1");
        assertEquals(2, tasksB1.size());

        // 调用：B1 的超时定时器到期
        executeTimerJob(processInstanceId);
        // 断言：B1 的两个会签任务都标记为跳过，跳转到 C1，B2 不会创建
        tasksB1.forEach(task -> assertTaskStatus(task.getId(), BpmTaskStatusEnum.SKIP.getStatus(),
                BpmReasonEnum.TIMEOUT_JUMP.format("审批 C1")));
        Task taskC1 = getRunningTask(processInstanceId);
        assertEquals("approve-c1", taskC1.getTaskDefinitionKey());
        assertEquals(0, historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId)
                .taskDefinitionKey("approve-b2").count());
        // 断言：C1 之后，从条件分支聚合到 D，正常结束
        approveTask(taskC1);
        assertEquals("approve-d", getRunningTask(processInstanceId).getTaskDefinitionKey());
        approveTask(getRunningTask(processInstanceId));
        assertProcessInstanceStatus(processInstanceId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
    }

    @Test
    public void testTimeoutJump_backward() {
        // 准备参数：金额 200，A -> B1 -> B2 审批通过后，D 超时跳转回 A
        String processInstanceId = deployAndStartTimeoutJumpModel("approve-d", "approve-a", 200);
        approveTask(getRunningTask(processInstanceId)); // 审批 A
        approveTask(getRunningTask(processInstanceId)); // 审批 B1
        approveTask(getRunningTask(processInstanceId)); // 审批 B2
        Task taskD = getRunningTask(processInstanceId);
        assertEquals("approve-d", taskD.getTaskDefinitionKey());

        // 调用：D 的超时定时器到期
        executeTimerJob(processInstanceId);
        // 断言：D 标记为跳过，回到 A；目标节点按正常到达处理，不设置退回标记
        assertTaskStatus(taskD.getId(), BpmTaskStatusEnum.SKIP.getStatus(), BpmReasonEnum.TIMEOUT_JUMP.format("审批 A"));
        assertEquals("approve-a", getRunningTask(processInstanceId).getTaskDefinitionKey());
        assertEquals(Boolean.TRUE, runtimeService.getVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_TIMEOUT_JUMPED));
        assertNull(runtimeService.getVariable(processInstanceId,
                String.format(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_RETURN_FLAG, "approve-a")));
        // 断言：审批详情中，A 之后已经审批过的节点会再次执行，仍然预测展示
        BpmApprovalDetailRespVO detail = getApprovalDetail(processInstanceId);
        assertEquals(List.of("approve-a"), getActivityNodeIds(detail, BpmTaskStatusEnum.RUNNING));
        assertEquals(List.of("approve-b1", "approve-b2", "approve-d", "end"),
                getActivityNodeIds(detail, BpmTaskStatusEnum.NOT_START));
        // 断言：A 重新审批后，按条件再次进入 B1
        approveTask(getRunningTask(processInstanceId));
        assertEquals("approve-b1", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testTimeoutJump_withBeforeSignTask() {
        // 准备参数：B1 向前加签给用户 2，加签任务未审批时，B1 超时跳转到 B2
        String processInstanceId = deployAndStartTimeoutJumpModel("approve-b1", "approve-b2", 200);
        approveTask(getRunningTask(processInstanceId)); // 审批 A
        Task taskB1 = getRunningTask(processInstanceId);
        taskUpdateService.createSignTask(1L, new BpmTaskSignCreateReqVO().setId(taskB1.getId())
                .setType(BpmTaskSignTypeEnum.BEFORE.getType()).setUserIds(Set.of(2L)).setReason("加签"));
        Task childTask = taskQueryService.getTaskListByParentTaskId(taskB1.getId()).get(0);

        // 调用：B1 的超时定时器到期
        executeTimerJob(processInstanceId);
        // 断言：父任务、加签任务都标记为跳过并结束，只剩 B2 的任务
        String reason = BpmReasonEnum.TIMEOUT_JUMP.format("审批 B2");
        assertTaskStatus(taskB1.getId(), BpmTaskStatusEnum.SKIP.getStatus(), reason);
        assertTaskStatus(childTask.getId(), BpmTaskStatusEnum.SKIP.getStatus(), reason);
        assertNull(taskQueryService.getTask(childTask.getId()));
        assertEquals("approve-b2", getRunningTask(processInstanceId).getTaskDefinitionKey());

        // 断言：打印数据中，被跳过的向前加签父任务（assignee 为空）使用 owner 展示处理人；处理人不存在时，展示为空
        HistoricProcessInstance processInstance = processInstanceQueryService.getHistoricProcessInstance(processInstanceId);
        List<HistoricTaskInstance> finishedTasks = taskQueryService.getFinishedTaskListByProcessInstanceIdWithoutCancel(processInstanceId);
        HistoricTaskInstance parentTask = finishedTasks.stream().filter(task -> task.getId().equals(taskB1.getId()))
                .findFirst().orElseThrow();
        assertNull(parentTask.getAssignee());
        assertEquals("1", parentTask.getOwner());
        BpmProcessPrintDataRespVO printData = BpmProcessInstanceConvert.INSTANCE.buildProcessInstancePrintData(processInstance,
                processDefinitionServiceImpl.getProcessDefinitionInfo(processInstance.getProcessDefinitionId()), finishedTasks,
                Map.of(1L, new AdminUserRespDTO().setId(1L).setNickname("用户1")), new UserSimpleBaseVO());
        Map<String, String> descriptions = printData.getTasks().stream().collect(Collectors.toMap(
                BpmProcessPrintDataRespVO.Task::getId, BpmProcessPrintDataRespVO.Task::getDescription));
        assertTrue(descriptions.get(taskB1.getId()).startsWith("用户1 / 审批 B1 / "));
        assertTrue(descriptions.get(taskB1.getId()).contains(" / 跳过 / " + reason));
        assertTrue(descriptions.get(childTask.getId()).startsWith(" / "));
    }

    @Test
    public void testTimeoutJump_unassignedTask() {
        // 准备参数：B1 的任务未认领（没有 assignee）
        String processInstanceId = deployAndStartTimeoutJumpModel("approve-b1", "approve-b2", 200);
        approveTask(getRunningTask(processInstanceId)); // 审批 A
        Task taskB1 = getRunningTask(processInstanceId);
        flowableTaskService.unclaim(taskB1.getId());
        assertNull(getRunningTask(processInstanceId).getAssignee());

        // 调用：B1 的超时定时器到期
        executeTimerJob(processInstanceId);
        // 断言：自动跳转按节点处理，不依赖任务的 assignee，跳转到 B2
        assertTaskStatus(taskB1.getId(), BpmTaskStatusEnum.SKIP.getStatus(), BpmReasonEnum.TIMEOUT_JUMP.format("审批 B2"));
        assertEquals("approve-b2", getRunningTask(processInstanceId).getTaskDefinitionKey());
    }

    @Test
    public void testTimeoutJump_approvalDetailOtherConditionBranch() {
        // 准备参数：金额 200 走 B1 分支，B1 超时跳转到另一条分支的 C1
        String processInstanceId = deployAndStartTimeoutJumpModel("approve-b1", "approve-c1", 200);
        approveTask(getRunningTask(processInstanceId)); // 审批 A
        // 断言：跳转前，按流程变量预测 B1 分支
        BpmApprovalDetailRespVO detail = getApprovalDetail(processInstanceId);
        assertEquals(List.of("approve-b1"), getActivityNodeIds(detail, BpmTaskStatusEnum.RUNNING));
        assertEquals(List.of("approve-b2", "approve-d", "end"), getActivityNodeIds(detail, BpmTaskStatusEnum.NOT_START));

        // 调用：B1 的超时定时器到期
        executeTimerJob(processInstanceId);
        // 断言：跳转后，进行中的是 C1，只预测 C1 之后的 D、结束；B1 保留跳过的审批记录，已绕过的 B2 不再展示
        detail = getApprovalDetail(processInstanceId);
        assertEquals(List.of("approve-c1"), getActivityNodeIds(detail, BpmTaskStatusEnum.RUNNING));
        assertEquals(List.of("approve-d", "end"), getActivityNodeIds(detail, BpmTaskStatusEnum.NOT_START));
        assertEquals(List.of("approve-b1"), getActivityNodeIds(detail, BpmTaskStatusEnum.SKIP));
        assertTrue(detail.getActivityNodes().stream().noneMatch(node -> "approve-b2".equals(node.getId())));
        // 断言：C1 审批后，预测 D 之后的节点
        approveTask(getRunningTask(processInstanceId));
        detail = getApprovalDetail(processInstanceId);
        assertEquals(List.of("approve-d"), getActivityNodeIds(detail, BpmTaskStatusEnum.RUNNING));
        assertEquals(List.of("end"), getActivityNodeIds(detail, BpmTaskStatusEnum.NOT_START));
    }

    @Test
    public void testDeployModel_timeoutJumpScope() {
        // 调用，并断言异常：BPMN 设计器的模型，超时执行动作为【自动跳转】，不允许部署
        String bpmnXml = ResourceUtil.readUtf8Str("bpmn/timeout-approve.bpmn20.xml")
                .replace("id=\"timeoutApprove\" name=\"超时审批\"",
                        "id=\"" + MODEL_KEY + "\" name=\"" + MODEL_NAME + "\"");
        String bpmnModelId = modelServiceImpl.createModel(buildBpmnModelSaveReqVO(bpmnXml.replace(
                "<flowable:timeoutHandlerType>2</flowable:timeoutHandlerType>",
                "<flowable:timeoutHandlerType>4</flowable:timeoutHandlerType>")));
        assertServiceException(() -> modelServiceImpl.deployModel(1L, bpmnModelId),
                MODEL_DEPLOY_FAIL_TIMEOUT_JUMP_NOT_SIMPLE, "一级审批");
        assertEquals(0, repositoryService.createProcessDefinitionQuery().processDefinitionKey(MODEL_KEY).count());
        // 调用：BPMN 设计器的模型，其它超时执行动作，正常部署
        modelServiceImpl.updateModelBpmnXml(bpmnModelId, bpmnXml);
        modelServiceImpl.deployModel(1L, bpmnModelId);
        assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionKey(MODEL_KEY).count());
        tearDown();

        // 准备参数：SIMPLE 模型保存后，持久化的 JSON 被修改为无效的跳转（例如说，直接修改数据）
        String simpleModelId = modelServiceImpl.createModel(buildModelSaveReqVO(buildTimeoutJumpNodes("approve-b1", "approve-c1")));
        ReflectionTestUtils.invokeMethod(modelServiceImpl, "updateModelSimpleJson", simpleModelId,
                buildModelSaveReqVO(buildTimeoutJumpNodes("approve-b1", "approve-b1")).getSimpleModel());
        // 调用，并断言异常：部署时，同样校验 SIMPLE 模型
        assertServiceException(() -> modelServiceImpl.deployModel(1L, simpleModelId),
                MODEL_SAVE_FAIL_TIMEOUT_JUMP_NODE_INVALID, "审批 B1");
        assertEquals(0, repositoryService.createProcessDefinitionQuery().processDefinitionKey(MODEL_KEY).count());
    }

    @Test
    public void testDeployModel_timeoutJumpInSubProcess() {
        // 调用，并断言异常：BPMN 设计器的嵌入式子流程中，超时执行动作为【自动跳转】，不允许部署
        String modelId = modelServiceImpl.createModel(buildBpmnModelSaveReqVO(buildSubProcessBpmnXml(
                BpmUserTaskTimeoutHandlerTypeEnum.AUTO_JUMP)));
        assertServiceException(() -> modelServiceImpl.deployModel(1L, modelId),
                MODEL_DEPLOY_FAIL_TIMEOUT_JUMP_NOT_SIMPLE, "子流程审批");
        assertEquals(0, repositoryService.createProcessDefinitionQuery().processDefinitionKey(MODEL_KEY).count());

        // 调用：嵌入式子流程中，其它超时执行动作，正常部署
        modelServiceImpl.updateModelBpmnXml(modelId, buildSubProcessBpmnXml(BpmUserTaskTimeoutHandlerTypeEnum.APPROVE));
        modelServiceImpl.deployModel(1L, modelId);
        assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionKey(MODEL_KEY).count());
    }

    @Test
    public void testDeployModel_timeoutJumpNotMatch() {
        // 准备参数：SIMPLE 模型 B1 超时跳转到 C1，流程图与 SIMPLE 模型一致
        String modelId = modelServiceImpl.createModel(buildModelSaveReqVO(buildTimeoutJumpNodes("approve-b1", "approve-c1")));
        String bpmnXml = new String(modelServiceImpl.getModelBpmnXML(modelId), StandardCharsets.UTF_8);
        BoundaryEvent timeoutEvent = getTimeoutBoundaryEvent(BpmnModelUtils.getBpmnModel(bpmnXml.getBytes(StandardCharsets.UTF_8)), "approve-b1");
        assertEquals("approve-c1", BpmnModelUtils.parseBoundaryEventExtensionElement(timeoutEvent, USER_TASK_TIMEOUT_JUMP_NODE_ID));
        assertEquals(String.valueOf(BpmUserTaskTimeoutHandlerTypeEnum.AUTO_JUMP.getType()),
                BpmnModelUtils.parseBoundaryEventExtensionElement(timeoutEvent, USER_TASK_TIMEOUT_HANDLER_TYPE));

        // 调用，并断言异常：流程图被单独修改（例如说，/update-bpmn 接口），替换为其它可以跳转的节点、不存在的节点
        modelServiceImpl.updateModelBpmnXml(modelId, updateTimeoutBoundaryEvent(bpmnXml, "approve-b1", USER_TASK_TIMEOUT_JUMP_NODE_ID, "approve-b2"));
        assertServiceException(() -> modelServiceImpl.deployModel(1L, modelId), MODEL_DEPLOY_FAIL_TIMEOUT_JUMP_NOT_MATCH, "审批 B1");
        modelServiceImpl.updateModelBpmnXml(modelId, updateTimeoutBoundaryEvent(bpmnXml, "approve-b1", USER_TASK_TIMEOUT_JUMP_NODE_ID, "not-exists"));
        assertServiceException(() -> modelServiceImpl.deployModel(1L, modelId), MODEL_DEPLOY_FAIL_TIMEOUT_JUMP_NOT_MATCH, "审批 B1");
        // 调用，并断言异常：流程图中缺少自动跳转（执行动作改为自动同意）
        modelServiceImpl.updateModelBpmnXml(modelId, updateTimeoutBoundaryEvent(bpmnXml, "approve-b1", USER_TASK_TIMEOUT_HANDLER_TYPE,
                String.valueOf(BpmUserTaskTimeoutHandlerTypeEnum.APPROVE.getType())));
        assertServiceException(() -> modelServiceImpl.deployModel(1L, modelId), MODEL_DEPLOY_FAIL_TIMEOUT_JUMP_NOT_MATCH, "审批 B1");
        // 调用，并断言异常：流程图中多出自动跳转（SIMPLE 模型中，B1 未开启超时）
        modelServiceImpl.updateModelBpmnXml(modelId, bpmnXml);
        BpmSimpleModelNodeVO noTimeoutSimpleModel = buildModelSaveReqVO(buildTimeoutJumpNodes("approve-b1", "approve-c1")).getSimpleModel();
        noTimeoutSimpleModel.getChildNode().getChildNode().getConditionNodes().get(0).getChildNode().setTimeoutHandler(null);
        ReflectionTestUtils.invokeMethod(modelServiceImpl, "updateModelSimpleJson", modelId, noTimeoutSimpleModel);
        assertServiceException(() -> modelServiceImpl.deployModel(1L, modelId), MODEL_DEPLOY_FAIL_TIMEOUT_JUMP_NOT_MATCH, "审批 B1");
        assertEquals(0, repositoryService.createProcessDefinitionQuery().processDefinitionKey(MODEL_KEY).count());

        // 调用：恢复一致后，正常部署
        ReflectionTestUtils.invokeMethod(modelServiceImpl, "updateModelSimpleJson", modelId,
                buildModelSaveReqVO(buildTimeoutJumpNodes("approve-b1", "approve-c1")).getSimpleModel());
        modelServiceImpl.deployModel(1L, modelId);
        assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionKey(MODEL_KEY).count());
    }

    @Test
    public void testDeployModel_timeoutJumpTargetInvalid() {
        // 准备参数：SIMPLE 模型 B1 超时跳转到 C1，流程图中跳转的配置与 SIMPLE 模型一致
        String modelId = modelServiceImpl.createModel(buildModelSaveReqVO(buildTimeoutJumpNodes("approve-b1", "approve-c1")));
        String bpmnXml = new String(modelServiceImpl.getModelBpmnXML(modelId), StandardCharsets.UTF_8);

        // 调用，并断言异常：流程图中删除了目标节点 C1
        modelServiceImpl.updateModelBpmnXml(modelId, updateBpmnXml(bpmnXml,
                bpmnModel -> bpmnModel.getMainProcess().removeFlowElement("approve-c1")));
        assertServiceException(() -> modelServiceImpl.deployModel(1L, modelId), MODEL_DEPLOY_FAIL_TIMEOUT_JUMP_NOT_MATCH, "审批 B1");
        // 调用，并断言异常：流程图中目标节点 C1 改为自动通过，不支持跳转
        modelServiceImpl.updateModelBpmnXml(modelId, updateBpmnXml(bpmnXml, bpmnModel -> CollUtil.getFirst(
                BpmnModelUtils.getFlowElementById(bpmnModel, "approve-c1").getExtensionElements().get(USER_TASK_APPROVE_TYPE))
                .setElementText(String.valueOf(BpmUserTaskApproveTypeEnum.AUTO_APPROVE.getType()))));
        assertServiceException(() -> modelServiceImpl.deployModel(1L, modelId), MODEL_DEPLOY_FAIL_TIMEOUT_JUMP_NOT_MATCH, "审批 B1");
        // 调用，并断言异常：流程图中目标节点 C1 改为办理节点（同样是人工审批的用户任务），不支持跳转
        modelServiceImpl.updateModelBpmnXml(modelId, updateBpmnXml(bpmnXml, bpmnModel -> CollUtil.getFirst(
                BpmnModelUtils.getFlowElementById(bpmnModel, "approve-c1").getExtensionElements().get(NODE_TYPE))
                .setElementText(String.valueOf(BpmSimpleModelNodeTypeEnum.TRANSACTOR_NODE.getType()))));
        assertServiceException(() -> modelServiceImpl.deployModel(1L, modelId), MODEL_DEPLOY_FAIL_TIMEOUT_JUMP_NOT_MATCH, "审批 B1");
        assertEquals(0, repositoryService.createProcessDefinitionQuery().processDefinitionKey(MODEL_KEY).count());

        // 调用：恢复后，正常部署
        modelServiceImpl.updateModelBpmnXml(modelId, bpmnXml);
        modelServiceImpl.deployModel(1L, modelId);
        assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionKey(MODEL_KEY).count());
    }

    @Test
    public void testTimeoutJump_withReminder() {
        // 准备参数：SIMPLE 模型 B1 超时跳转到 B2；流程图中 B1 另有一个【自动提醒】的超时边界事件，并且排在【自动跳转】之前
        String modelId = modelServiceImpl.createModel(buildModelSaveReqVO(buildTimeoutJumpNodes("approve-b1", "approve-b2")));
        String bpmnXml = new String(modelServiceImpl.getModelBpmnXML(modelId), StandardCharsets.UTF_8);
        String jumpEventId = getTimeoutBoundaryEvent(BpmnModelUtils.getBpmnModel(
                bpmnXml.getBytes(StandardCharsets.UTF_8)), "approve-b1").getId();
        modelServiceImpl.updateModelBpmnXml(modelId, updateBpmnXml(bpmnXml, bpmnModel -> {
            BoundaryEvent reminderEvent = new BoundaryEvent();
            reminderEvent.setId("reminder-b1");
            reminderEvent.setCancelActivity(false);
            reminderEvent.setAttachedToRef((UserTask) BpmnModelUtils.getFlowElementById(bpmnModel, "approve-b1"));
            reminderEvent.setAttachedToRefId("approve-b1");
            TimerEventDefinition timerEventDefinition = new TimerEventDefinition();
            timerEventDefinition.setTimeDuration("PT30M");
            reminderEvent.addEventDefinition(timerEventDefinition);
            BpmnModelUtils.addExtensionElement(reminderEvent, BOUNDARY_EVENT_TYPE, BpmBoundaryEventTypeEnum.USER_TASK_TIMEOUT.getType());
            BpmnModelUtils.addExtensionElement(reminderEvent, USER_TASK_TIMEOUT_HANDLER_TYPE, BpmUserTaskTimeoutHandlerTypeEnum.REMINDER.getType());
            // 先移除【自动跳转】，再依次添加，使【自动提醒】在流程图中排在前面
            BoundaryEvent jumpEvent = (BoundaryEvent) BpmnModelUtils.getFlowElementById(bpmnModel, jumpEventId);
            bpmnModel.getMainProcess().removeFlowElement(jumpEventId);
            bpmnModel.getMainProcess().addFlowElement(reminderEvent);
            bpmnModel.getMainProcess().addFlowElement(jumpEvent);
        }));
        modelServiceImpl.deployModel(1L, modelId);
        UserTask userTaskB1 = (UserTask) BpmnModelUtils.getFlowElementById(
                repositoryService.getBpmnModel(getActiveProcessDefinition().getId()), "approve-b1");
        assertEquals(List.of("reminder-b1", jumpEventId), userTaskB1.getBoundaryEvents().stream().map(BoundaryEvent::getId).toList());
        // 准备参数：金额 200，审批 A 后到达 B1
        String processInstanceId = processInstanceUpdateService.createProcessInstance(1L, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey(MODEL_KEY).setBusinessKey("business-1")
                .setVariables(new HashMap<>(Map.of("amount", 200))));
        approveTask(getRunningTask(processInstanceId)); // 审批 A
        Task taskB1 = getRunningTask(processInstanceId);
        assertEquals("approve-b1", taskB1.getTaskDefinitionKey());
        assertEquals(2, managementService.createTimerJobQuery().processInstanceId(processInstanceId).count());

        // 调用：【自动提醒】的定时器到期
        executeTimerJob(processInstanceId, "reminder-b1");
        // 断言：发送提醒，任务仍在 B1
        verify(messageService).sendMessageWhenTaskTimeout(any());
        assertEquals("approve-b1", getRunningTask(processInstanceId).getTaskDefinitionKey());

        // 调用：【自动跳转】的定时器到期
        executeTimerJob(processInstanceId, jumpEventId);
        // 断言：按【自动跳转】的边界事件，跳转到 B2
        assertTaskStatus(taskB1.getId(), BpmTaskStatusEnum.SKIP.getStatus(), BpmReasonEnum.TIMEOUT_JUMP.format("审批 B2"));
        assertEquals("approve-b2", getRunningTask(processInstanceId).getTaskDefinitionKey());
        assertEquals(0, managementService.createTimerJobQuery().processInstanceId(processInstanceId).count());
    }

    @Test
    public void testReturnTask_approvalDetail() {
        // 准备参数：金额 200，A -> B1 -> B2 审批通过后，D 手工退回到 A（C1 的超时跳转不在执行路径上）
        String processInstanceId = deployAndStartTimeoutJumpModel("approve-c1", "approve-a", 200);
        approveTask(getRunningTask(processInstanceId)); // 审批 A
        approveTask(getRunningTask(processInstanceId)); // 审批 B1
        approveTask(getRunningTask(processInstanceId)); // 审批 B2
        Task taskD = getRunningTask(processInstanceId);
        assertEquals("approve-d", taskD.getTaskDefinitionKey());

        // 调用：D 手工退回到 A
        taskUpdateService.returnTask(1L, new BpmTaskReturnReqVO().setId(taskD.getId())
                .setTargetTaskDefinitionKey("approve-a").setReason("退回"));
        // 断言：实际移动到 A，记录了需要预测的节点
        assertEquals("approve-a", getRunningTask(processInstanceId).getTaskDefinitionKey());
        assertNotNull(runtimeService.getVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_NEED_SIMULATE_TASK_IDS));
        assertNull(runtimeService.getVariable(processInstanceId,
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_TIMEOUT_JUMPED));
        // 断言：审批详情从进行中的 A 开始预测，A 之后已经审批过的节点会再次执行，仍然展示
        BpmApprovalDetailRespVO detail = getApprovalDetail(processInstanceId);
        assertEquals(List.of("approve-a"), getActivityNodeIds(detail, BpmTaskStatusEnum.RUNNING));
        assertEquals(List.of("approve-b1", "approve-b2", "approve-d", "end"),
                getActivityNodeIds(detail, BpmTaskStatusEnum.NOT_START));
        assertTrue(detail.getActivityNodes().stream().noneMatch(node -> "approve-c1".equals(node.getId())));
        // 断言：A 重新审批后，从进行中的 B1 开始预测
        approveTask(getRunningTask(processInstanceId));
        detail = getApprovalDetail(processInstanceId);
        assertEquals(List.of("approve-b1"), getActivityNodeIds(detail, BpmTaskStatusEnum.RUNNING));
        assertEquals(List.of("approve-b2", "approve-d", "end"), getActivityNodeIds(detail, BpmTaskStatusEnum.NOT_START));
    }

    @Test
    public void testSaveSimpleModel_buttonsSetting() {
        // 准备参数：发起人（根节点）、审批 A（子节点）、审批 B1（条件分支内的节点）都配置合法的按钮类型编号
        BpmModelSaveReqVO createReqVO = buildModelSaveReqVO(buildTimeoutJumpNodes("approve-b1", "approve-c1"));
        createReqVO.getSimpleModel().setButtonsSetting(buildButtonsSetting(1, 2, 3, 4, 5, 6));
        getSimpleNode(createReqVO.getSimpleModel(), "approve-a").setButtonsSetting(buildButtonsSetting(1, 2, 3, 4, 5, 6, 7));
        getSimpleNode(createReqVO.getSimpleModel(), "approve-b1").setButtonsSetting(buildButtonsSetting(7, 1));

        // 调用：保存成功，按钮类型编号按原样保存
        String modelId = modelServiceImpl.createModel(createReqVO);
        BpmSimpleModelNodeVO simpleModel = modelServiceImpl.getSimpleModel(modelId);
        assertEquals(List.of(1, 2, 3, 4, 5, 6), getButtonIds(simpleModel));
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7), getButtonIds(getSimpleNode(simpleModel, "approve-a")));
        assertEquals(List.of(7, 1), getButtonIds(getSimpleNode(simpleModel, "approve-b1")));

        // 调用，并断言异常：新建时，条件分支内的节点使用无效的按钮类型编号
        BpmModelSaveReqVO invalidCreateReqVO = buildModelSaveReqVO(buildTimeoutJumpNodes("approve-b1", "approve-c1"));
        invalidCreateReqVO.setKey(MODEL_KEY + "Invalid");
        getSimpleNode(invalidCreateReqVO.getSimpleModel(), "approve-b1").setButtonsSetting(buildButtonsSetting(1, 8));
        assertThrows(ConstraintViolationException.class, () -> modelServiceImpl.createModel(invalidCreateReqVO));
        assertEquals(0, repositoryService.createModelQuery().modelKey(MODEL_KEY + "Invalid").count());
        // 调用，并断言异常：更新 SIMPLE 模型时，子节点使用无效的按钮类型编号，模型保持不变
        BpmSimpleModelNodeVO invalidSimpleModel = buildModelSaveReqVO(buildTimeoutJumpNodes("approve-b1", "approve-c1")).getSimpleModel();
        getSimpleNode(invalidSimpleModel, "approve-a").setButtonsSetting(buildButtonsSetting(0));
        assertThrows(ConstraintViolationException.class, () -> modelServiceImpl.updateSimpleModel(1L,
                new BpmSimpleModelUpdateReqVO().setId(modelId).setSimpleModel(invalidSimpleModel)));
        assertEquals(List.of(7, 1), getButtonIds(getSimpleNode(modelServiceImpl.getSimpleModel(modelId), "approve-b1")));
    }

    @Test
    public void testValidateSimpleModelReqVO_buttonsSetting() {
        Validator validator = validatorFactory.getValidator();
        BpmSimpleModelNodeVO simpleModel = buildModelSaveReqVO(buildTimeoutJumpNodes("approve-b1", "approve-c1")).getSimpleModel();
        BpmSimpleModelUpdateReqVO reqVO = new BpmSimpleModelUpdateReqVO().setId("model").setSimpleModel(simpleModel);

        // 调用 + 断言：请求参数级联校验到根节点的按钮，合法
        simpleModel.setButtonsSetting(buildButtonsSetting(1, 2, 3, 4, 5, 6, 7));
        assertTrue(validator.validate(reqVO).isEmpty());
        // 调用 + 断言：根节点的按钮类型编号无效
        simpleModel.setButtonsSetting(buildButtonsSetting(1, 8));
        assertEquals(Set.of("simpleModel.buttonsSetting[1].id"), validator.validate(reqVO).stream()
                .map(violation -> violation.getPropertyPath().toString()).collect(Collectors.toSet()));
        // 调用 + 断言：子节点未级联校验（避免一并校验节点的其它约束），由保存时遍历所有节点的按钮校验，见 testSaveSimpleModel_buttonsSetting
        simpleModel.setButtonsSetting(null);
        getSimpleNode(simpleModel, "approve-b1").setButtonsSetting(buildButtonsSetting(8));
        assertTrue(validator.validate(reqVO).isEmpty());
    }

    @Test
    public void testTimeoutJump_saveInvalid() {
        // 调用，并断言异常：跳转到自己
        assertServiceException(() -> modelServiceImpl.createModel(buildModelSaveReqVO(
                buildTimeoutJumpNodes("approve-b1", "approve-b1"))), MODEL_SAVE_FAIL_TIMEOUT_JUMP_NODE_INVALID, "审批 B1");
        // 调用，并断言异常：跳转到发起人节点
        assertServiceException(() -> modelServiceImpl.createModel(buildModelSaveReqVO(
                buildTimeoutJumpNodes("approve-b1", "start-user"))), MODEL_SAVE_FAIL_TIMEOUT_JUMP_NODE_INVALID, "审批 B1");
        // 调用，并断言异常：跳转到审批人为【发起人自选】的节点；当前节点为自选时，可以保存
        BpmSimpleModelNodeVO selectApproveA = buildTimeoutJumpNodes("approve-b1", "approve-c1");
        selectApproveA.getChildNode().getConditionNodes().get(1).getChildNode()
                .setCandidateStrategy(BpmTaskCandidateStrategyEnum.START_USER_SELECT.getStrategy());
        assertServiceException(() -> modelServiceImpl.createModel(buildModelSaveReqVO(selectApproveA)),
                MODEL_SAVE_FAIL_TIMEOUT_JUMP_NODE_INVALID, "审批 B1");
        BpmSimpleModelNodeVO selectSourceA = buildTimeoutJumpNodes("approve-c1", "approve-b1");
        selectSourceA.getChildNode().getConditionNodes().get(1).getChildNode()
                .setCandidateStrategy(BpmTaskCandidateStrategyEnum.START_USER_SELECT.getStrategy());
        repositoryService.deleteModel(modelServiceImpl.createModel(buildModelSaveReqVO(selectSourceA)));
        // 调用，并断言异常：跳转到并行分支内的节点
        BpmSimpleModelNodeVO approveA = buildTimeoutJumpNodes("approve-a", "parallel-approve");
        BpmSimpleModelNodeVO approveD = approveA.getChildNode().getChildNode();
        BpmSimpleModelNodeVO parallel = new BpmSimpleModelNodeVO().setId("parallel").setName("并行分支")
                .setType(BpmSimpleModelNodeTypeEnum.PARALLEL_BRANCH_NODE.getType())
                .setConditionNodes(List.of(
                        new BpmSimpleModelNodeVO().setId("parallel-1").setType(BpmSimpleModelNodeTypeEnum.CONDITION_NODE.getType())
                                .setChildNode(approveNode("parallel-approve", "并行审批")),
                        new BpmSimpleModelNodeVO().setId("parallel-2").setType(BpmSimpleModelNodeTypeEnum.CONDITION_NODE.getType())))
                .setChildNode(approveD.getChildNode());
        approveD.setChildNode(parallel);
        assertServiceException(() -> modelServiceImpl.createModel(buildModelSaveReqVO(approveA)),
                MODEL_SAVE_FAIL_TIMEOUT_JUMP_NODE_INVALID, "审批 A");
        // 断言：校验失败时，不保存模型
        assertEquals(0, repositoryService.createModelQuery().modelKey(MODEL_KEY).count());

        // 调用，并断言异常：修改 Simple 模型时，同样校验
        String modelId = createModel();
        BpmSimpleModelUpdateReqVO updateReqVO = new BpmSimpleModelUpdateReqVO();
        updateReqVO.setId(modelId);
        updateReqVO.setSimpleModel(buildModelSaveReqVO(buildTimeoutJumpNodes("approve-d", "approve-d")).getSimpleModel());
        assertServiceException(() -> modelServiceImpl.updateSimpleModel(1L, updateReqVO),
                MODEL_SAVE_FAIL_TIMEOUT_JUMP_NODE_INVALID, "审批 D");
    }

    // ========== 私有方法 ==========

    /**
     * 创建 Simple 模型：发起人 -> 审批（用户 1）-> 结束，管理员为用户 1
     */
    private String createModel() {
        return modelServiceImpl.createModel(buildModelSaveReqVO(approveNode("approve", "审批")
                .setChildNode(endNode())));
    }

    /**
     * 构建 Simple 模型的保存请求：发起人 -> firstNode -> ...，管理员为用户 1
     */
    private static BpmModelSaveReqVO buildModelSaveReqVO(BpmSimpleModelNodeVO firstNode) {
        BpmSimpleModelNodeVO startUserNode = new BpmSimpleModelNodeVO().setId("start-user").setName("发起人")
                .setType(BpmSimpleModelNodeTypeEnum.START_USER_NODE.getType())
                .setChildNode(firstNode);
        BpmModelSaveReqVO reqVO = new BpmModelSaveReqVO();
        reqVO.setKey(MODEL_KEY);
        reqVO.setName(MODEL_NAME);
        reqVO.setCategory("OA");
        reqVO.setSimpleModel(startUserNode);
        reqVO.setType(BpmModelTypeEnum.SIMPLE.getType());
        reqVO.setFormType(BpmModelFormTypeEnum.CUSTOM.getType());
        reqVO.setFormCustomCreatePath("/bpm/oa/leave/create");
        reqVO.setFormCustomViewPath("/bpm/oa/leave/detail");
        reqVO.setVisible(true);
        reqVO.setManagerUserIds(List.of(1L));
        return reqVO;
    }

    /**
     * 构建带条件分支的 Simple 模型：发起人 -> 审批 A -> 条件分支（金额大于 100：审批 B1 -> 审批 B2；默认：审批 C1）-> 审批 D -> 结束
     *
     * @param timeoutNodeId 开启超时（1 小时）自动跳转的节点
     * @param jumpNodeId    跳转的目标节点
     * @return 第一个节点，即审批 A
     */
    private static BpmSimpleModelNodeVO buildTimeoutJumpNodes(String timeoutNodeId, String jumpNodeId) {
        BpmSimpleModelNodeVO approveD = approveNode("approve-d", "审批 D").setChildNode(endNode());
        BpmSimpleModelNodeVO approveB1 = approveNode("approve-b1", "审批 B1")
                .setChildNode(approveNode("approve-b2", "审批 B2"));
        BpmSimpleModelNodeVO branch = new BpmSimpleModelNodeVO().setId("branch").setName("条件分支")
                .setType(BpmSimpleModelNodeTypeEnum.CONDITION_BRANCH_NODE.getType())
                .setConditionNodes(List.of(conditionNode("condition-amount", "${amount > 100}", approveB1),
                        conditionNode("condition-default", null, approveNode("approve-c1", "审批 C1"))))
                .setChildNode(approveD);
        BpmSimpleModelNodeVO approveA = approveNode("approve-a", "审批 A").setChildNode(branch);
        // 设置超时自动跳转
        BpmSimpleModelNodeVO timeoutNode = Stream.of(approveA, approveB1, approveB1.getChildNode(),
                        branch.getConditionNodes().get(1).getChildNode(), approveD)
                .filter(node -> node.getId().equals(timeoutNodeId)).findFirst().orElseThrow();
        timeoutNode.setTimeoutHandler(new BpmSimpleModelNodeVO.TimeoutHandler().setEnable(true)
                .setType(BpmUserTaskTimeoutHandlerTypeEnum.AUTO_JUMP.getType()).setTimeDuration("PT1H")
                .setJumpNodeId(jumpNodeId));
        return approveA;
    }

    /**
     * 构建 BPMN 模型的保存请求，管理员为用户 1
     */
    private static BpmModelSaveReqVO buildBpmnModelSaveReqVO(String bpmnXml) {
        BpmModelSaveReqVO reqVO = buildModelSaveReqVO(null);
        reqVO.setType(BpmModelTypeEnum.BPMN.getType());
        reqVO.setSimpleModel(null);
        reqVO.setBpmnXml(bpmnXml);
        return reqVO;
    }

    /**
     * 修改流程图中，用户任务超时边界事件的扩展元素：解析实际存储的流程图，修改后重新序列化，不依赖 XML 文本格式
     *
     * @param bpmnXml     流程图
     * @param userTaskId  用户任务编号
     * @param elementName 扩展元素名
     * @param elementText 扩展元素的新值
     * @return 修改后的流程图
     */
    private static String updateTimeoutBoundaryEvent(String bpmnXml, String userTaskId, String elementName, String elementText) {
        return updateBpmnXml(bpmnXml, bpmnModel -> {
            ExtensionElement element = CollUtil.getFirst(getTimeoutBoundaryEvent(bpmnModel, userTaskId)
                    .getExtensionElements().get(elementName));
            assertNotNull(element);
            element.setElementText(elementText);
        });
    }

    private static List<BpmSimpleModelNodeVO.OperationButtonSetting> buildButtonsSetting(Integer... ids) {
        return Stream.of(ids).map(id -> new BpmSimpleModelNodeVO.OperationButtonSetting()
                .setId(id).setDisplayName("按钮" + id).setEnable(true)).collect(Collectors.toList());
    }

    private static List<Integer> getButtonIds(BpmSimpleModelNodeVO simpleNode) {
        return simpleNode.getButtonsSetting().stream().map(BpmSimpleModelNodeVO.OperationButtonSetting::getId).toList();
    }

    private static BpmSimpleModelNodeVO getSimpleNode(BpmSimpleModelNodeVO rootNode, String nodeId) {
        List<BpmSimpleModelNodeVO> nodes = new ArrayList<>();
        SimpleModelUtils.traverseNode(rootNode, node -> {
            if (nodeId.equals(node.getId())) {
                nodes.add(node);
            }
        });
        assertEquals(1, nodes.size());
        return nodes.get(0);
    }

    /**
     * 修改流程图：解析实际存储的流程图，修改后重新序列化
     *
     * @param bpmnXml  流程图
     * @param consumer 修改逻辑
     * @return 修改后的流程图
     */
    private static String updateBpmnXml(String bpmnXml, Consumer<BpmnModel> consumer) {
        BpmnModel bpmnModel = BpmnModelUtils.getBpmnModel(bpmnXml.getBytes(StandardCharsets.UTF_8));
        consumer.accept(bpmnModel);
        return BpmnModelUtils.getBpmnXml(bpmnModel);
    }

    private static BoundaryEvent getTimeoutBoundaryEvent(BpmnModel bpmnModel, String userTaskId) {
        UserTask userTask = (UserTask) BpmnModelUtils.getFlowElementById(bpmnModel, userTaskId);
        assertNotNull(userTask);
        BoundaryEvent boundaryEvent = CollUtil.findOne(userTask.getBoundaryEvents(), event -> ObjUtil.equal(
                String.valueOf(BpmBoundaryEventTypeEnum.USER_TASK_TIMEOUT.getType()),
                BpmnModelUtils.parseBoundaryEventExtensionElement(event, BOUNDARY_EVENT_TYPE)));
        assertNotNull(boundaryEvent);
        return boundaryEvent;
    }

    /**
     * 构建 BPMN 流程图：开始 -> 嵌入式子流程（子流程审批，挂载超时边界事件）-> 结束
     *
     * @param timeoutHandlerType 超时执行动作
     */
    private static String buildSubProcessBpmnXml(BpmUserTaskTimeoutHandlerTypeEnum timeoutHandlerType) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             xmlns:flowable="http://flowable.org/bpmn"
                             targetNamespace="http://www.yudao.iocoder.cn/bpm">
                    <process id="%s" name="%s" isExecutable="true">
                        <startEvent id="start"/>
                        <sequenceFlow id="flow-start" sourceRef="start" targetRef="sub"/>
                        <subProcess id="sub" name="子流程">
                            <startEvent id="sub-start"/>
                            <sequenceFlow id="flow-sub-start" sourceRef="sub-start" targetRef="sub-task"/>
                            <userTask id="sub-task" name="子流程审批">
                                <extensionElements>
                                    <flowable:candidateStrategy>30</flowable:candidateStrategy>
                                </extensionElements>
                            </userTask>
                            <boundaryEvent id="sub-task-timeout" attachedToRef="sub-task" cancelActivity="false">
                                <extensionElements>
                                    <flowable:boundaryEventType>1</flowable:boundaryEventType>
                                    <flowable:timeoutHandlerType>%d</flowable:timeoutHandlerType>
                                </extensionElements>
                                <timerEventDefinition>
                                    <timeDuration>PT1H</timeDuration>
                                </timerEventDefinition>
                            </boundaryEvent>
                            <sequenceFlow id="flow-sub-task" sourceRef="sub-task" targetRef="sub-end"/>
                            <endEvent id="sub-end"/>
                        </subProcess>
                        <sequenceFlow id="flow-sub" sourceRef="sub" targetRef="end"/>
                        <endEvent id="end"/>
                    </process>
                </definitions>
                """.formatted(MODEL_KEY, MODEL_NAME, timeoutHandlerType.getType());
    }

    private BpmApprovalDetailRespVO getApprovalDetail(String processInstanceId) {
        return processInstanceQueryService.getApprovalDetail(1L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId));
    }

    /**
     * 获得审批详情中，指定状态的节点编号们
     */
    private static List<String> getActivityNodeIds(BpmApprovalDetailRespVO detail, BpmTaskStatusEnum status) {
        return detail.getActivityNodes().stream().filter(node -> status.getStatus().equals(node.getStatus()))
                .map(BpmApprovalDetailRespVO.ActivityNode::getId).toList();
    }

    private static BpmSimpleModelNodeVO approveNode(String id, String name) {
        return new BpmSimpleModelNodeVO().setId(id).setName(name)
                .setType(BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType())
                .setApproveType(BpmUserTaskApproveTypeEnum.USER.getType())
                .setApproveMethod(BpmUserTaskApproveMethodEnum.RANDOM.getMethod())
                .setCandidateStrategy(BpmTaskCandidateStrategyEnum.USER.getStrategy()).setCandidateParam("1");
    }

    private static BpmSimpleModelNodeVO conditionNode(String id, String expression, BpmSimpleModelNodeVO childNode) {
        return new BpmSimpleModelNodeVO().setId(id).setName(id)
                .setType(BpmSimpleModelNodeTypeEnum.CONDITION_NODE.getType())
                .setConditionSetting(new BpmSimpleModelNodeVO.ConditionSetting()
                        .setConditionType(BpmSimpleModeConditionTypeEnum.EXPRESSION.getType())
                        .setConditionExpression(expression).setDefaultFlow(expression == null))
                .setChildNode(childNode);
    }

    private static BpmSimpleModelNodeVO endNode() {
        return new BpmSimpleModelNodeVO().setId("end").setType(BpmSimpleModelNodeTypeEnum.END_NODE.getType());
    }

    /**
     * 创建并部署 {@link #buildTimeoutJumpNodes(String, String)} 的模型，然后发起金额为 amount 的流程
     *
     * @return 流程实例编号
     */
    private String deployAndStartTimeoutJumpModel(String timeoutNodeId, String jumpNodeId, int amount) {
        String modelId = modelServiceImpl.createModel(buildModelSaveReqVO(buildTimeoutJumpNodes(timeoutNodeId, jumpNodeId)));
        modelServiceImpl.deployModel(1L, modelId);
        return processInstanceUpdateService.createProcessInstance(1L, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey(MODEL_KEY).setBusinessKey("business-1")
                .setVariables(new HashMap<>(Map.of("amount", amount))));
    }

    /**
     * 将流程实例上唯一的定时器转为可执行任务并立即执行，模拟定时器到期，由 BpmTaskEventListener#timerFired 处理
     */
    private void executeTimerJob(String processInstanceId) {
        Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstanceId).singleResult();
        assertNotNull(timerJob);
        managementService.executeJob(managementService.moveTimerToExecutableJob(timerJob.getId()).getId());
    }

    /**
     * 将流程实例上，指定边界事件的定时器转为可执行任务并立即执行，用于同一节点有多个定时器的场景
     */
    private void executeTimerJob(String processInstanceId, String elementId) {
        Job timerJob = managementService.createTimerJobQuery().processInstanceId(processInstanceId)
                .elementId(elementId).singleResult();
        assertNotNull(timerJob);
        managementService.executeJob(managementService.moveTimerToExecutableJob(timerJob.getId()).getId());
    }

    private void assertTaskStatus(String taskId, Integer status, String reason) {
        HistoricTaskInstance task = taskQueryService.getHistoricTask(taskId);
        assertEquals(status, FlowableUtils.getTaskStatus(task));
        assertEquals(reason, task.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_REASON));
    }

    private ProcessDefinition getActiveProcessDefinition() {
        ProcessDefinition definition = processDefinitionServiceImpl.getActiveProcessDefinition(MODEL_KEY);
        assertNotNull(definition);
        return definition;
    }

    private String startProcessInstance() {
        return processInstanceUpdateService.createProcessInstance(1L, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey(MODEL_KEY).setBusinessKey("business-1"));
    }

    /**
     * 发起流程失败：ServiceException 被 FlowableUtils#executeAuthenticatedUserId 包装，由全局异常处理器解包
     */
    private void assertStartProcessInstanceFail(ErrorCode errorCode) {
        RuntimeException exception = assertThrows(RuntimeException.class, this::startProcessInstance);
        ServiceException serviceException = assertInstanceOf(ServiceException.class, exception.getCause());
        assertEquals(errorCode.getCode(), serviceException.getCode());
    }

    private Task getRunningTask(String processInstanceId) {
        List<Task> tasks = taskQueryService.getRunningTaskListByProcessInstanceId(processInstanceId, null, null);
        assertEquals(1, tasks.size());
        return tasks.get(0);
    }

    private void approveTask(Task task) {
        taskUpdateService.approveTask(Long.valueOf(task.getAssignee()), new BpmTaskApproveReqVO().setId(task.getId())
                .setReason("同意"));
    }

    private void assertProcessInstanceStatus(String processInstanceId, Integer status) {
        assertEquals(status, FlowableUtils.getProcessInstanceStatus(
                processInstanceQueryService.getHistoricProcessInstance(processInstanceId)));
    }

}
