package cn.iocoder.yudao.module.bpm.service.task;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCancelReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCreateReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.dal.redis.BpmProcessIdRedisDAO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelFormTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmReasonEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.event.BpmProcessInstanceEventPublisher;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmHttpRequestUtils;
import cn.iocoder.yudao.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.yudao.module.bpm.service.message.BpmMessageService;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceApproveReqDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ExecutionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link BpmProcessInstanceServiceImpl} 的单元测试
 *
 * @author HUIHUI
 */
public class BpmProcessInstanceServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmProcessInstanceServiceImpl processInstanceService;

    @Mock
    private RuntimeService runtimeService;
    @Mock
    private HistoryService historyService;
    @Mock
    private BpmProcessDefinitionService processDefinitionService;
    @Mock
    private BpmTaskService taskService;
    @Mock
    private BpmMessageService messageService;
    @Mock
    private AdminUserApi adminUserApi;
    @Mock
    private BpmProcessInstanceEventPublisher processInstanceEventPublisher;
    @Mock
    private BpmProcessIdRedisDAO processIdRedisDAO;
    @Mock
    private ProcessInstance processInstance;
    @Mock
    private ProcessDefinition processDefinition;

    // ========== createProcessInstance 相关 ==========

    @Test
    public void testCreateProcessInstance_success() {
        // 准备参数：动态表单必填字段已填写，开启流程编号规则、标题设置，并配置发起人自选审批人
        BpmProcessInstanceServiceImpl spyService = spy(processInstanceService);
        BpmModelMetaInfoVO.ProcessIdRule processIdRule = new BpmModelMetaInfoVO.ProcessIdRule().setEnable(true)
                .setPrefix("QJ");
        mockCreateProcessInstance(buildProcessDefinitionInfo()
                .setFormType(BpmModelFormTypeEnum.NORMAL.getType())
                .setFormFields(List.of(buildFormField("day", "请假天数", List.of("required"))))
                .setProcessIdRule(processIdRule)
                .setTitleSetting(new BpmModelMetaInfoVO.TitleSetting().setEnable(true)
                        .setTitle("{" + BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_ID + "}的"
                                + "{" + BpmnVariableConstants.PROCESS_DEFINITION_NAME + "}，请假{day}天")));
        mockApprovalDetail(spyService, buildActivityNode("task1", "一级审批", BpmTaskCandidateStrategyEnum.USER),
                buildActivityNode("task2", "二级审批", BpmTaskCandidateStrategyEnum.START_USER_SELECT));
        when(adminUserApi.getUserMap(List.of(2L))).thenReturn(Map.of(2L, new AdminUserRespDTO().setId(2L)));
        when(adminUserApi.getUser(1L)).thenReturn(new AdminUserRespDTO().setId(1L).setNickname("张三"));
        when(processIdRedisDAO.generate(processIdRule)).thenReturn("QJ-001");
        ProcessInstanceBuilder processInstanceBuilder = mockProcessInstanceBuilder();
        Map<String, List<Long>> startUserSelectAssignees = Map.of("task2", List.of(2L));
        BpmProcessInstanceCreateReqVO reqVO = new BpmProcessInstanceCreateReqVO().setProcessDefinitionId("definition-1")
                .setVariables(new HashMap<>(Map.of("day", 3)))
                .setStartUserSelectAssignees(startUserSelectAssignees);

        // 调用
        String processInstanceId = spyService.createProcessInstance(1L, reqVO);
        // 断言
        assertEquals("process-instance-1", processInstanceId);
        verify(processInstanceBuilder).processDefinitionId("definition-1");
        verify(processInstanceBuilder).businessKey(null);
        verify(processInstanceBuilder).predefineProcessInstanceId("QJ-001");
        verify(processInstanceBuilder).name("张三的请假，请假3天");
        Map<String, Object> variables = captureVariables(processInstanceBuilder);
        assertEquals(3, variables.get("day"));
        assertEquals(1L, variables.get(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_ID));
        assertEquals(BpmProcessInstanceStatusEnum.RUNNING.getStatus(),
                variables.get(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS));
        assertEquals(true, variables.get(BpmnVariableConstants.PROCESS_INSTANCE_SKIP_EXPRESSION_ENABLED));
        assertEquals(startUserSelectAssignees,
                variables.get(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_SELECT_ASSIGNEES));
    }

    @Test
    public void testCreateProcessInstance_dto() {
        // 准备参数：未开启流程编号规则、标题设置
        BpmProcessInstanceServiceImpl spyService = spy(processInstanceService);
        when(processDefinitionService.getActiveProcessDefinition("leave")).thenReturn(processDefinition);
        when(processDefinition.getId()).thenReturn("definition-1");
        when(processDefinition.getName()).thenReturn("请假");
        BpmProcessDefinitionInfoDO processDefinitionInfo = buildProcessDefinitionInfo();
        when(processDefinitionService.getProcessDefinitionInfo("definition-1")).thenReturn(processDefinitionInfo);
        when(processDefinitionService.canUserStartProcessDefinition(processDefinitionInfo, 1L)).thenReturn(true);
        mockApprovalDetail(spyService);
        ProcessInstanceBuilder processInstanceBuilder = mockProcessInstanceBuilder();
        // 发起时，需要设置 Flowable 的认证用户，作为流程发起人
        List<String> authenticatedUserIds = new ArrayList<>();
        when(processInstanceBuilder.start()).thenAnswer(invocation -> {
            authenticatedUserIds.add(Authentication.getAuthenticatedUserId());
            return processInstance;
        });

        // 调用
        String processInstanceId = spyService.createProcessInstance(1L, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey("leave").setBusinessKey("business-1"));
        // 断言
        assertEquals("process-instance-1", processInstanceId);
        assertEquals(List.of("1"), authenticatedUserIds);
        assertNull(Authentication.getAuthenticatedUserId());
        verify(processInstanceBuilder).businessKey("business-1");
        verify(processInstanceBuilder).name("请假");
        verify(processInstanceBuilder, never()).predefineProcessInstanceId(anyString());
        Map<String, Object> variables = captureVariables(processInstanceBuilder);
        assertEquals(1L, variables.get(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_ID));
        assertFalse(variables.containsKey(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_SELECT_ASSIGNEES));
        verifyNoInteractions(processIdRedisDAO);
    }

    @Test
    public void testCreateProcessInstance_definitionNotExists() {
        // 准备参数
        BpmProcessInstanceCreateReqVO reqVO = new BpmProcessInstanceCreateReqVO().setProcessDefinitionId("definition-1");

        // 调用，并断言异常
        assertServiceException(() -> processInstanceService.createProcessInstance(1L, reqVO),
                PROCESS_DEFINITION_NOT_EXISTS);
        verifyNoInteractions(runtimeService);
    }

    @Test
    public void testCreateProcessInstance_dtoDefinitionNotExists() {
        // 调用
        RuntimeException exception = assertThrows(RuntimeException.class, () -> processInstanceService
                .createProcessInstance(1L, new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey("leave")));
        // 断言：ServiceException 被 FlowableUtils 包装，由全局异常处理器解包；认证用户需要被清理
        ServiceException serviceException = assertInstanceOf(ServiceException.class, exception.getCause());
        assertEquals(PROCESS_DEFINITION_NOT_EXISTS.getCode(), serviceException.getCode());
        assertNull(Authentication.getAuthenticatedUserId());
    }

    @Test
    public void testCreateProcessInstance_definitionSuspended() {
        // 准备参数
        when(processDefinitionService.getProcessDefinition("definition-1")).thenReturn(processDefinition);
        when(processDefinition.getId()).thenReturn("definition-1");
        when(processDefinition.isSuspended()).thenReturn(true);

        // 调用，并断言异常
        assertServiceException(() -> processInstanceService.createProcessInstance(1L,
                new BpmProcessInstanceCreateReqVO().setProcessDefinitionId("definition-1")), PROCESS_DEFINITION_IS_SUSPENDED);
    }

    @Test
    public void testCreateProcessInstance_definitionInfoNotExists() {
        // 准备参数
        when(processDefinitionService.getProcessDefinition("definition-1")).thenReturn(processDefinition);
        when(processDefinition.getId()).thenReturn("definition-1");

        // 调用，并断言异常
        assertServiceException(() -> processInstanceService.createProcessInstance(1L,
                new BpmProcessInstanceCreateReqVO().setProcessDefinitionId("definition-1")), PROCESS_DEFINITION_NOT_EXISTS);
    }

    @Test
    public void testCreateProcessInstance_userCannotStart() {
        // 准备参数
        when(processDefinitionService.getProcessDefinition("definition-1")).thenReturn(processDefinition);
        when(processDefinition.getId()).thenReturn("definition-1");
        when(processDefinitionService.getProcessDefinitionInfo("definition-1")).thenReturn(buildProcessDefinitionInfo());

        // 调用，并断言异常
        assertServiceException(() -> processInstanceService.createProcessInstance(1L,
                new BpmProcessInstanceCreateReqVO().setProcessDefinitionId("definition-1")),
                PROCESS_INSTANCE_START_USER_CAN_START);
        verifyNoInteractions(runtimeService);
    }

    @Test
    public void testCreateProcessInstance_formFieldRequired() {
        // 准备参数：必填标记为数组 ["required"]，字段未填写
        when(processDefinitionService.getProcessDefinition("definition-1")).thenReturn(processDefinition);
        when(processDefinition.getId()).thenReturn("definition-1");
        when(processDefinitionService.getProcessDefinitionInfo("definition-1")).thenReturn(buildProcessDefinitionInfo()
                .setFormType(BpmModelFormTypeEnum.NORMAL.getType())
                .setFormFields(List.of(buildFormField("day", "请假天数", List.of("required")))));

        // 调用，并断言异常
        assertServiceException(() -> processInstanceService.createProcessInstance(1L,
                new BpmProcessInstanceCreateReqVO().setProcessDefinitionId("definition-1")),
                PROCESS_INSTANCE_START_FORM_FIELD_REQUIRED, "请假天数");
        verifyNoInteractions(runtimeService);
    }

    @Test
    public void testCreateProcessInstance_formFieldRequiredBlank() {
        // 准备参数：字段标识为 key，必填标记为对象 {"required": true}，字段值为空白字符串
        when(processDefinitionService.getProcessDefinition("definition-1")).thenReturn(processDefinition);
        when(processDefinition.getId()).thenReturn("definition-1");
        String formField = JsonUtils.toJsonString(Map.of("key", "reason", "validate", Map.of("required", true)));
        when(processDefinitionService.getProcessDefinitionInfo("definition-1")).thenReturn(buildProcessDefinitionInfo()
                .setFormType(BpmModelFormTypeEnum.NORMAL.getType()).setFormFields(List.of(formField)));

        // 调用，并断言异常：没有 title 时，使用字段标识提示
        assertServiceException(() -> processInstanceService.createProcessInstance(1L,
                new BpmProcessInstanceCreateReqVO().setProcessDefinitionId("definition-1")
                        .setVariables(new HashMap<>(Map.of("reason", " ")))),
                PROCESS_INSTANCE_START_FORM_FIELD_REQUIRED, "reason");
    }

    @Test
    public void testCreateProcessInstance_startUserSelectAssigneesNotConfig() {
        // 准备参数：预测到发起人自选节点，但未选择审批人
        BpmProcessInstanceServiceImpl spyService = spy(processInstanceService);
        mockCreateProcessInstance(buildProcessDefinitionInfo());
        mockApprovalDetail(spyService,
                buildActivityNode("task2", "二级审批", BpmTaskCandidateStrategyEnum.START_USER_SELECT));

        // 调用，并断言异常
        assertServiceException(() -> spyService.createProcessInstance(1L,
                new BpmProcessInstanceCreateReqVO().setProcessDefinitionId("definition-1")),
                PROCESS_INSTANCE_START_USER_SELECT_ASSIGNEES_NOT_CONFIG, "二级审批");
        verifyNoInteractions(runtimeService);
    }

    @Test
    public void testCreateProcessInstance_startUserSelectAssigneesNotExists() {
        // 准备参数：选择的审批人不存在
        BpmProcessInstanceServiceImpl spyService = spy(processInstanceService);
        mockCreateProcessInstance(buildProcessDefinitionInfo());
        mockApprovalDetail(spyService,
                buildActivityNode("task2", "二级审批", BpmTaskCandidateStrategyEnum.START_USER_SELECT));
        when(adminUserApi.getUserMap(List.of(2L))).thenReturn(Map.of());

        // 调用，并断言异常
        assertServiceException(() -> spyService.createProcessInstance(1L,
                new BpmProcessInstanceCreateReqVO().setProcessDefinitionId("definition-1")
                        .setStartUserSelectAssignees(Map.of("task2", List.of(2L)))),
                PROCESS_INSTANCE_START_USER_SELECT_ASSIGNEES_NOT_EXISTS, "二级审批", 2L);
    }

    // ========== cancelProcessInstance 相关 ==========

    @Test
    public void testCancelProcessInstanceByAdmin_notExists() {
        // 准备参数
        BpmProcessInstanceServiceImpl spyService = spy(processInstanceService);
        doReturn(null).when(spyService).getProcessInstance("process-instance-1");

        // 调用，并断言异常
        assertServiceException(() -> spyService.cancelProcessInstanceByAdmin(1L, buildCancelReq()),
                PROCESS_INSTANCE_CANCEL_FAIL_NOT_EXISTS);
    }

    @Test
    public void testCancelProcessInstanceByStartUser_notExists() {
        // 准备参数
        BpmProcessInstanceServiceImpl spyService = spy(processInstanceService);
        doReturn(null).when(spyService).getProcessInstance("process-instance-1");
        BpmProcessInstanceCancelReqVO reqVO = buildCancelReq();

        // 调用，并断言异常
        assertServiceException(() -> spyService.cancelProcessInstanceByStartUser(1L, reqVO),
                PROCESS_INSTANCE_CANCEL_FAIL_NOT_EXISTS);
    }

    @Test
    public void testCancelProcessInstanceByStartUser_notAllow() {
        // 准备参数
        BpmProcessInstanceServiceImpl spyService = spy(processInstanceService);
        doReturn(processInstance).when(spyService).getProcessInstance("process-instance-1");
        when(processInstance.getStartUserId()).thenReturn("1");
        when(processInstance.getProcessDefinitionId()).thenReturn("definition-1");
        when(processDefinitionService.getProcessDefinitionInfo("definition-1"))
                .thenReturn(new BpmProcessDefinitionInfoDO().setAllowCancelRunningProcess(false));
        BpmProcessInstanceCancelReqVO reqVO = buildCancelReq();

        // 调用，并断言异常
        assertServiceException(() -> spyService.cancelProcessInstanceByStartUser(1L, reqVO),
                PROCESS_INSTANCE_CANCEL_FAIL_NOT_ALLOW);
    }

    // ========== Event 事件相关方法 ==========

    @Test
    public void testProcessProcessInstanceCompleted_approve() {
        // 准备参数：流程结束时仍为审批中，说明审批通过；配置了流程后置通知
        mockCompletedProcessInstance(BpmProcessInstanceStatusEnum.RUNNING.getStatus(), null);
        BpmModelMetaInfoVO.HttpRequestSetting setting = new BpmModelMetaInfoVO.HttpRequestSetting()
                .setUrl("http://127.0.0.1/after");
        when(processDefinitionService.getProcessDefinitionInfo("definition-1"))
                .thenReturn(new BpmProcessDefinitionInfoDO().setProcessAfterTriggerSetting(setting));

        // 调用
        try (MockedStatic<BpmHttpRequestUtils> httpRequestUtilsMockedStatic = mockStatic(BpmHttpRequestUtils.class)) {
            processInstanceService.processProcessInstanceCompleted(processInstance);
            // 断言：后置通知
            httpRequestUtilsMockedStatic.verify(() -> BpmHttpRequestUtils.executeBpmHttpRequest(processInstance,
                    "http://127.0.0.1/after", null, null, true, null));
        }
        // 断言：状态更新为审批通过
        verify(runtimeService).setVariable("process-instance-1", BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS,
                BpmProcessInstanceStatusEnum.APPROVE.getStatus());
        // 断言：审批通过消息
        ArgumentCaptor<BpmMessageSendWhenProcessInstanceApproveReqDTO> messageCaptor =
                ArgumentCaptor.forClass(BpmMessageSendWhenProcessInstanceApproveReqDTO.class);
        verify(messageService).sendMessageWhenProcessInstanceApprove(messageCaptor.capture());
        assertEquals("process-instance-1", messageCaptor.getValue().getProcessInstanceId());
        assertEquals("请假流程", messageCaptor.getValue().getProcessInstanceName());
        assertEquals(1L, messageCaptor.getValue().getStartUserId());
        // 断言：状态事件
        BpmProcessInstanceStatusEvent event = captureStatusEvent();
        assertEquals("process-instance-1", event.getId());
        assertEquals(BpmProcessInstanceStatusEnum.APPROVE.getStatus(), event.getStatus());
        assertEquals("leave", event.getProcessDefinitionKey());
        assertEquals("business-1", event.getBusinessKey());
    }

    @Test
    public void testProcessProcessInstanceCompleted_childRejectRolledBack() {
        // 准备参数
        BpmProcessInstanceServiceImpl spyService = spy(processInstanceService);
        ProcessInstance parentProcessInstance = mockChildRejectProcessInstance(spyService);

        // 调用：事务回滚
        executeWithSynchronization(() -> spyService.processProcessInstanceCompleted(processInstance),
                TransactionSynchronization.STATUS_ROLLED_BACK);
        // 断言：回滚时，不结束父流程
        verify(spyService).updateProcessInstanceReject(parentProcessInstance,
                BpmReasonEnum.REJECT_CHILD_PROCESS.getReason());
        verifyNoInteractions(taskService);
    }

    @Test
    public void testProcessProcessInstanceCreated_definitionNotExists() {
        // 准备参数
        when(processInstance.getProcessDefinitionId()).thenReturn("definition-1");

        // 调用
        executeWithSynchronization(() -> processInstanceService.processProcessInstanceCreated(processInstance),
                TransactionSynchronization.STATUS_COMMITTED);
        // 断言
        verifyNoInteractions(runtimeService, adminUserApi);
    }

    @Test
    public void testProcessProcessInstanceCreated_titleAndBeforeTrigger() {
        // 准备参数：开启标题设置，并配置流程前置通知
        BpmModelMetaInfoVO.HttpRequestSetting setting = new BpmModelMetaInfoVO.HttpRequestSetting()
                .setUrl("http://127.0.0.1/before");
        mockCreatedProcessInstance(buildProcessDefinitionInfo()
                .setTitleSetting(new BpmModelMetaInfoVO.TitleSetting().setEnable(true)
                        .setTitle("{" + BpmnVariableConstants.PROCESS_DEFINITION_NAME + "}-{day}天"))
                .setProcessBeforeTriggerSetting(setting));
        when(processInstance.getName()).thenReturn("请假");

        // 调用
        try (MockedStatic<BpmHttpRequestUtils> httpRequestUtilsMockedStatic = mockStatic(BpmHttpRequestUtils.class)) {
            executeWithSynchronization(() -> processInstanceService.processProcessInstanceCreated(processInstance),
                    TransactionSynchronization.STATUS_COMMITTED);
            // 断言
            verify(runtimeService).setProcessInstanceName("process-instance-1", "请假-3天");
            httpRequestUtilsMockedStatic.verify(() -> BpmHttpRequestUtils.executeBpmHttpRequest(processInstance,
                    "http://127.0.0.1/before", null, null, true, null));
        }
    }

    @Test
    public void testProcessProcessInstanceCreated_nameNotChanged() {
        // 准备参数：未开启标题设置，名称与流程定义一致
        mockCreatedProcessInstance(buildProcessDefinitionInfo());
        when(processInstance.getName()).thenReturn("请假");

        // 调用
        try (MockedStatic<BpmHttpRequestUtils> httpRequestUtilsMockedStatic = mockStatic(BpmHttpRequestUtils.class)) {
            executeWithSynchronization(() -> processInstanceService.processProcessInstanceCreated(processInstance),
                    TransactionSynchronization.STATUS_COMMITTED);
            // 断言：名称未变化时，不更新；未配置前置通知时，不发起请求
            verify(runtimeService, never()).setProcessInstanceName(anyString(), anyString());
            httpRequestUtilsMockedStatic.verifyNoInteractions();
        }
    }

    // ========== 私有方法 ==========

    private static BpmProcessInstanceCancelReqVO buildCancelReq() {
        return new BpmProcessInstanceCancelReqVO().setId("process-instance-1").setReason("撤销申请");
    }

    private static BpmProcessDefinitionInfoDO buildProcessDefinitionInfo() {
        return new BpmProcessDefinitionInfoDO().setProcessDefinitionId("definition-1");
    }

    private static String buildFormField(String field, String title, Object validate) {
        return JsonUtils.toJsonString(Map.of("field", field, "title", title, "validate", validate));
    }

    private static BpmApprovalDetailRespVO.ActivityNode buildActivityNode(String id, String name,
                                                                         BpmTaskCandidateStrategyEnum strategy) {
        return new BpmApprovalDetailRespVO.ActivityNode().setId(id).setName(name)
                .setCandidateStrategy(strategy.getStrategy());
    }

    private void mockCreateProcessInstance(BpmProcessDefinitionInfoDO processDefinitionInfo) {
        when(processDefinitionService.getProcessDefinition("definition-1")).thenReturn(processDefinition);
        when(processDefinition.getId()).thenReturn("definition-1");
        lenient().when(processDefinition.getName()).thenReturn(" 请假 ");
        when(processDefinitionService.getProcessDefinitionInfo("definition-1")).thenReturn(processDefinitionInfo);
        when(processDefinitionService.canUserStartProcessDefinition(processDefinitionInfo, 1L)).thenReturn(true);
    }

    /**
     * 模拟发起流程时的审批节点预测结果
     */
    private void mockApprovalDetail(BpmProcessInstanceServiceImpl spyService,
                                    BpmApprovalDetailRespVO.ActivityNode... activityNodes) {
        doReturn(new BpmApprovalDetailRespVO().setActivityNodes(new ArrayList<>(List.of(activityNodes))))
                .when(spyService).getApprovalDetail(eq(1L), any());
    }

    private ProcessInstanceBuilder mockProcessInstanceBuilder() {
        ProcessInstanceBuilder processInstanceBuilder = mock(ProcessInstanceBuilder.class, RETURNS_SELF);
        when(runtimeService.createProcessInstanceBuilder()).thenReturn(processInstanceBuilder);
        lenient().when(processInstanceBuilder.start()).thenReturn(processInstance);
        when(processInstance.getId()).thenReturn("process-instance-1");
        return processInstanceBuilder;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> captureVariables(ProcessInstanceBuilder processInstanceBuilder) {
        ArgumentCaptor<Map<String, Object>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(processInstanceBuilder).variables(variablesCaptor.capture());
        return variablesCaptor.getValue();
    }

    private void mockCompletedProcessInstance(Integer status, String reason) {
        Map<String, Object> variables = new HashMap<>();
        variables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS, status);
        variables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_REASON, reason);
        when(processInstance.getProcessVariables()).thenReturn(variables);
        lenient().when(processInstance.getId()).thenReturn("process-instance-1");
        lenient().when(processInstance.getName()).thenReturn("请假流程");
        lenient().when(processInstance.getStartUserId()).thenReturn("1");
        lenient().when(processInstance.getProcessDefinitionId()).thenReturn("definition-1");
        lenient().when(processInstance.getProcessDefinitionKey()).thenReturn("leave");
        lenient().when(processInstance.getBusinessKey()).thenReturn("business-1");
    }

    private ProcessInstance mockChildRejectProcessInstance(BpmProcessInstanceServiceImpl spyService) {
        mockCompletedProcessInstance(BpmProcessInstanceStatusEnum.REJECT.getStatus(), "不同意");
        when(processInstance.getSuperExecutionId()).thenReturn("super-execution-1");
        ExecutionQuery executionQuery = mock(ExecutionQuery.class, RETURNS_SELF);
        when(runtimeService.createExecutionQuery()).thenReturn(executionQuery);
        Execution execution = mock(Execution.class);
        when(execution.getProcessInstanceId()).thenReturn("parent-process-instance-1");
        when(executionQuery.singleResult()).thenReturn(execution);
        ProcessInstance parentProcessInstance = mock(ProcessInstance.class);
        lenient().when(parentProcessInstance.getId()).thenReturn("parent-process-instance-1");
        when(parentProcessInstance.getProcessInstanceId()).thenReturn("parent-process-instance-1");
        doReturn(parentProcessInstance).when(spyService).getProcessInstance("parent-process-instance-1");
        return parentProcessInstance;
    }

    private void mockCreatedProcessInstance(BpmProcessDefinitionInfoDO processDefinitionInfo) {
        when(processInstance.getProcessDefinitionId()).thenReturn("definition-1");
        lenient().when(processInstance.getProcessInstanceId()).thenReturn("process-instance-1");
        when(processInstance.getStartUserId()).thenReturn("1");
        lenient().when(processInstance.getProcessVariables()).thenReturn(Map.of("day", 3));
        when(processDefinitionService.getProcessDefinitionInfo("definition-1")).thenReturn(processDefinitionInfo);
        when(processDefinitionService.getProcessDefinition("definition-1")).thenReturn(processDefinition);
        when(processDefinition.getName()).thenReturn("请假");
        lenient().when(adminUserApi.getUser(1L)).thenReturn(new AdminUserRespDTO().setId(1L).setNickname("张三"));
    }

    private BpmProcessInstanceStatusEvent captureStatusEvent() {
        ArgumentCaptor<BpmProcessInstanceStatusEvent> eventCaptor =
                ArgumentCaptor.forClass(BpmProcessInstanceStatusEvent.class);
        verify(processInstanceEventPublisher).sendProcessInstanceResultEvent(eventCaptor.capture());
        return eventCaptor.getValue();
    }

    /**
     * 在事务同步上下文中执行，并模拟事务提交（或回滚）回调
     */
    private static void executeWithSynchronization(Runnable runnable, int transactionStatus) {
        TransactionSynchronizationManager.initSynchronization();
        try {
            runnable.run();
            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
            if (transactionStatus == TransactionSynchronization.STATUS_COMMITTED) {
                synchronizations.forEach(TransactionSynchronization::afterCommit);
            }
            synchronizations.forEach(synchronization -> synchronization.afterCompletion(transactionStatus));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

}
