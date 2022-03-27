package cn.iocoder.yudao.module.bpm;

import org.flowable.engine.history.HistoricDetail;
import org.flowable.engine.history.HistoricFormProperty;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricVariableUpdate;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.task.api.Task;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author henryyan
 * testMultiInstanceForUserTask 会签
 * cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.BpmUserTaskActivityBehavior#handleAssignments(org.flowable.task.service.TaskService, java.lang.String, java.lang.String, java.util.List, java.util.List, org.flowable.task.service.impl.persistence.entity.TaskEntity, org.flowable.common.engine.impl.el.ExpressionManager, org.flowable.engine.delegate.DelegateExecution, org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl)
 * 执行了两次,任务分配到了同一个人
 */
public class MultiInstancesTest extends AbstractOATest {

    /**
     * Java Service多实例（是否顺序结果一样）
     */
    @Test
    @Deployment(resources = {"diagrams/chapter9/testMultiInstanceFixedNumbers.bpmn"})
    public void testParallel() throws Exception {
        Map<String, Object> variables = new HashMap<String, Object>();
        long loop = 3;
        variables.put("loop", loop);
        variables.put("counter", 0); // 计数器
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testMultiInstanceFixedNumbers", variables);
        Object variable = runtimeService.getVariable(processInstance.getId(), "counter");
        assertEquals(loop, variable);
    }

    /**
     * 用户任务多实例--顺序
     */
    @Test
    @Deployment(resources = {"diagrams/chapter9/testMultiInstanceForUserTask.sequential.bpmn"})
    public void testForUserSequence() throws Exception {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testMultiInstanceForUserTask");
        long count = taskService.createTaskQuery().processInstanceId(processInstance.getId()).count();
        assertEquals(1, count);

        Task task = taskService.createTaskQuery().singleResult();
        taskService.complete(task.getId());
        count = taskService.createTaskQuery().processInstanceId(processInstance.getId()).count();
        assertEquals(1, count);

        task = taskService.createTaskQuery().singleResult();
        taskService.complete(task.getId());
        count = taskService.createTaskQuery().processInstanceId(processInstance.getId()).count();
        assertEquals(1, count);

        task = taskService.createTaskQuery().singleResult();
        taskService.complete(task.getId());
        count = taskService.createTaskQuery().processInstanceId(processInstance.getId()).count();
        assertEquals(0, count);
    }

    /**
     * 用户任务多实例--并行
     */
    @Test
    @Deployment(resources = {"diagrams/chapter9/testMultiInstanceForUserTask.nosequential.bpmn"})
    public void testForUserNoSequential() throws Exception {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testMultiInstanceForUserTask");
        long count = taskService.createTaskQuery().processInstanceId(processInstance.getId()).count();
        assertEquals(3, count);
    }

    /**
     * 用户任务多实例，通过用户数量决定实例个数--并行
     */
    @Test
    @Deployment(resources = {"diagrams/chapter9/testMultiInstanceForUserTask.users.nosequential.bpmn"})
    public void testForUserCreateByUsersNoSequential() throws Exception {
        Map<String, Object> variables = new HashMap<String, Object>();
        List<String> users = Arrays.asList("user1", "user2", "user3");
        variables.put("users", users);
        runtimeService.startProcessInstanceByKey("testMultiInstanceForUserTask", variables);
        for (String userId : users) {
            assertEquals(1, taskService.createTaskQuery().taskAssignee(userId).count());
        }
    }

    /**
     * 用户任务多实例，通过用户数量决定实例个数--顺序
     */
    @Test
    @Deployment(resources = {"diagrams/chapter9/testMultiInstanceForUserTask.users.sequential.bpmn"})
    public void testForUserCreateByUsersSequential() throws Exception {
        Map<String, Object> variables = new HashMap<String, Object>();
        List<String> users = Arrays.asList("user1", "user2", "user3");
        variables.put("users", users);
        runtimeService.startProcessInstanceByKey("testMultiInstanceForUserTask", variables);
        for (String userId : users) {
            Task task = taskService.createTaskQuery().taskAssignee(userId).singleResult();
            taskService.complete(task.getId());
        }
    }

    /**
     * 用户任务多实例，按照任务完成的百分比比率决定是否提前结束流程
     */
    @Test
    @Deployment(resources = {"diagrams/chapter9/testMultiInstanceForUserTask.users.sequential.with.complete.conditon.bpmn"})
    public void testForUserCreateByUsersSequentialWithCompleteCondition() throws Exception {
        Map<String, Object> variables = new HashMap<String, Object>();
        List<String> users = Arrays.asList("user1", "user2", "user3");
        variables.put("users", users);
        variables.put("rate", 0.6d);
        runtimeService.startProcessInstanceByKey("testMultiInstanceForUserTask", variables);

        Task task = taskService.createTaskQuery().taskAssignee("user1").singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().taskAssignee("user2").singleResult();
        taskService.complete(task.getId());

        long count = historyService.createHistoricProcessInstanceQuery().finished().count();
        assertEquals(1, count);

    }

    /**
     * 用户任务多实例，按照任务完成的百分比比率决定是否提前结束流程
     */
    @Test
    @Deployment(resources = {"diagrams/chapter9/testMultiInstanceForUserTask.exception.bpmn"})
    public void testForUserCreateByUsersException() throws Exception {
        Map<String, Object> variables = new HashMap<String, Object>();
        List<String> users = Arrays.asList("user1", "user2", "user3");
        variables.put("users", users);
        runtimeService.startProcessInstanceByKey("testMultiInstanceForUserTask", variables);

        Task task = taskService.createTaskQuery().taskAssignee("user1").singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().taskAssignee("user2").singleResult();
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().taskAssignee("user3").singleResult();
        taskService.complete(task.getId());

        List<Task> list = taskService.createTaskQuery().list();
        for (Task task2 : list) {
            System.out.println("============" + task2.getName());
        }

    }
    /////////////////////////////////////////////////
    /**
     * 全部通过
     */
    @Test
    @Deployment(resources = {"diagrams/chapter9/leave-countersign.bpmn"})
    public void testAllApproved() throws Exception {
        Map<String, Object> variables = new HashMap<String, Object>();
        List<String> users = Arrays.asList("groupLeader", "deptLeader", "hr");
        variables.put("users", users);
        identityService.setAuthenticatedUserId("henryyan");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leave-countersign", variables);
        for (String user : users) {
            Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskAssignee(user).singleResult();
            Map<String, Object> taskVariables = new HashMap<String, Object>();
            taskVariables.put("approved", "true");
            taskService.complete(task.getId(), taskVariables);
        }

        Task task = taskService.createTaskQuery().taskAssignee("henryyan").singleResult();
        assertNotNull(task);
        assertEquals("销假", task.getName());
    }

    /**
     * 部分通过
     */
    @Test
    @Deployment(resources = {"diagrams/chapter9/leave-countersign.bpmn"})
    public void testNotAllApproved() throws Exception {
        Map<String, Object> variables = new HashMap<String, Object>();
        List<String> users = Arrays.asList("groupLeader", "deptLeader", "hr");
        variables.put("users", users);
        identityService.setAuthenticatedUserId("henryyan");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leave-countersign", variables);
        for (String user : users) {
            Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskAssignee(user).singleResult();
            Map<String, Object> taskVariables = new HashMap<String, Object>();
            taskVariables.put("approved", "false");
            taskService.complete(task.getId(), taskVariables);
        }

        Task task = taskService.createTaskQuery().taskAssignee("henryyan").singleResult();
        assertNotNull(task);
        assertEquals("调整申请", task.getName());
    }
}
