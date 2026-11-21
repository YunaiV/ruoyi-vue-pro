package cn.iocoder.yudao.module.bpm.framework.flowable;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO.ActivityNode;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceBpmnModelViewRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstancePageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRejectReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskReturnReqVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModelNodeTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceQueryServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceUpdateServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskUpdateServiceImpl;
import jakarta.annotation.Resource;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.PROCESS_DEFINITION_NOT_EXISTS;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.PROCESS_INSTANCE_NOT_EXISTS;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.START_USER_NODE_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * {@link BpmProcessInstanceQueryServiceImpl} 查询相关方法的 Flowable 集成测试
 *
 * <p>流程实例、任务的状态由真实的 Flowable 事件监听器维护，覆盖审批详情、流程图进度、分页、下一节点预测等基于历史数据的查询。</p>
 *
 * @author HUIHUI
 */
public class BpmProcessInstanceQueryFlowableTest extends BaseFlowableUnitTest {

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private HistoryService historyService;
    @Resource
    private TaskService flowableTaskService;

    @Resource
    private BpmTaskUpdateServiceImpl taskUpdateService;
    @Resource
    private BpmProcessInstanceQueryServiceImpl processInstanceQueryService;
    @Resource
    private BpmProcessInstanceUpdateServiceImpl processInstanceUpdateService;

    private final List<String> deploymentIds = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        // 两级审批流程：task1 -> task2；审批人均为用户 1，候选人预测同样为用户 1
        deploy("bpmn/two-step-approve.bpmn20.xml");
        mockFlowableBackedServices(repositoryService);
        when(taskCandidateInvoker.calculateUsersByActivity(any(), any(), any(), any(), any()))
                .thenReturn(new LinkedHashSet<>(List.of(1L)));
    }

    @AfterEach
    public void tearDown() {
        deploymentIds.forEach(id -> repositoryService.deleteDeployment(id, true));
    }

    // ========== getApprovalDetail ==========

    @Test
    public void testGetApprovalDetail_running() {
        // 准备参数：task1 已审批，task2 待审批
        String processInstanceId = startProcessInstance(1L, null);
        approveRunningTask(processInstanceId);
        Task task2 = getRunningTask(processInstanceId);

        // 调用
        BpmApprovalDetailRespVO respVO = processInstanceQueryService.getApprovalDetail(1L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId));
        // 断言：已结束（发起人、task1） + 进行中（task2） + 预测（结束节点）
        assertEquals(BpmProcessInstanceStatusEnum.RUNNING.getStatus(), respVO.getStatus());
        List<ActivityNode> nodes = respVO.getActivityNodes();
        assertEquals(List.of(START_USER_NODE_ID, "task1", "task2", "end"), convertList(nodes, ActivityNode::getId));
        assertEquals(List.of(BpmTaskStatusEnum.APPROVE.getStatus(), BpmTaskStatusEnum.APPROVE.getStatus(),
                        BpmTaskStatusEnum.RUNNING.getStatus(), BpmTaskStatusEnum.NOT_START.getStatus()),
                convertList(nodes, ActivityNode::getStatus));
        assertEquals(BpmSimpleModelNodeTypeEnum.START_USER_NODE.getType(), nodes.get(0).getNodeType());
        assertEquals(1L, nodes.get(0).getTasks().get(0).getAssignee());
        assertEquals("同意", nodes.get(1).getTasks().get(0).getReason());
        assertEquals("用户1", nodes.get(2).getTasks().get(0).getAssigneeUser().getNickname());
        assertEquals(BpmSimpleModelNodeTypeEnum.END_NODE.getType(), nodes.get(3).getNodeType());
        // 断言：当前用户的待办为 task2，并返回流程实例信息
        assertEquals(task2.getId(), respVO.getTodoTask().getId());
        assertEquals(processInstanceId, respVO.getProcessInstance().getId());
        assertEquals("用户1", respVO.getProcessInstance().getStartUser().getNickname());
    }

    @Test
    public void testGetApprovalDetail_approved() {
        // 准备参数：task1、task2 均审批通过
        String processInstanceId = startProcessInstance(1L, null);
        approveRunningTask(processInstanceId);
        approveRunningTask(processInstanceId);

        // 调用
        BpmApprovalDetailRespVO respVO = processInstanceQueryService.getApprovalDetail(1L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId));
        // 断言：流程已结束，不再预测；最后为审批通过的结束节点
        assertEquals(BpmProcessInstanceStatusEnum.APPROVE.getStatus(), respVO.getStatus());
        List<ActivityNode> nodes = respVO.getActivityNodes();
        assertEquals(4, nodes.size());
        assertEquals(List.of(START_USER_NODE_ID, "task1", "task2"), convertList(nodes.subList(0, 3), ActivityNode::getId));
        ActivityNode endNode = nodes.get(3);
        assertEquals(BpmSimpleModelNodeTypeEnum.END_NODE.getType(), endNode.getNodeType());
        assertEquals(BpmProcessInstanceStatusEnum.APPROVE.getStatus(), endNode.getStatus());
        assertNotNull(endNode.getEndTime());
        assertNull(respVO.getTodoTask());
    }

    @Test
    public void testGetApprovalDetail_rejected() {
        // 准备参数：task1 审批不通过
        String processInstanceId = startProcessInstance(1L, null);
        Task task1 = getRunningTask(processInstanceId);
        taskUpdateService.rejectTask(1L, new BpmTaskRejectReqVO().setId(task1.getId()).setReason("不同意"));

        // 调用
        BpmApprovalDetailRespVO respVO = processInstanceQueryService.getApprovalDetail(1L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId));
        // 断言：不通过时不展示结束节点
        assertEquals(BpmProcessInstanceStatusEnum.REJECT.getStatus(), respVO.getStatus());
        List<ActivityNode> nodes = respVO.getActivityNodes();
        assertEquals(List.of(START_USER_NODE_ID, "task1"), convertList(nodes, ActivityNode::getId));
        assertEquals(BpmTaskStatusEnum.REJECT.getStatus(), nodes.get(1).getStatus());
        assertEquals("不同意", nodes.get(1).getTasks().get(0).getReason());
        assertNull(respVO.getTodoTask());
    }

    @Test
    public void testGetApprovalDetail_runningCallActivity() {
        // 准备参数：主流程停留在子流程节点
        deploy("bpmn/lifecycle/child.bpmn20.xml");
        deploy("bpmn/lifecycle/child-parent.bpmn20.xml");
        String processInstanceId = processInstanceUpdateService.createProcessInstance(1L, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey("childParent"));
        ProcessInstance child = runtimeService.createProcessInstanceQuery()
                .superProcessInstanceId(processInstanceId).singleResult();

        // 调用
        BpmApprovalDetailRespVO respVO = processInstanceQueryService.getApprovalDetail(1L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId));
        // 断言：进行中的子流程节点关联子流程实例（CallActivity 没有自身的任务）
        // 注意：BPMN 设计器的预测（BpmnModelUtils#simulateProcess）不会越过 CallActivity，因此不断言后续预测节点
        List<ActivityNode> nodes = respVO.getActivityNodes();
        assertEquals(List.of(START_USER_NODE_ID, "child"), convertList(nodes.subList(0, 2), ActivityNode::getId));
        ActivityNode childNode = nodes.get(1);
        assertEquals(BpmSimpleModelNodeTypeEnum.CHILD_PROCESS.getType(), childNode.getNodeType());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), childNode.getStatus());
        assertEquals(child.getId(), childNode.getProcessInstanceId());
        assertTrue(childNode.getTasks().isEmpty());
        // 断言：用户 1 在主流程中没有待办
        assertNull(respVO.getTodoTask());
    }

    @Test
    public void testGetApprovalDetail_returnTwiceThenPreviousBranch() {
        // 准备参数：A -> 排他网关 -> 金额大于 100：B -> B2；否则：C；最后汇聚到 D
        String processInstanceId = startReturnBranchProcessInstance();
        // 第一次：金额 200，经过 A、B、B2 到达 D，D 退回到 A
        approveRunningTask(processInstanceId, "A", Map.of("amount", 200));
        approveRunningTask(processInstanceId, "B", null);
        approveRunningTask(processInstanceId, "B2", null);
        returnRunningTask(processInstanceId, "D", "A");
        // 第二次：金额 50，经过 A、C 到达 D，D 再次退回到 A
        approveRunningTask(processInstanceId, "A", Map.of("amount", 50));
        approveRunningTask(processInstanceId, "C", null);
        returnRunningTask(processInstanceId, "D", "A");
        // 第三次：金额 200，A 审批通过后，B 进行中。此时，退回记录的预测节点只有 D、C，不包括 B2
        approveRunningTask(processInstanceId, "A", Map.of("amount", 200));

        // 调用
        BpmApprovalDetailRespVO respVO = processInstanceQueryService.getApprovalDetail(1L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId));
        // 断言：B 进行中；B 之后的 B2、D、结束节点都需要预测，即使 B2 在第一次执行过
        List<ActivityNode> nodes = respVO.getActivityNodes();
        assertEquals(List.of("B"), convertList(filterList(nodes,
                node -> BpmTaskStatusEnum.RUNNING.getStatus().equals(node.getStatus())), ActivityNode::getId));
        assertEquals(List.of("B2", "D", "end"), convertList(filterList(nodes,
                node -> BpmTaskStatusEnum.NOT_START.getStatus().equals(node.getStatus())), ActivityNode::getId));
    }

    @Test
    public void testGetApprovalDetail_returnThenVariableChangedAfterGateway() {
        // 准备参数：金额 200，经过 A、B、B2 到达 D，D 退回到 A；A 再次审批通过后，B 进行中
        String processInstanceId = startReturnBranchProcessInstance();
        approveRunningTask(processInstanceId, "A", Map.of("amount", 200));
        approveRunningTask(processInstanceId, "B", null);
        approveRunningTask(processInstanceId, "B2", null);
        returnRunningTask(processInstanceId, "D", "A");
        approveRunningTask(processInstanceId, "A", Map.of("amount", 200));
        // 准备参数：通过网关后，金额修改为 50（例如说，修改表单）。从头预测会走 C 分支
        runtimeService.setVariable(processInstanceId, "amount", 50);

        // 调用
        BpmApprovalDetailRespVO respVO = processInstanceQueryService.getApprovalDetail(1L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId));
        // 断言：从进行中的 B 开始预测：B2、D、结束节点，不展示没有走的 C 分支
        List<ActivityNode> nodes = respVO.getActivityNodes();
        assertEquals(List.of("B"), convertList(filterList(nodes,
                node -> BpmTaskStatusEnum.RUNNING.getStatus().equals(node.getStatus())), ActivityNode::getId));
        assertEquals(List.of("B2", "D", "end"), convertList(filterList(nodes,
                node -> BpmTaskStatusEnum.NOT_START.getStatus().equals(node.getStatus())), ActivityNode::getId));
    }

    @Test
    public void testGetApprovalDetail_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> processInstanceQueryService.getApprovalDetail(1L,
                new BpmApprovalDetailReqVO().setProcessInstanceId("not-exists")), PROCESS_INSTANCE_NOT_EXISTS);
    }

    @Test
    public void testGetApprovalDetail_definitionNotExists() {
        // 调用，并断言异常：发起前预览审批详情时，流程定义已被删除
        assertServiceException(() -> processInstanceQueryService.getApprovalDetail(1L,
                new BpmApprovalDetailReqVO().setProcessDefinitionId("not-exists")), PROCESS_DEFINITION_NOT_EXISTS);
    }

    // ========== getApprovalDetail 表单字段权限 ==========

    @Test // 发起人从“我的流程”进入，待办属于其他审批人：使用发起人节点的权限，可编辑降级为只读
    public void testGetApprovalDetail_formFieldsPermission_startUserWithoutTodo() {
        // 准备参数：发起人 1，task1 待用户 2 审批
        String processInstanceId = startFormPermissionProcessInstance();

        // 调用
        BpmApprovalDetailRespVO respVO = processInstanceQueryService.getApprovalDetail(1L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId));
        // 断言
        assertNull(respVO.getTodoTask());
        assertEquals(Map.of("a", "1", "b", "3", "c", "1"), respVO.getFormFieldsPermission());
    }

    @Test // 审批人有本流程的待办：使用待办所在节点的权限，保留可编辑
    public void testGetApprovalDetail_formFieldsPermission_todoTask() {
        // 准备参数：task1 待用户 2 审批
        String processInstanceId = startFormPermissionProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用
        BpmApprovalDetailRespVO respVO = processInstanceQueryService.getApprovalDetail(2L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId));
        // 断言
        assertEquals(task1.getId(), respVO.getTodoTask().getId());
        assertEquals(Map.of("a", "1", "b", "2", "c", "3"), respVO.getFormFieldsPermission());
    }

    @Test // 传递其它流程实例的待办 taskId：忽略该任务，使用本流程实例的待办
    public void testGetApprovalDetail_formFieldsPermission_todoTaskOfOtherProcessInstance() {
        // 准备参数：两个流程实例，task1 均待用户 2 审批
        String processInstanceId = startFormPermissionProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        Task otherTask1 = getRunningTask(startFormPermissionProcessInstance());

        // 调用
        BpmApprovalDetailRespVO respVO = processInstanceQueryService.getApprovalDetail(2L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId).setTaskId(otherTask1.getId()));
        // 断言
        assertEquals(task1.getId(), respVO.getTodoTask().getId());
        assertEquals(processInstanceId, respVO.getTodoTask().getProcessInstanceId());
        assertEquals(Map.of("a", "1", "b", "2", "c", "3"), respVO.getFormFieldsPermission());
    }

    @Test // 发起人传递其他审批人的 taskId：忽略该任务，回落发起人节点的权限
    public void testGetApprovalDetail_formFieldsPermission_taskIdOfOtherUser() {
        // 准备参数：task1 待用户 2 审批
        String processInstanceId = startFormPermissionProcessInstance();
        Task task1 = getRunningTask(processInstanceId);

        // 调用
        BpmApprovalDetailRespVO respVO = processInstanceQueryService.getApprovalDetail(1L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId).setTaskId(task1.getId()));
        // 断言
        assertNull(respVO.getTodoTask());
        assertEquals(Map.of("a", "1", "b", "3", "c", "1"), respVO.getFormFieldsPermission());
    }

    @Test // 流程已结束：已办使用办理节点的权限，发起人使用发起人节点的权限，参与者使用自己审批通过的节点的权限，均降级为只读；其他人不返回权限
    public void testGetApprovalDetail_formFieldsPermission_processEnd() {
        // 准备参数：用户 2 审批通过 task1，流程结束
        String processInstanceId = startFormPermissionProcessInstance();
        Task task1 = getRunningTask(processInstanceId);
        taskUpdateService.approveTask(2L, new BpmTaskApproveReqVO().setId(task1.getId()).setReason("同意"));

        // 调用 + 断言：用户 2 从“已办”进入
        BpmApprovalDetailRespVO doneRespVO = processInstanceQueryService.getApprovalDetail(2L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId).setTaskId(task1.getId()));
        assertEquals(BpmProcessInstanceStatusEnum.APPROVE.getStatus(), doneRespVO.getStatus());
        assertNull(doneRespVO.getTodoTask());
        assertEquals(Map.of("a", "1", "b", "1", "c", "3"), doneRespVO.getFormFieldsPermission());
        // 调用 + 断言：发起人 1 从“我的流程”进入
        BpmApprovalDetailRespVO startUserRespVO = processInstanceQueryService.getApprovalDetail(1L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId));
        assertEquals(Map.of("a", "1", "b", "3", "c", "1"), startUserRespVO.getFormFieldsPermission());
        // 调用 + 断言：用户 2 不传 taskId，既不是发起人也没有待办：使用自己审批通过的 task1 的权限
        BpmApprovalDetailRespVO otherRespVO = processInstanceQueryService.getApprovalDetail(2L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId));
        assertEquals(Map.of("a", "1", "b", "1", "c", "3"), otherRespVO.getFormFieldsPermission());
        // 调用 + 断言：没有参与的用户 3 不传 taskId，不返回权限
        assertNull(processInstanceQueryService.getApprovalDetail(3L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId)).getFormFieldsPermission());
    }

    @Test // 传递其它流程实例的已办 taskId：忽略该任务
    public void testGetApprovalDetail_formFieldsPermission_doneTaskOfOtherProcessInstance() {
        // 准备参数：两个流程实例，task1 均由用户 2 审批通过
        String processInstanceId = startFormPermissionProcessInstance();
        taskUpdateService.approveTask(2L, new BpmTaskApproveReqVO().setId(getRunningTask(processInstanceId).getId())
                .setReason("同意"));
        String otherProcessInstanceId = startFormPermissionProcessInstance();
        Task otherTask1 = getRunningTask(otherProcessInstanceId);
        taskUpdateService.approveTask(2L, new BpmTaskApproveReqVO().setId(otherTask1.getId()).setReason("同意"));

        // 调用
        BpmApprovalDetailRespVO respVO = processInstanceQueryService.getApprovalDetail(2L,
                new BpmApprovalDetailReqVO().setProcessInstanceId(processInstanceId).setTaskId(otherTask1.getId()));
        // 断言
        assertNull(respVO.getFormFieldsPermission());
    }

    @Test // 流程未发起：使用传递的发起人节点的权限，保留可编辑
    public void testGetApprovalDetail_formFieldsPermission_notStart() {
        // 准备参数
        deploy("bpmn/form-permission.bpmn20.xml");
        String processDefinitionId = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("formPermission").latestVersion().singleResult().getId();

        // 调用
        BpmApprovalDetailRespVO respVO = processInstanceQueryService.getApprovalDetail(1L, new BpmApprovalDetailReqVO()
                .setProcessDefinitionId(processDefinitionId).setActivityId(START_USER_NODE_ID));
        // 断言
        assertNull(respVO.getTodoTask());
        assertEquals(Map.of("a", "2", "b", "3", "c", "1"), respVO.getFormFieldsPermission());
    }

    // ========== getProcessInstanceBpmnModelView ==========

    @Test
    public void testGetProcessInstanceBpmnModelView_running() {
        // 准备参数：task1 已审批，task2 待审批
        String processInstanceId = startProcessInstance(1L, null);
        approveRunningTask(processInstanceId);

        // 调用
        BpmProcessInstanceBpmnModelViewRespVO respVO = processInstanceQueryService.getProcessInstanceBpmnModelView(processInstanceId);
        // 断言：进度信息
        assertEquals(Set.of("task2"), respVO.getUnfinishedTaskActivityIds());
        assertTrue(respVO.getFinishedTaskActivityIds().containsAll(Set.of("start", "task1")));
        assertFalse(respVO.getFinishedTaskActivityIds().contains("task2"));
        assertEquals(Set.of("flow-start", "flow-task1"), respVO.getFinishedSequenceFlowActivityIds());
        assertTrue(respVO.getRejectedTaskActivityIds().isEmpty());
        // 断言：基础信息
        assertEquals(BpmProcessInstanceStatusEnum.RUNNING.getStatus(), respVO.getProcessInstance().getStatus());
        assertEquals("用户1", respVO.getProcessInstance().getStartUser().getNickname());
        assertEquals(List.of(BpmTaskStatusEnum.APPROVE.getStatus(), BpmTaskStatusEnum.RUNNING.getStatus()),
                convertList(respVO.getTasks(), task -> task.getStatus()));
        assertTrue(respVO.getBpmnXml().contains("twoStepApprove"));
        assertNull(respVO.getSimpleModel());
    }

    @Test
    public void testGetProcessInstanceBpmnModelView_rejected() {
        // 准备参数：task1 已审批，task2 审批不通过（驳回到 task1），task1 再次审批不通过
        String processInstanceId = startProcessInstance(1L, null);
        approveRunningTask(processInstanceId);
        Task task = getRunningTask(processInstanceId);
        taskUpdateService.rejectTask(1L, new BpmTaskRejectReqVO().setId(task.getId()).setReason("驳回"));
        task = getRunningTask(processInstanceId);
        assertEquals("task1", task.getTaskDefinitionKey());
        taskUpdateService.rejectTask(1L, new BpmTaskRejectReqVO().setId(task.getId()).setReason("不同意"));

        // 调用
        BpmProcessInstanceBpmnModelViewRespVO respVO = processInstanceQueryService.getProcessInstanceBpmnModelView(processInstanceId);
        // 断言：只取最后一个不通过的节点
        assertEquals(BpmProcessInstanceStatusEnum.REJECT.getStatus(), respVO.getProcessInstance().getStatus());
        assertEquals(Set.of("task1"), respVO.getRejectedTaskActivityIds());
        assertFalse(respVO.getFinishedTaskActivityIds().contains("task1"));
        assertTrue(respVO.getUnfinishedTaskActivityIds().isEmpty());
    }

    @Test
    public void testGetProcessInstanceBpmnModelView_notExists() {
        assertNull(processInstanceQueryService.getProcessInstanceBpmnModelView("not-exists"));
    }

    // ========== getProcessInstancePage ==========

    @Test
    public void testGetProcessInstancePage() {
        // 准备参数：用户 1 发起并审批通过；用户 2 发起且审批中，表单 day = 3
        String approvedId = startProcessInstance(1L, null);
        approveRunningTask(approvedId);
        approveRunningTask(approvedId);
        String runningId = startProcessInstance(2L, new HashMap<>(Map.of("day", 3)));
        LocalDateTime now = LocalDateTime.now();

        // 调用 + 断言：【我的流程】按发起人过滤
        assertPageIds(List.of(approvedId), processInstanceQueryService.getProcessInstancePage(1L, new BpmProcessInstancePageReqVO()));
        // 调用 + 断言：【管理流程】按 startUserId 过滤
        assertPageIds(List.of(runningId), processInstanceQueryService.getProcessInstancePage(null,
                (BpmProcessInstancePageReqVO) new BpmProcessInstancePageReqVO().setStartUserId(2L)));
        // 调用 + 断言：名称、流程标识、发起时间，按发起时间倒序
        BpmProcessInstancePageReqVO reqVO = new BpmProcessInstancePageReqVO();
        reqVO.setName("两级").setProcessDefinitionKey("twoStepApprove")
                .setCreateTime(new LocalDateTime[]{now.minusHours(1), now.plusHours(1)});
        assertEquals(Set.of(approvedId, runningId),
                new HashSet<>(convertList(processInstanceQueryService.getProcessInstancePage(null, reqVO).getList(),
                        HistoricProcessInstance::getId)));
        // 调用 + 断言：状态、结束时间
        reqVO = new BpmProcessInstancePageReqVO();
        reqVO.setStatus(BpmProcessInstanceStatusEnum.APPROVE.getStatus())
                .setEndTime(new LocalDateTime[]{now.minusHours(1), now.plusHours(1)});
        assertPageIds(List.of(approvedId), processInstanceQueryService.getProcessInstancePage(null, reqVO));
        // 调用 + 断言：表单字段，空值的字段被忽略
        reqVO = new BpmProcessInstancePageReqVO();
        reqVO.setFormFieldsParams("{\"day\": 3, \"reason\": \"\"}");
        assertPageIds(List.of(runningId), processInstanceQueryService.getProcessInstancePage(null, reqVO));
        // 调用 + 断言：分类不匹配时返回空
        reqVO = new BpmProcessInstancePageReqVO();
        reqVO.setCategory("not-exists");
        PageResult<HistoricProcessInstance> pageResult = processInstanceQueryService.getProcessInstancePage(null, reqVO);
        assertEquals(0, pageResult.getTotal());
        assertTrue(pageResult.getList().isEmpty());
    }

    @Test // 表单字段：数字范围、日期区间、模糊查询，以及原有的全等匹配。表单变量通过 JSON 反序列化，与前端发起流程时一致
    public void testGetProcessInstancePage_formFieldsParams() {
        // 准备参数：amount 混合 integer、long、double 类型；applyDate 混合 date、datetime（含毫秒）格式；day 为其它数字字段
        String id1 = startProcessInstance(1L, parseVariables("{\"amount\": 0, \"applyDate\": \"2026-01-01\", \"reason\": \"差旅 100%\"}"));
        String id2 = startProcessInstance(1L, parseVariables("{\"amount\": 10, \"applyDate\": \"2026-01-15 09:30:00\", \"reason\": \"差旅_北京\"}"));
        String id3 = startProcessInstance(1L, parseVariables("{\"amount\": 10.5, \"applyDate\": \"2026-01-31 23:59:59.999\", \"reason\": \"采购\"}"));
        String id4 = startProcessInstance(1L, parseVariables("{\"amount\": 3000000000, \"day\": 5, \"applyDate\": \"2026-02-01 00:00:00\", \"reason\": \"差旅\"}"));
        String id5 = startProcessInstance(1L, parseVariables("{\"amount\": 100.0, \"day\": 5, \"applyDate\": \"2025-12-31 23:59:59\", \"reason\": \"其它\"}"));
        String id6 = startProcessInstance(1L, parseVariables("{\"day\": 5}"));
        // 断言：Flowable 存储的变量类型
        assertEquals(List.of("integer", "integer", "double", "long", "double"), convertList(List.of(id1, id2, id3, id4, id5),
                id -> historyService.createHistoricVariableInstanceQuery().processInstanceId(id).variableName("amount")
                        .singleResult().getVariableTypeName()));

        // 调用 + 断言：数字范围，两端包含（包括 0），整数、小数、Long 均匹配；不匹配其它字段（day）
        assertEquals(Set.of(id1, id2, id3), getPageIds("{\"amount\": {\"op\": \"numberRange\", \"value\": [0, 10.5]}}"));
        assertEquals(Set.of(id3, id4, id5), getPageIds("{\"amount\": {\"op\": \"numberRange\", \"value\": [10.2, null]}}"));
        assertEquals(Set.of(id1), getPageIds("{\"amount\": {\"op\": \"numberRange\", \"value\": [null, 0]}}"));
        PageResult<HistoricProcessInstance> pageResult = getPage("{\"amount\": {\"op\": \"numberRange\", \"value\": [0.5, 9.9]}}", 1, 10);
        assertEquals(0, pageResult.getTotal());
        assertTrue(pageResult.getList().isEmpty());
        // 调用 + 断言：日期区间，包含开始日期的 00:00:00、结束日期的 23:59:59.999，不包含结束日期下一天的 00:00:00
        assertEquals(Set.of(id1, id2, id3), getPageIds("{\"applyDate\": {\"op\": \"dateRange\", \"value\": [\"2026-01-01\", \"2026-01-31\"]}}"));
        assertEquals(Set.of(id4), getPageIds("{\"applyDate\": {\"op\": \"dateRange\", \"value\": [\"2026-02-01\", null]}}"));
        // 调用 + 断言：模糊匹配，_ 作为 LIKE 通配符匹配任意一个字符
        assertEquals(Set.of(id1, id2, id4), getPageIds("{\"reason\": {\"op\": \"like\", \"value\": \"差旅\"}}"));
        assertEquals(Set.of(id1, id2), getPageIds("{\"reason\": {\"op\": \"like\", \"value\": \"差旅_\"}}"));
        // 调用 + 断言：组合条件，并分页：总数一致，跨页不重复，超出范围时为空页
        String combined = "{\"amount\": {\"op\": \"numberRange\", \"value\": [0, 100]},"
                + " \"applyDate\": {\"op\": \"dateRange\", \"value\": [\"2026-01-01\", \"2026-01-31\"]},"
                + " \"reason\": {\"op\": \"like\", \"value\": \"差旅\"}}";
        PageResult<HistoricProcessInstance> page1 = getPage(combined, 1, 1);
        PageResult<HistoricProcessInstance> page2 = getPage(combined, 2, 1);
        PageResult<HistoricProcessInstance> page3 = getPage(combined, 3, 1);
        assertEquals(List.of(2L, 2L, 2L), List.of(page1.getTotal(), page2.getTotal(), page3.getTotal()));
        assertEquals(1, page1.getList().size());
        assertEquals(1, page2.getList().size());
        assertEquals(Set.of(id1, id2), Set.of(page1.getList().get(0).getId(), page2.getList().get(0).getId()));
        assertTrue(page3.getList().isEmpty());
        // 调用 + 断言：原有的标量全等匹配
        assertEquals(Set.of(id3), getPageIds("{\"reason\": \"采购\", \"amount\": 10.5}"));
        assertEquals(Set.of(id2), getPageIds("{\"amount\": 10}"));
        // 调用 + 断言：空条件，不进行筛选
        assertEquals(Set.of(id1, id2, id3, id4, id5, id6), getPageIds("{\"amount\": {\"op\": \"numberRange\", \"value\": [null, null]},"
                + " \"applyDate\": {\"op\": \"dateRange\", \"value\": null}, \"reason\": {\"op\": \"like\", \"value\": \"\"}, \"day\": null}"));
        // 调用 + 断言：不支持的查询方式、日期格式
        assertEquals(BAD_REQUEST.getCode(), assertThrows(ServiceException.class,
                () -> getPage("{\"amount\": {\"op\": \"gt\", \"value\": 1}}", 1, 10)).getCode());
        assertEquals(BAD_REQUEST.getCode(), assertThrows(ServiceException.class,
                () -> getPage("{\"applyDate\": {\"op\": \"dateRange\", \"value\": [\"2026-01-01 00:00:00\", null]}}", 1, 10)).getCode());
    }

    // ========== getNextApprovalNodes ==========

    @Test
    public void testGetNextApprovalNodes() {
        // 准备参数
        String processInstanceId = startProcessInstance(1L, null);
        Task task1 = getRunningTask(processInstanceId);

        // 调用 + 断言：task1 的下一个节点为 task2，并预测审批人
        List<ActivityNode> nodes = processInstanceQueryService.getNextApprovalNodes(1L,
                new BpmApprovalDetailReqVO().setTaskId(task1.getId()));
        assertEquals(1, nodes.size());
        assertEquals("task2", nodes.get(0).getId());
        assertEquals(BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType(), nodes.get(0).getNodeType());
        assertEquals(BpmTaskStatusEnum.RUNNING.getStatus(), nodes.get(0).getStatus());
        assertEquals(List.of(1L), nodes.get(0).getCandidateUserIds());
        assertEquals("用户1", nodes.get(0).getCandidateUsers().get(0).getNickname());

        // 调用 + 断言：task2 的下一个节点为结束节点，无需审批
        approveRunningTask(processInstanceId);
        Task task2 = getRunningTask(processInstanceId);
        assertTrue(processInstanceQueryService.getNextApprovalNodes(1L,
                new BpmApprovalDetailReqVO().setTaskId(task2.getId())).isEmpty());
    }

    // ========== 私有方法 ==========

    private void deploy(String resource) {
        Deployment deployment = repositoryService.createDeployment().addClasspathResource(resource).deploy();
        deploymentIds.add(deployment.getId());
    }

    private String startProcessInstance(Long userId, Map<String, Object> variables) {
        return processInstanceUpdateService.createProcessInstance(userId, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey("twoStepApprove").setVariables(variables));
    }

    /**
     * 发起表单字段权限流程：发起人 1 的发起人节点自动通过，task1 待用户 2 审批
     */
    private String startFormPermissionProcessInstance() {
        if (repositoryService.createProcessDefinitionQuery().processDefinitionKey("formPermission").count() == 0) {
            deploy("bpmn/form-permission.bpmn20.xml");
        }
        doAnswer(invocation -> {
            DelegateExecution execution = invocation.getArgument(0);
            return START_USER_NODE_ID.equals(execution.getCurrentActivityId()) ? Set.of(1L) : Set.of(2L);
        }).when(taskCandidateInvoker).calculateUsersByTask(any());
        return processInstanceUpdateService.createProcessInstance(1L, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey("formPermission"));
    }

    private Task getRunningTask(String processInstanceId) {
        List<Task> tasks = flowableTaskService.createTaskQuery().processInstanceId(processInstanceId).list();
        assertEquals(1, tasks.size());
        return tasks.get(0);
    }

    private void approveRunningTask(String processInstanceId) {
        Task task = getRunningTask(processInstanceId);
        taskUpdateService.approveTask(1L, new BpmTaskApproveReqVO().setId(task.getId()).setReason("同意"));
    }

    /**
     * 发起退回分支流程：A -> 排他网关 -> 金额大于 100：B -> B2；否则：C；最后汇聚到 D。发起时金额为 200
     */
    private String startReturnBranchProcessInstance() {
        String bpmnXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:flowable=\"http://flowable.org/bpmn\""
                + " targetNamespace=\"http://www.yudao.iocoder.cn/bpm\">"
                + "<process id=\"returnBranch\" name=\"退回分支\" isExecutable=\"true\">"
                + "<startEvent id=\"start\"/>"
                + "<sequenceFlow id=\"flow-start\" sourceRef=\"start\" targetRef=\"A\"/>"
                + buildUserTaskXml("A") + "<sequenceFlow id=\"flow-a\" sourceRef=\"A\" targetRef=\"gateway\"/>"
                + "<exclusiveGateway id=\"gateway\" default=\"flow-c\"/>"
                + "<sequenceFlow id=\"flow-b\" sourceRef=\"gateway\" targetRef=\"B\">"
                + "<conditionExpression xsi:type=\"tFormalExpression\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<![CDATA[${amount > 100}]]></conditionExpression></sequenceFlow>"
                + "<sequenceFlow id=\"flow-c\" sourceRef=\"gateway\" targetRef=\"C\"/>"
                + buildUserTaskXml("B") + "<sequenceFlow id=\"flow-b-b2\" sourceRef=\"B\" targetRef=\"B2\"/>"
                + buildUserTaskXml("B2") + "<sequenceFlow id=\"flow-b2-d\" sourceRef=\"B2\" targetRef=\"D\"/>"
                + buildUserTaskXml("C") + "<sequenceFlow id=\"flow-c-d\" sourceRef=\"C\" targetRef=\"D\"/>"
                + buildUserTaskXml("D") + "<sequenceFlow id=\"flow-d\" sourceRef=\"D\" targetRef=\"end\"/>"
                + "<endEvent id=\"end\"/>"
                + "</process></definitions>";
        deploymentIds.add(repositoryService.createDeployment().addString("return-branch.bpmn20.xml", bpmnXml)
                .deploy().getId());
        return processInstanceUpdateService.createProcessInstance(1L, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey("returnBranch").setVariables(new HashMap<>(Map.of("amount", 200))));
    }

    private void approveRunningTask(String processInstanceId, String taskDefinitionKey, Map<String, Object> variables) {
        Task task = getRunningTask(processInstanceId);
        assertEquals(taskDefinitionKey, task.getTaskDefinitionKey());
        taskUpdateService.approveTask(1L, new BpmTaskApproveReqVO().setId(task.getId()).setReason("同意").setVariables(variables));
    }

    private void returnRunningTask(String processInstanceId, String taskDefinitionKey, String targetTaskDefinitionKey) {
        Task task = getRunningTask(processInstanceId);
        assertEquals(taskDefinitionKey, task.getTaskDefinitionKey());
        taskUpdateService.returnTask(1L, new BpmTaskReturnReqVO().setId(task.getId())
                .setTargetTaskDefinitionKey(targetTaskDefinitionKey).setReason("退回"));
    }

    private static String buildUserTaskXml(String id) {
        return "<userTask id=\"" + id + "\" name=\"审批 " + id + "\"><extensionElements>"
                + "<flowable:candidateStrategy>30</flowable:candidateStrategy></extensionElements></userTask>";
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseVariables(String json) {
        return JsonUtils.parseObject(json, Map.class);
    }

    private PageResult<HistoricProcessInstance> getPage(String formFieldsParams, int pageNo, int pageSize) {
        BpmProcessInstancePageReqVO reqVO = new BpmProcessInstancePageReqVO();
        reqVO.setProcessDefinitionKey("twoStepApprove").setFormFieldsParams(formFieldsParams);
        reqVO.setPageNo(pageNo);
        reqVO.setPageSize(pageSize);
        return processInstanceQueryService.getProcessInstancePage(null, reqVO);
    }

    /**
     * 查询第一页的流程实例编号，并断言总数与列表一致
     */
    private Set<String> getPageIds(String formFieldsParams) {
        PageResult<HistoricProcessInstance> pageResult = getPage(formFieldsParams, 1, 10);
        assertEquals(pageResult.getList().size(), pageResult.getTotal());
        return new HashSet<>(convertList(pageResult.getList(), HistoricProcessInstance::getId));
    }

    private static void assertPageIds(List<String> expected, PageResult<HistoricProcessInstance> pageResult) {
        assertEquals(expected.size(), pageResult.getTotal());
        assertEquals(expected, convertList(pageResult.getList(), HistoricProcessInstance::getId));
    }

}
