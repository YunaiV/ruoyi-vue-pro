package cn.iocoder.yudao.module.bpm.framework.flowable;

import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmReasonEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.listener.BpmTaskEventListener;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.listener.BpmCallActivityListener;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.START_USER_NODE_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 任务创建、分配事件后自动处理，以及子流程发起的 Flowable 集成测试
 *
 * <p>补充 {@link BpmTaskServiceFlowableTest} 未覆盖的 {@link BpmTaskServiceImpl#processTaskCreated}、
 * {@link BpmTaskServiceImpl#processTaskAssigned} 分支，以及 {@link BpmCallActivityListener}、
 * {@link BpmProcessInstanceServiceImpl#processProcessInstanceCreated} 在真实事务提交后的回调。
 * 事件均由 {@link BpmTaskEventListener} 等真实监听器触发。</p>
 *
 * @author HUIHUI
 */
public class BpmProcessLifecycleFlowableTest extends BaseFlowableUnitTest {

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private HistoryService historyService;
    @Resource
    private TaskService flowableTaskService;

    @Resource
    private BpmProcessInstanceServiceImpl processInstanceService;

    private final List<String> deploymentIds = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        mockFlowableBackedServices(repositoryService);
    }

    @AfterEach
    public void tearDown() {
        // 级联删除流程定义、流程实例与历史数据
        deploymentIds.forEach(id -> repositoryService.deleteDeployment(id, true));
    }

    // ========== 任务创建后的自动处理 ==========

    @Test
    public void testProcessTaskCreated_assignEmptyApprove() {
        // 准备参数：人工审批，计算不到审批人时自动通过
        deploy("bpmn/lifecycle/assign-empty-approve.bpmn20.xml");
        when(taskCandidateInvoker.calculateUsersByTask(any())).thenReturn(Set.of());

        // 调用
        String processInstanceId = startProcessInstance("assignEmptyApprove", 1L);
        // 断言：任务没有审批人，在事务完成后自动通过，且不会发送待办通知
        HistoricTaskInstance task = getHistoricTask(processInstanceId, "task");
        assertNull(task.getAssignee());
        assertTaskStatus(task.getId(), BpmTaskStatusEnum.APPROVE.getStatus(), BpmReasonEnum.ASSIGN_EMPTY_APPROVE.getReason());
        assertProcessInstanceStatus(processInstanceId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
        verify(messageService, never()).sendMessageWhenTaskAssigned(any());
    }

    @Test
    public void testProcessTaskCreated_assignEmptyReject() {
        // 准备参数：人工审批，计算不到审批人时自动拒绝
        deploy("bpmn/lifecycle/assign-empty-reject.bpmn20.xml");
        when(taskCandidateInvoker.calculateUsersByTask(any())).thenReturn(Set.of());

        // 调用
        String processInstanceId = startProcessInstance("assignEmptyReject", 1L);
        // 断言：任务没有审批人，在事务完成后自动拒绝，流程不通过
        HistoricTaskInstance task = getHistoricTask(processInstanceId, "task");
        assertNull(task.getAssignee());
        assertTaskStatus(task.getId(), BpmTaskStatusEnum.REJECT.getStatus(), BpmReasonEnum.ASSIGN_EMPTY_REJECT.getReason());
        assertProcessInstanceStatus(processInstanceId, BpmProcessInstanceStatusEnum.REJECT.getStatus());
        verify(messageService, never()).sendMessageWhenTaskAssigned(any());
    }

    // ========== 任务分配后的自动处理 ==========

    @Test
    public void testProcessTaskAssigned_startUserNodeAutoApprove() {
        // 准备参数
        deploy("bpmn/lifecycle/start-user-node.bpmn20.xml");

        // 调用
        String processInstanceId = startProcessInstance("startUserNode", 1L);
        // 断言：发起人节点首次自动通过，流转到下一个审批节点
        assertTaskStatus(getHistoricTask(processInstanceId, START_USER_NODE_ID).getId(),
                BpmTaskStatusEnum.APPROVE.getStatus(),
                BpmReasonEnum.ASSIGN_START_USER_APPROVE_WHEN_SKIP_START_USER_NODE.getReason());
        Task task = getRunningTask(processInstanceId);
        assertEquals("task", task.getTaskDefinitionKey());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), FlowableUtils.getTaskStatus(task));
    }

    @Test
    public void testProcessTaskAssigned_startUserSkip() {
        // 准备参数：审批人与发起人相同时，自动跳过
        deploy("bpmn/lifecycle/start-user-skip.bpmn20.xml");

        // 调用 + 断言：发起人 1 = 审批人 1，自动通过
        String processInstanceId = startProcessInstance("startUserSkip", 1L);
        assertTaskStatus(getHistoricTask(processInstanceId, "task").getId(), BpmTaskStatusEnum.APPROVE.getStatus(),
                BpmReasonEnum.ASSIGN_START_USER_APPROVE_WHEN_SKIP.getReason());
        assertProcessInstanceStatus(processInstanceId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());

        // 调用 + 断言：发起人 2 ≠ 审批人 1，正常待审批并发送待办通知
        String otherProcessInstanceId = startProcessInstance("startUserSkip", 2L);
        Task task = getRunningTask(otherProcessInstanceId);
        assertEquals("1", task.getAssignee());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), FlowableUtils.getTaskStatus(task));
        verify(messageService).sendMessageWhenTaskAssigned(argThat(reqDTO -> task.getId().equals(reqDTO.getTaskId())));
    }

    @Test
    public void testProcessTaskAssigned_startUserTransferDeptLeader() {
        // 准备参数：审批人与发起人相同时转交部门负责人；发起人 1 的部门负责人为 2
        deploy("bpmn/lifecycle/start-user-transfer-dept-leader.bpmn20.xml");
        when(adminUserApi.getUser(1L)).thenReturn(new AdminUserRespDTO().setId(1L).setNickname("用户1").setDeptId(10L));
        when(deptApi.getDept(10L)).thenReturn(new DeptRespDTO().setId(10L).setLeaderUserId(2L));

        // 调用
        String processInstanceId = startProcessInstance("startUserTransferDeptLeader", 1L);
        // 断言：任务转交给部门负责人，原审批人成为 owner，仅向部门负责人发送待办通知
        Task task = getRunningTask(processInstanceId);
        assertEquals("2", task.getAssignee());
        assertEquals("1", task.getOwner());
        verify(messageService).sendMessageWhenTaskAssigned(argThat(reqDTO ->
                task.getId().equals(reqDTO.getTaskId()) && reqDTO.getAssigneeUserId().equals(2L)));
        verify(messageService, times(1)).sendMessageWhenTaskAssigned(any());
    }

    @Test
    public void testProcessTaskAssigned_startUserDeptLeaderNotFound() {
        // 准备参数：审批人与发起人相同时转交部门负责人；发起人 1 的部门没有负责人
        deploy("bpmn/lifecycle/start-user-transfer-dept-leader.bpmn20.xml");
        when(adminUserApi.getUser(1L)).thenReturn(new AdminUserRespDTO().setId(1L).setNickname("用户1").setDeptId(10L));
        when(deptApi.getDept(10L)).thenReturn(new DeptRespDTO().setId(10L));

        // 调用
        String processInstanceId = startProcessInstance("startUserTransferDeptLeader", 1L);
        // 断言：找不到部门负责人时自动通过
        assertTaskStatus(getHistoricTask(processInstanceId, "task").getId(), BpmTaskStatusEnum.APPROVE.getStatus(),
                BpmReasonEnum.ASSIGN_START_USER_APPROVE_WHEN_DEPT_LEADER_NOT_FOUND.getReason());
        assertProcessInstanceStatus(processInstanceId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
    }

    // ========== 子流程发起 ==========

    @Test
    public void testCallActivity_startUserAndTitle() {
        // 准备参数：开启自定义标题
        deploy("bpmn/lifecycle/child.bpmn20.xml");
        deploy("bpmn/lifecycle/child-parent.bpmn20.xml");
        when(processDefinitionService.getProcessDefinitionInfo(anyString())).thenAnswer(invocation ->
                new BpmProcessDefinitionInfoDO().setProcessDefinitionId(invocation.getArgument(0))
                        .setModelType(BpmModelTypeEnum.BPMN.getType())
                        .setTitleSetting(new BpmModelMetaInfoVO.TitleSetting().setEnable(true)
                                .setTitle("{PROCESS_DEFINITION_NAME}-{PROCESS_START_USER_ID}")));

        // 调用：用户 2 发起主流程
        String processInstanceId = startProcessInstance("childParent", 2L);
        // 断言：主流程标题在发起时生成
        assertEquals("主流程-用户2", processInstanceService.getProcessInstance(processInstanceId).getName());
        // 断言：子流程发起人同主流程（BpmCallActivityListener），标题在 PROCESS_CREATED 事务提交后生成
        ProcessInstance child = runtimeService.createProcessInstanceQuery()
                .superProcessInstanceId(processInstanceId).singleResult();
        assertNotNull(child);
        assertEquals("2", child.getStartUserId());
        assertEquals("子流程-用户2", processInstanceService.getProcessInstance(child.getId()).getName());
        Task childTask = getRunningTask(child.getId());
        assertEquals("task", childTask.getTaskDefinitionKey());
        assertEquals("子流程审批", childTask.getName());
    }

    // ========== 私有方法 ==========

    private void deploy(String resource) {
        deploymentIds.add(repositoryService.createDeployment().addClasspathResource(resource).deploy().getId());
    }

    private String startProcessInstance(String processDefinitionKey, Long userId) {
        return processInstanceService.createProcessInstance(userId, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey(processDefinitionKey).setBusinessKey("business-1"));
    }

    private Task getRunningTask(String processInstanceId) {
        List<Task> tasks = flowableTaskService.createTaskQuery().processInstanceId(processInstanceId)
                .includeTaskLocalVariables().list();
        assertEquals(1, tasks.size());
        return tasks.get(0);
    }

    private HistoricTaskInstance getHistoricTask(String processInstanceId, String taskDefinitionKey) {
        return historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId)
                .taskDefinitionKey(taskDefinitionKey).includeTaskLocalVariables().singleResult();
    }

    private void assertTaskStatus(String taskId, Integer status, String reason) {
        HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery().taskId(taskId)
                .includeTaskLocalVariables().singleResult();
        assertEquals(status, FlowableUtils.getTaskStatus(task));
        assertEquals(reason, FlowableUtils.getTaskReason(task));
    }

    private void assertProcessInstanceStatus(String processInstanceId, Integer status) {
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).includeProcessVariables().singleResult();
        assertNotNull(processInstance.getEndTime());
        assertEquals(status, FlowableUtils.getProcessInstanceStatus(processInstance));
    }

}
