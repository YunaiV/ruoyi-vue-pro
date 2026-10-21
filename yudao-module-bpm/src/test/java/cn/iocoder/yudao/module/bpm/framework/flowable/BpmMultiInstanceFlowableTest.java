package cn.iocoder.yudao.module.bpm.framework.flowable;

import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRejectReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmChildProcessMultiInstanceSourceTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmChildProcessStartUserEmptyTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmChildProcessStartUserTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModelNodeTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskApproveMethodEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskApproveTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmReasonEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.BpmParallelMultiInstanceBehavior;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.BpmSequentialMultiInstanceBehavior;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.SimpleModelUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskServiceImpl;
import jakarta.annotation.Resource;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 多实例（会签、或签、依次审批、子流程多实例）的 Flowable 集成测试
 *
 * <p>覆盖 {@link BpmParallelMultiInstanceBehavior}、{@link BpmSequentialMultiInstanceBehavior} 在真实引擎中的实例创建、
 * 审批人分配，以及 {@link BpmTaskServiceImpl} 审批后完成条件的流转。</p>
 *
 * @author HUIHUI
 */
public class BpmMultiInstanceFlowableTest extends BaseFlowableUnitTest {

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private HistoryService historyService;

    @Resource
    private BpmTaskServiceImpl taskService;
    @Resource
    private BpmProcessInstanceServiceImpl processInstanceService;

    private final List<String> deploymentIds = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        deploymentIds.add(repositoryService.createDeployment()
                .addClasspathResource("bpmn/multi-instance/user-task.bpmn20.xml")
                .addClasspathResource("bpmn/multi-instance/call-activity.bpmn20.xml")
                .deploy().getId());
        mockFlowableBackedServices(repositoryService);
        // 多人审批的审批人：用户 1、用户 2（有序）
        when(taskCandidateInvoker.calculateUsersByTask(any())).thenReturn(new LinkedHashSet<>(List.of(1L, 2L)));
    }

    @AfterEach
    public void tearDown() {
        deploymentIds.forEach(id -> repositoryService.deleteDeployment(id, true));
    }

    // ========== 多人审批 ==========

    @Test
    public void testApproveTask_any() {
        // 准备参数
        String processInstanceId = startProcessInstance("multiAny", null);
        List<Task> tasks = getRunningTasks(processInstanceId);
        assertEquals(List.of("1", "2"), convertList(tasks, Task::getAssignee));

        // 调用：用户 1 审批通过
        approveTask(tasks.get(0));
        // 断言：或签一人通过即可，用户 2 的任务被取消，流程审批通过
        assertTaskStatus(tasks.get(1).getId(), BpmTaskStatusEnum.CANCEL.getStatus(),
                BpmReasonEnum.CANCEL_BY_SYSTEM.getReason());
        assertProcessInstanceStatus(processInstanceId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
    }

    @Test
    public void testRejectTask_any() {
        // 准备参数
        String processInstanceId = startProcessInstance("multiAny", null);
        List<Task> tasks = getRunningTasks(processInstanceId);

        // 调用：用户 1 审批不通过
        taskService.rejectTask(1L, new BpmTaskRejectReqVO().setId(tasks.get(0).getId()).setReason("不同意"));
        // 断言：或签一人不通过即可，用户 2 的任务被取消，流程不通过
        assertTaskStatus(tasks.get(0).getId(), BpmTaskStatusEnum.REJECT.getStatus(), "不同意");
        assertTaskStatus(tasks.get(1).getId(), BpmTaskStatusEnum.CANCEL.getStatus(),
                BpmReasonEnum.CANCEL_BY_SYSTEM.getReason());
        assertTrue(getRunningTasks(processInstanceId).isEmpty());
        assertProcessInstanceStatus(processInstanceId, BpmProcessInstanceStatusEnum.REJECT.getStatus());
    }

    @Test
    public void testApproveTask_ratio() {
        // 准备参数
        String processInstanceId = startProcessInstance("multiRatio", null);
        List<Task> tasks = getRunningTasks(processInstanceId);
        assertEquals(List.of("1", "2"), convertList(tasks, Task::getAssignee));

        // 调用 + 断言：用户 1 审批通过，仍需等待用户 2 审批
        approveTask(tasks.get(0));
        assertEquals(List.of(tasks.get(1).getId()), convertList(getRunningTasks(processInstanceId), Task::getId));
        // 调用 + 断言：用户 2 审批通过，流程审批通过
        approveTask(tasks.get(1));
        assertProcessInstanceStatus(processInstanceId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
    }

    @Test
    public void testApproveTask_ratioPartial() {
        // 准备参数：通过比例 60%，审批人为用户 1、用户 2、用户 3
        when(taskCandidateInvoker.calculateUsersByTask(any())).thenReturn(new LinkedHashSet<>(List.of(1L, 2L, 3L)));
        String processInstanceId = startProcessInstance("multiRatioPartial", null);
        List<Task> tasks = getRunningTasks(processInstanceId);
        assertEquals(List.of("1", "2", "3"), convertList(tasks, Task::getAssignee));

        // 调用 + 断言：用户 1 审批通过（1/3 < 60%），仍需等待其他人审批
        approveTask(tasks.get(0));
        assertEquals(List.of(tasks.get(1).getId(), tasks.get(2).getId()),
                convertList(getRunningTasks(processInstanceId), Task::getId));
        // 调用 + 断言：用户 2 审批通过（2/3 >= 60%），用户 3 的任务被取消，流程审批通过
        approveTask(tasks.get(1));
        assertTaskStatus(tasks.get(2).getId(), BpmTaskStatusEnum.CANCEL.getStatus(),
                BpmReasonEnum.CANCEL_BY_SYSTEM.getReason());
        assertProcessInstanceStatus(processInstanceId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
    }

    @Test
    public void testRejectTask_ratio() {
        // 准备参数：用户 1 已审批通过
        String processInstanceId = startProcessInstance("multiRatio", null);
        List<Task> tasks = getRunningTasks(processInstanceId);
        approveTask(tasks.get(0));

        // 调用：用户 2 审批不通过
        taskService.rejectTask(2L, new BpmTaskRejectReqVO().setId(tasks.get(1).getId()).setReason("不同意"));
        // 断言：会签任一人不通过，流程不通过
        assertTaskStatus(tasks.get(0).getId(), BpmTaskStatusEnum.APPROVE.getStatus(), "同意");
        assertTaskStatus(tasks.get(1).getId(), BpmTaskStatusEnum.REJECT.getStatus(), "不同意");
        assertProcessInstanceStatus(processInstanceId, BpmProcessInstanceStatusEnum.REJECT.getStatus());
    }

    @Test
    public void testRejectTask_ratioReturn() {
        // 准备参数：发起审批节点由用户 1 审批，会签节点由用户 1、用户 2 审批
        when(taskCandidateInvoker.calculateUsersByTask(any())).thenAnswer(invocation ->
                "multiReturn-task".equals(((DelegateExecution) invocation.getArgument(0)).getCurrentActivityId())
                        ? new LinkedHashSet<>(List.of(1L, 2L)) : Set.of(1L));
        String processInstanceId = startProcessInstance("multiReturn", null);
        approveTask(getRunningTask(processInstanceId));
        List<Task> tasks = getRunningTasks(processInstanceId);
        assertEquals(List.of("1", "2"), convertList(tasks, Task::getAssignee));

        // 调用：用户 2 审批不通过，按配置退回到发起审批节点
        taskService.rejectTask(2L, new BpmTaskRejectReqVO().setId(tasks.get(1).getId()).setReason("退回修改"));
        // 断言：用户 2 的任务标记为退回，用户 1 的会签任务被取消，流程回到发起审批节点
        assertTaskStatus(tasks.get(1).getId(), BpmTaskStatusEnum.RETURN.getStatus(), "退回修改");
        assertTaskStatus(tasks.get(0).getId(), BpmTaskStatusEnum.CANCEL.getStatus(),
                BpmReasonEnum.CANCEL_BY_SYSTEM.getReason());
        Task firstTask = getRunningTask(processInstanceId);
        assertEquals("multiReturn-first", firstTask.getTaskDefinitionKey());

        // 调用 + 断言：再次审批发起审批节点后，重新生成用户 1、用户 2 的会签任务
        approveTask(firstTask);
        List<Task> newTasks = getRunningTasks(processInstanceId);
        assertEquals(List.of("1", "2"), convertList(newTasks, Task::getAssignee));
        assertEquals(List.of("multiReturn-task", "multiReturn-task"), convertList(newTasks, Task::getTaskDefinitionKey));
        // 调用 + 断言：全部审批通过后，流程审批通过
        newTasks.forEach(this::approveTask);
        assertProcessInstanceStatus(processInstanceId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
    }

    @Test
    public void testApproveTask_sequential() {
        // 准备参数
        String processInstanceId = startProcessInstance("multiSequential", null);
        Task task1 = getRunningTask(processInstanceId);
        assertEquals("1", task1.getAssignee());

        // 调用 + 断言：用户 1 审批通过后，才轮到用户 2
        approveTask(task1);
        Task task2 = getRunningTask(processInstanceId);
        assertEquals("2", task2.getAssignee());
        // 调用 + 断言：用户 2 审批通过，流程审批通过
        approveTask(task2);
        assertProcessInstanceStatus(processInstanceId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
    }

    // ========== 子流程多实例 ==========

    @Test
    public void testCallActivity_fixedQuantity() {
        // 调用
        String processInstanceId = startProcessInstance("multiChildFixedQuantity", null);
        // 断言：并行发起 2 个子流程，全部审批通过后，主流程结束
        List<ProcessInstance> children = getChildProcessInstances(processInstanceId);
        assertEquals(2, children.size());
        children.forEach(child -> approveTask(getRunningTask(child.getId())));
        assertProcessInstanceStatus(processInstanceId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
    }

    @Test
    public void testCallActivity_numberForm() {
        // 调用：表单 count = 3
        String processInstanceId = startProcessInstance("multiChildNumberForm", new HashMap<>(Map.of("count", 3)));
        // 断言：并行发起 3 个子流程，全部审批通过后，主流程结束
        List<ProcessInstance> children = getChildProcessInstances(processInstanceId);
        assertEquals(3, children.size());
        children.forEach(child -> approveTask(getRunningTask(child.getId())));
        assertProcessInstanceStatus(processInstanceId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
    }

    @Test
    public void testCallActivity_multipleFormSequential() {
        // 调用：表单 users 选择了 2 个用户
        String processInstanceId = startProcessInstance("multiChildMultipleForm",
                new HashMap<>(Map.of("users", List.of(1L, 2L))));
        // 断言：依次发起子流程，前一个子流程结束后才发起下一个
        for (int i = 0; i < 2; i++) {
            List<ProcessInstance> children = getChildProcessInstances(processInstanceId);
            assertEquals(1, children.size());
            approveTask(getRunningTask(children.get(0).getId()));
        }
        assertEquals(2, historyService.createHistoricProcessInstanceQuery()
                .superProcessInstanceId(processInstanceId).count());
        assertProcessInstanceStatus(processInstanceId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
    }

    // ========== Simple 设计器模型 ==========

    @Test
    public void testSimpleModel_ratioApproveThenChildProcess() {
        // 准备参数：由 SimpleModelUtils 生成【会签 -> 子流程（固定数量 2 个）】的流程，与 Simple 设计器保存后部署的模型一致
        BpmSimpleModelNodeVO childProcessNode = new BpmSimpleModelNodeVO().setId("child").setName("子流程")
                .setType(BpmSimpleModelNodeTypeEnum.CHILD_PROCESS.getType())
                .setChildProcessSetting(new BpmSimpleModelNodeVO.ChildProcessSetting()
                        .setCalledProcessDefinitionKey("multiChild").setCalledProcessDefinitionName("子流程")
                        .setAsync(false).setSkipStartUserNode(false)
                        .setStartUserSetting(new BpmSimpleModelNodeVO.ChildProcessSetting.StartUserSetting()
                                .setType(BpmChildProcessStartUserTypeEnum.MAIN_PROCESS_START_USER.getType())
                                .setEmptyType(BpmChildProcessStartUserEmptyTypeEnum.MAIN_PROCESS_START_USER.getType()))
                        .setMultiInstanceSetting(new BpmSimpleModelNodeVO.ChildProcessSetting.MultiInstanceSetting()
                                .setEnable(true).setSequential(false).setApproveRatio(100)
                                .setSourceType(BpmChildProcessMultiInstanceSourceTypeEnum.FIXED_QUANTITY.getType())
                                .setSource("2")))
                .setChildNode(new BpmSimpleModelNodeVO().setId("end").setType(BpmSimpleModelNodeTypeEnum.END_NODE.getType()));
        BpmSimpleModelNodeVO approveNode = new BpmSimpleModelNodeVO().setId("approve").setName("会签审批")
                .setType(BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType())
                .setApproveType(BpmUserTaskApproveTypeEnum.USER.getType())
                .setApproveMethod(BpmUserTaskApproveMethodEnum.RATIO.getMethod()).setApproveRatio(100)
                .setCandidateStrategy(BpmTaskCandidateStrategyEnum.USER.getStrategy()).setCandidateParam("1,2")
                .setChildNode(childProcessNode);
        BpmnModel bpmnModel = SimpleModelUtils.buildBpmnModel("simpleMulti", "Simple 多实例", approveNode);
        deploymentIds.add(repositoryService.createDeployment().addBpmnModel("simpleMulti.bpmn20.xml", bpmnModel)
                .deploy().getId());
        String processInstanceId = startProcessInstance("simpleMulti", null);

        // 调用 + 断言：会签需要用户 1、用户 2 都审批通过
        List<Task> tasks = getRunningTasks(processInstanceId);
        assertEquals(List.of("1", "2"), convertList(tasks, Task::getAssignee));
        tasks.forEach(this::approveTask);
        // 调用 + 断言：并行发起 2 个子流程，全部审批通过后，主流程结束
        List<ProcessInstance> children = getChildProcessInstances(processInstanceId);
        assertEquals(2, children.size());
        children.forEach(child -> approveTask(getRunningTask(child.getId())));
        assertProcessInstanceStatus(processInstanceId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
    }

    // ========== 私有方法 ==========

    private String startProcessInstance(String processDefinitionKey, Map<String, Object> variables) {
        return processInstanceService.createProcessInstance(1L, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey(processDefinitionKey).setVariables(variables));
    }

    private List<ProcessInstance> getChildProcessInstances(String processInstanceId) {
        return runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstanceId).list();
    }

    private List<Task> getRunningTasks(String processInstanceId) {
        // 按审批人排序，便于断言
        return taskService.getRunningTaskListByProcessInstanceId(processInstanceId, null, null).stream()
                .sorted(Comparator.comparing(Task::getAssignee)).toList();
    }

    private Task getRunningTask(String processInstanceId) {
        List<Task> tasks = getRunningTasks(processInstanceId);
        assertEquals(1, tasks.size());
        return tasks.get(0);
    }

    private void approveTask(Task task) {
        taskService.approveTask(Long.valueOf(task.getAssignee()), new BpmTaskApproveReqVO().setId(task.getId())
                .setReason("同意"));
    }

    private void assertTaskStatus(String taskId, Integer status, String reason) {
        HistoricTaskInstance task = taskService.getHistoricTask(taskId);
        assertEquals(status, FlowableUtils.getTaskStatus(task));
        assertEquals(reason, FlowableUtils.getTaskReason(task));
    }

    private void assertProcessInstanceStatus(String processInstanceId, Integer status) {
        HistoricProcessInstance processInstance = processInstanceService.getHistoricProcessInstance(processInstanceId);
        assertNotNull(processInstance.getEndTime());
        assertEquals(status, FlowableUtils.getProcessInstanceStatus(processInstance));
    }

}
