package cn.iocoder.yudao.module.bpm.framework.flowable;

import cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.BpmUserTaskActivityBehavior;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import jakarta.annotation.Resource;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link BpmUserTaskActivityBehavior} 的 Flowable 集成测试
 *
 * @author HUIHUI
 */
public class BpmUserTaskActivityBehaviorFlowableTest extends BaseFlowableUnitTest {

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private TaskService taskService;

    private String deploymentId;

    @BeforeEach
    public void setUp() {
        // 简单审批流程：BPMN 中的 assignee 表达式为 ${startUserId}
        deploymentId = repositoryService.createDeployment()
                .addClasspathResource("bpmn/simple-approve.bpmn20.xml")
                .deploy().getId();
    }

    @AfterEach
    public void tearDown() {
        repositoryService.deleteDeployment(deploymentId, true);
    }

    @Test
    public void testHandleAssignments_success() {
        // 准备参数：发起人为用户 1，审批人计算结果为用户 2
        when(taskCandidateInvoker.calculateUsersByTask(any())).thenReturn(Set.of(2L));

        // 调用
        String processInstanceId = startProcessInstance();
        // 断言：审批人以 BpmTaskCandidateInvoker 的计算结果为准，而不是 BPMN 中的 assignee 表达式
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        assertEquals("审批", task.getName());
        assertEquals("2", task.getAssignee());
    }

    @Test
    public void testHandleAssignments_candidateEmpty() {
        // 准备参数：计算不到审批人
        when(taskCandidateInvoker.calculateUsersByTask(any())).thenReturn(Set.of());

        // 调用
        String processInstanceId = startProcessInstance();
        // 断言：任务仍然创建，但没有审批人（后续由审批人为空的策略处理）
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        assertNotNull(task);
        assertNull(task.getAssignee());
    }

    private String startProcessInstance() {
        // 模拟 FlowableWebFilter 设置发起人，与生产发起流程的上下文一致
        ProcessInstance processInstance = FlowableUtils.executeAuthenticatedUserId(1L,
                () -> runtimeService.startProcessInstanceByKey("simpleApprove", "business-1",
                        Map.of("startUserId", 1L)));
        return processInstance.getId();
    }

}
