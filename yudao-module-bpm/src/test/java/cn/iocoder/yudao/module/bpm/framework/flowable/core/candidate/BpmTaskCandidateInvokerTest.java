package cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskAssignStartUserHandlerTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.strategy.other.BpmTaskCandidateAssignEmptyStrategy;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.strategy.user.BpmTaskCandidateUserStrategy;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.internal.util.collections.Sets;

import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.flowable.bpmn.constants.BpmnXMLConstants.FLOWABLE_EXTENSIONS_NAMESPACE;
import static org.flowable.bpmn.constants.BpmnXMLConstants.FLOWABLE_EXTENSIONS_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link BpmTaskCandidateInvoker} 的单元测试
 *
 * @author 芋道源码
 */
public class BpmTaskCandidateInvokerTest extends BaseMockitoUnitTest {

    private BpmTaskCandidateInvoker taskCandidateInvoker;

    @Mock
    private AdminUserApi adminUserApi;

    @Mock
    private BpmProcessInstanceService processInstanceService;

    @Spy
    private BpmTaskCandidateStrategy userStrategy;
    @Mock
    private BpmTaskCandidateAssignEmptyStrategy emptyStrategy;

    @Spy
    private List<BpmTaskCandidateStrategy> strategyList;

    @BeforeEach
    public void setUp() {
        userStrategy = new BpmTaskCandidateUserStrategy(); // 创建 strategy 实例
        when(emptyStrategy.getStrategy()).thenReturn(BpmTaskCandidateStrategyEnum.ASSIGN_EMPTY);
        strategyList = ListUtil.of(userStrategy, emptyStrategy); // 创建 strategyList
        taskCandidateInvoker = new BpmTaskCandidateInvoker(strategyList, adminUserApi);
    }

    /**
     * 场景：成功计算到候选人，但是移除了发起人的用户
     */
    @Test
    public void testCalculateUsersByTask_some() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            // 准备参数
            String param = "1,2";
            DelegateExecution execution = mock(DelegateExecution.class);
            // mock 方法（DelegateExecution）
            UserTask userTask = mock(UserTask.class);
            String processInstanceId = randomString();
            when(execution.getProcessInstanceId()).thenReturn(processInstanceId);
            when(execution.getCurrentFlowElement()).thenReturn(userTask);
            when(userTask.getAttributeValue(eq(BpmnModelConstants.NAMESPACE), eq(BpmnModelConstants.USER_TASK_CANDIDATE_STRATEGY)))
                    .thenReturn(BpmTaskCandidateStrategyEnum.USER.getStrategy().toString());
            when(userTask.getAttributeValue(eq(BpmnModelConstants.NAMESPACE), eq(BpmnModelConstants.USER_TASK_CANDIDATE_PARAM)))
                    .thenReturn(param);
            // mock 方法（adminUserApi）
            AdminUserRespDTO user1 = randomPojo(AdminUserRespDTO.class, o -> o.setId(1L)
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()));
            AdminUserRespDTO user2 = randomPojo(AdminUserRespDTO.class, o -> o.setId(2L)
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()));
            Map<Long, AdminUserRespDTO> userMap = MapUtil.builder(user1.getId(), user1)
                    .put(user2.getId(), user2).build();
            when(adminUserApi.getUserMap(eq(asSet(1L, 2L)))).thenReturn(userMap);
            // mock 移除发起人的用户
            springUtilMockedStatic.when(() -> SpringUtil.getBean(BpmProcessInstanceService.class))
                    .thenReturn(processInstanceService);
            ProcessInstance processInstance = mock(ProcessInstance.class);
            when(processInstanceService.getProcessInstance(eq(processInstanceId))).thenReturn(processInstance);
            when(processInstance.getStartUserId()).thenReturn("1");
            mockFlowElementExtensionElement(userTask, BpmnModelConstants.USER_TASK_ASSIGN_START_USER_HANDLER_TYPE,
                    String.valueOf(BpmUserTaskAssignStartUserHandlerTypeEnum.SKIP.getType()));

            // 调用
            Set<Long> results = taskCandidateInvoker.calculateUsersByTask(execution);
            // 断言
            assertEquals(asSet(2L), results);
        }
    }

    /**
     * 场景：没有计算到候选人，但是被禁用移除，最终通过 empty 进行分配
     */
    @Test
    public void testCalculateUsersByTask_none() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            // 准备参数
            String param = "1,2";
            DelegateExecution execution = mock(DelegateExecution.class);
            // mock 方法（DelegateExecution）
            UserTask userTask = mock(UserTask.class);
            String processInstanceId = randomString();
            when(execution.getProcessInstanceId()).thenReturn(processInstanceId);
            when(execution.getCurrentFlowElement()).thenReturn(userTask);
            when(userTask.getAttributeValue(eq(BpmnModelConstants.NAMESPACE), eq(BpmnModelConstants.USER_TASK_CANDIDATE_STRATEGY)))
                    .thenReturn(BpmTaskCandidateStrategyEnum.USER.getStrategy().toString());
            when(userTask.getAttributeValue(eq(BpmnModelConstants.NAMESPACE), eq(BpmnModelConstants.USER_TASK_CANDIDATE_PARAM)))
                    .thenReturn(param);
            // mock 方法（adminUserApi）
            AdminUserRespDTO user1 = randomPojo(AdminUserRespDTO.class, o -> o.setId(1L)
                    .setStatus(CommonStatusEnum.DISABLE.getStatus()));
            AdminUserRespDTO user2 = randomPojo(AdminUserRespDTO.class, o -> o.setId(2L)
                    .setStatus(CommonStatusEnum.DISABLE.getStatus()));
            Map<Long, AdminUserRespDTO> userMap = MapUtil.builder(user1.getId(), user1)
                    .put(user2.getId(), user2).build();
            when(adminUserApi.getUserMap(eq(asSet(1L, 2L)))).thenReturn(userMap);
            // mock 方法（empty）
            when(emptyStrategy.calculateUsersByTask(same(execution), same(param)))
                    .thenReturn(Sets.newSet(2L));
            // mock 移除发起人的用户
            springUtilMockedStatic.when(() -> SpringUtil.getBean(BpmProcessInstanceService.class))
                    .thenReturn(processInstanceService);
            ProcessInstance processInstance = mock(ProcessInstance.class);
            when(processInstanceService.getProcessInstance(eq(processInstanceId))).thenReturn(processInstance);
            when(processInstance.getStartUserId()).thenReturn("1");

            // 调用
            Set<Long> results = taskCandidateInvoker.calculateUsersByTask(execution);
            // 断言
            assertEquals(asSet(2L), results);
        }
    }

    /**
     * 场景：没有计算到候选人，但是被禁用移除，最终通过 empty 进行分配
     */
    @Test
    public void testCalculateUsersByActivity_some() {
        try (MockedStatic<BpmnModelUtils> bpmnModelUtilsMockedStatic = mockStatic(BpmnModelUtils.class)) {
            // 准备参数
            String param = "1,2";
            BpmnModel bpmnModel = mock(BpmnModel.class);
            String activityId = randomString();
            Long startUserId = 1L;
            String processDefinitionId = randomString();
            Map<String, Object> processVariables = new HashMap<>();
            // mock 方法（DelegateExecution）
            UserTask userTask = mock(UserTask.class);
            bpmnModelUtilsMockedStatic.when(() -> BpmnModelUtils.parseCandidateStrategy(same(userTask)))
                    .thenReturn(BpmTaskCandidateStrategyEnum.USER.getStrategy());
            bpmnModelUtilsMockedStatic.when(() -> BpmnModelUtils.parseCandidateParam(same(userTask)))
                    .thenReturn(param);
            bpmnModelUtilsMockedStatic.when(() -> BpmnModelUtils.getFlowElementById(same(bpmnModel), eq(activityId))).thenReturn(userTask);
            // mock 方法（adminUserApi）
            AdminUserRespDTO user1 = randomPojo(AdminUserRespDTO.class, o -> o.setId(1L)
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()));
            AdminUserRespDTO user2 = randomPojo(AdminUserRespDTO.class, o -> o.setId(2L)
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()));
            Map<Long, AdminUserRespDTO> userMap = MapUtil.builder(user1.getId(), user1)
                    .put(user2.getId(), user2).build();
            when(adminUserApi.getUserMap(eq(asSet(1L, 2L)))).thenReturn(userMap);
            // mock 移除发起人的用户
            bpmnModelUtilsMockedStatic.when(() -> BpmnModelUtils.parseAssignStartUserHandlerType(same(userTask)))
                    .thenReturn(BpmUserTaskAssignStartUserHandlerTypeEnum.SKIP.getType());

            // 调用
            Set<Long> results = taskCandidateInvoker.calculateUsersByActivity(bpmnModel, activityId,
                    startUserId, processDefinitionId, processVariables);
            // 断言
            assertEquals(asSet(2L), results);
        }
    }

    /**
     * 场景：成功计算到候选人，但是移除了发起人的用户
     */
    @Test
    public void testCalculateUsersByActivity_none() {
        try (MockedStatic<BpmnModelUtils> bpmnModelUtilsMockedStatic = mockStatic(BpmnModelUtils.class)) {
            // 准备参数
            String param = "1,2";
            BpmnModel bpmnModel = mock(BpmnModel.class);
            String activityId = randomString();
            Long startUserId = 1L;
            String processDefinitionId = randomString();
            Map<String, Object> processVariables = new HashMap<>();
            // mock 方法（DelegateExecution）
            UserTask userTask = mock(UserTask.class);
            bpmnModelUtilsMockedStatic.when(() -> BpmnModelUtils.parseCandidateStrategy(same(userTask)))
                    .thenReturn(BpmTaskCandidateStrategyEnum.USER.getStrategy());
            bpmnModelUtilsMockedStatic.when(() -> BpmnModelUtils.parseCandidateParam(same(userTask)))
                    .thenReturn(param);
            bpmnModelUtilsMockedStatic.when(() -> BpmnModelUtils.getFlowElementById(same(bpmnModel), eq(activityId))).thenReturn(userTask);
            // mock 方法（adminUserApi）
            AdminUserRespDTO user1 = randomPojo(AdminUserRespDTO.class, o -> o.setId(1L)
                    .setStatus(CommonStatusEnum.DISABLE.getStatus()));
            AdminUserRespDTO user2 = randomPojo(AdminUserRespDTO.class, o -> o.setId(2L)
                    .setStatus(CommonStatusEnum.DISABLE.getStatus()));
            Map<Long, AdminUserRespDTO> userMap = MapUtil.builder(user1.getId(), user1)
                    .put(user2.getId(), user2).build();
            when(adminUserApi.getUserMap(eq(asSet(1L, 2L)))).thenReturn(userMap);
            // mock 方法（empty）
            when(emptyStrategy.calculateUsersByActivity(same(bpmnModel), eq(activityId),
                            eq(param), same(startUserId), same(processDefinitionId), same(processVariables)))
                    .thenReturn(Sets.newSet(2L));

            // 调用
            Set<Long> results = taskCandidateInvoker.calculateUsersByActivity(bpmnModel, activityId,
                    startUserId, processDefinitionId, processVariables);
            // 断言
            assertEquals(asSet(2L), results);
        }
    }

    private static void mockFlowElementExtensionElement(FlowElement element, String name, String value) {
        if (value == null) {
            return;
        }
        ExtensionElement extensionElement = new ExtensionElement();
        extensionElement.setNamespace(FLOWABLE_EXTENSIONS_NAMESPACE);
        extensionElement.setNamespacePrefix(FLOWABLE_EXTENSIONS_PREFIX);
        extensionElement.setElementText(value);
        extensionElement.setName(name);
        // mock
        Map<String, List<ExtensionElement>> extensionElements = element.getExtensionElements();
        if (extensionElements == null) {
            extensionElements = new LinkedHashMap<>();
        }
        extensionElements.put(name, Collections.singletonList(extensionElement));
        when(element.getExtensionElements()).thenReturn(extensionElements);
    }

    @Test
    public void testRemoveDisableUsers() {
        // 准备参数. 1L 可以找到；2L 是禁用的；3L 找不到
        Set<Long> assigneeUserIds = asSet(1L, 2L, 3L);
        // mock 方法
        AdminUserRespDTO user1 = randomPojo(AdminUserRespDTO.class, o -> o.setId(1L)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        AdminUserRespDTO user2 = randomPojo(AdminUserRespDTO.class, o -> o.setId(2L)
                .setStatus(CommonStatusEnum.DISABLE.getStatus()));
        Map<Long, AdminUserRespDTO> userMap = MapUtil.builder(user1.getId(), user1)
                .put(user2.getId(), user2).build();
        when(adminUserApi.getUserMap(eq(assigneeUserIds))).thenReturn(userMap);

        // 调用
        taskCandidateInvoker.removeDisableUsers(assigneeUserIds);
        // 断言
        assertEquals(asSet(1L), assigneeUserIds);
    }

}
