package cn.iocoder.yudao.module.bpm.framework.flowable;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO.ActivityNode;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceBpmnModelViewRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstancePageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRejectReqVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModelNodeTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskServiceImpl;
import jakarta.annotation.Resource;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.PROCESS_DEFINITION_NOT_EXISTS;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.PROCESS_INSTANCE_NOT_EXISTS;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.START_USER_NODE_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link BpmProcessInstanceServiceImpl} 查询相关方法的 Flowable 集成测试
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
    private TaskService flowableTaskService;

    @Resource
    private BpmTaskServiceImpl taskService;
    @Resource
    private BpmProcessInstanceServiceImpl processInstanceService;

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
        BpmApprovalDetailRespVO respVO = processInstanceService.getApprovalDetail(1L,
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
        BpmApprovalDetailRespVO respVO = processInstanceService.getApprovalDetail(1L,
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
        taskService.rejectTask(1L, new BpmTaskRejectReqVO().setId(task1.getId()).setReason("不同意"));

        // 调用
        BpmApprovalDetailRespVO respVO = processInstanceService.getApprovalDetail(1L,
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
        String processInstanceId = processInstanceService.createProcessInstance(1L, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey("childParent"));
        ProcessInstance child = runtimeService.createProcessInstanceQuery()
                .superProcessInstanceId(processInstanceId).singleResult();

        // 调用
        BpmApprovalDetailRespVO respVO = processInstanceService.getApprovalDetail(1L,
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
    public void testGetApprovalDetail_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> processInstanceService.getApprovalDetail(1L,
                new BpmApprovalDetailReqVO().setProcessInstanceId("not-exists")), PROCESS_INSTANCE_NOT_EXISTS);
    }

    @Test
    public void testGetApprovalDetail_definitionNotExists() {
        // 调用，并断言异常：发起前预览审批详情时，流程定义已被删除
        assertServiceException(() -> processInstanceService.getApprovalDetail(1L,
                new BpmApprovalDetailReqVO().setProcessDefinitionId("not-exists")), PROCESS_DEFINITION_NOT_EXISTS);
    }

    // ========== getProcessInstanceBpmnModelView ==========

    @Test
    public void testGetProcessInstanceBpmnModelView_running() {
        // 准备参数：task1 已审批，task2 待审批
        String processInstanceId = startProcessInstance(1L, null);
        approveRunningTask(processInstanceId);

        // 调用
        BpmProcessInstanceBpmnModelViewRespVO respVO = processInstanceService.getProcessInstanceBpmnModelView(processInstanceId);
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
        taskService.rejectTask(1L, new BpmTaskRejectReqVO().setId(task.getId()).setReason("驳回"));
        task = getRunningTask(processInstanceId);
        assertEquals("task1", task.getTaskDefinitionKey());
        taskService.rejectTask(1L, new BpmTaskRejectReqVO().setId(task.getId()).setReason("不同意"));

        // 调用
        BpmProcessInstanceBpmnModelViewRespVO respVO = processInstanceService.getProcessInstanceBpmnModelView(processInstanceId);
        // 断言：只取最后一个不通过的节点
        assertEquals(BpmProcessInstanceStatusEnum.REJECT.getStatus(), respVO.getProcessInstance().getStatus());
        assertEquals(Set.of("task1"), respVO.getRejectedTaskActivityIds());
        assertFalse(respVO.getFinishedTaskActivityIds().contains("task1"));
        assertTrue(respVO.getUnfinishedTaskActivityIds().isEmpty());
    }

    @Test
    public void testGetProcessInstanceBpmnModelView_notExists() {
        assertNull(processInstanceService.getProcessInstanceBpmnModelView("not-exists"));
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
        assertPageIds(List.of(approvedId), processInstanceService.getProcessInstancePage(1L, new BpmProcessInstancePageReqVO()));
        // 调用 + 断言：【管理流程】按 startUserId 过滤
        assertPageIds(List.of(runningId), processInstanceService.getProcessInstancePage(null,
                (BpmProcessInstancePageReqVO) new BpmProcessInstancePageReqVO().setStartUserId(2L)));
        // 调用 + 断言：名称、流程标识、发起时间，按发起时间倒序
        BpmProcessInstancePageReqVO reqVO = new BpmProcessInstancePageReqVO();
        reqVO.setName("两级").setProcessDefinitionKey("twoStepApprove")
                .setCreateTime(new LocalDateTime[]{now.minusHours(1), now.plusHours(1)});
        assertEquals(Set.of(approvedId, runningId),
                new HashSet<>(convertList(processInstanceService.getProcessInstancePage(null, reqVO).getList(),
                        HistoricProcessInstance::getId)));
        // 调用 + 断言：状态、结束时间
        reqVO = new BpmProcessInstancePageReqVO();
        reqVO.setStatus(BpmProcessInstanceStatusEnum.APPROVE.getStatus())
                .setEndTime(new LocalDateTime[]{now.minusHours(1), now.plusHours(1)});
        assertPageIds(List.of(approvedId), processInstanceService.getProcessInstancePage(null, reqVO));
        // 调用 + 断言：表单字段，空值的字段被忽略
        reqVO = new BpmProcessInstancePageReqVO();
        reqVO.setFormFieldsParams("{\"day\": 3, \"reason\": \"\"}");
        assertPageIds(List.of(runningId), processInstanceService.getProcessInstancePage(null, reqVO));
        // 调用 + 断言：分类不匹配时返回空
        reqVO = new BpmProcessInstancePageReqVO();
        reqVO.setCategory("not-exists");
        PageResult<HistoricProcessInstance> pageResult = processInstanceService.getProcessInstancePage(null, reqVO);
        assertEquals(0, pageResult.getTotal());
        assertTrue(pageResult.getList().isEmpty());
    }

    // ========== getNextApprovalNodes ==========

    @Test
    public void testGetNextApprovalNodes() {
        // 准备参数
        String processInstanceId = startProcessInstance(1L, null);
        Task task1 = getRunningTask(processInstanceId);

        // 调用 + 断言：task1 的下一个节点为 task2，并预测审批人
        List<ActivityNode> nodes = processInstanceService.getNextApprovalNodes(1L,
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
        assertTrue(processInstanceService.getNextApprovalNodes(1L,
                new BpmApprovalDetailReqVO().setTaskId(task2.getId())).isEmpty());
    }

    // ========== 私有方法 ==========

    private void deploy(String resource) {
        Deployment deployment = repositoryService.createDeployment().addClasspathResource(resource).deploy();
        deploymentIds.add(deployment.getId());
    }

    private String startProcessInstance(Long userId, Map<String, Object> variables) {
        return processInstanceService.createProcessInstance(userId, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey("twoStepApprove").setVariables(variables));
    }

    private Task getRunningTask(String processInstanceId) {
        List<Task> tasks = flowableTaskService.createTaskQuery().processInstanceId(processInstanceId).list();
        assertEquals(1, tasks.size());
        return tasks.get(0);
    }

    private void approveRunningTask(String processInstanceId) {
        Task task = getRunningTask(processInstanceId);
        taskService.approveTask(1L, new BpmTaskApproveReqVO().setId(task.getId()).setReason("同意"));
    }

    private static void assertPageIds(List<String> expected, PageResult<HistoricProcessInstance> pageResult) {
        assertEquals(expected.size(), pageResult.getTotal());
        assertEquals(expected, convertList(pageResult.getList(), HistoricProcessInstance::getId));
    }

}
