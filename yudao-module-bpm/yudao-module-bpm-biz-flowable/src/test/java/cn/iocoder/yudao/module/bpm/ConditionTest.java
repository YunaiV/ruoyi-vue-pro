package cn.iocoder.yudao.module.bpm;

import org.flowable.engine.history.HistoricDetail;
import org.flowable.engine.history.HistoricFormProperty;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricVariableUpdate;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.task.api.Task;
import org.joda.time.DateTime;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 条件测试
 * 1 某个节点-支付额度需要大于70%
 * 2 支付条件完成后,开始倒计时15天,要完成流程处理
 * @author cuicui
 */
public class ConditionTest extends AbstractOATest {

    @Test
    @Deployment(resources = {"chapter6/leave-timeLimit-money/leave-formkey-ext.bpmn", "chapter6/leave-timeLimit-money/leave-start.form",
            "chapter6/leave-timeLimit-money/approve-deptLeader.form", "chapter6/leave-timeLimit-money/approve-hr.form", "chapter6/leave-timeLimit-money/report-back.form",
            "chapter6/leave-timeLimit-money/modify-apply.form"})
    public void allPass() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, String> variables = new HashMap<String, String>();
        Calendar ca = Calendar.getInstance();
        String startDate = sdf.format(ca.getTime());
        ca.add(Calendar.DAY_OF_MONTH, 2); // 当前日期加2天
        String endDate = sdf.format(ca.getTime());

        // 启动流程
        variables.put("startDate", startDate);
        variables.put("endDate", endDate);
        variables.put("reason", "公休");

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

        // 读取启动表单
        Object renderedStartForm = formService.getRenderedStartForm(processDefinition.getId());
        assertNotNull(renderedStartForm);

        // 启动流程
        // 设置当前用户
        String currentUserId = "henryyan";
        identityService.setAuthenticatedUserId(currentUserId);
        ProcessInstance processInstance = formService.submitStartFormData(processDefinition.getId(), variables);
        assertNotNull(processInstance);

        // 部门领导审批通过
        Task deptLeaderTask = taskService.createTaskQuery().taskCandidateGroup("deptLeader").singleResult();
        assertNotNull(formService.getRenderedTaskForm(deptLeaderTask.getId()));
        variables = new HashMap<String, String>();
        variables.put("deptLeaderApproved", "true");
        formService.submitTaskFormData(deptLeaderTask.getId(), variables);

        // 人事审批通过
        Task hrTask = taskService.createTaskQuery().taskCandidateGroup("hr").singleResult();
        assertNotNull(formService.getRenderedTaskForm(hrTask.getId()));
        variables = new HashMap<String, String>();
        variables.put("hrApproved", "true");
        //手动设置支付金额
        variables.put("amountMoney", "19999");
        formService.submitTaskFormData(hrTask.getId(), variables);
        //判断支付金额是否>1万元
        // 财务打款通过
        Task caiwuTask = taskService.createTaskQuery().taskCandidateGroup("caiwu").singleResult();
        printTask(caiwuTask);
        taskService.complete(caiwuTask.getId());
        //判断倒计时15天
        Task chuNaTask = taskService.createTaskQuery().taskCandidateGroup("chuNa").singleResult();
        printTask(chuNaTask);
        taskService.complete(chuNaTask.getId());

        // 销假（根据申请人的用户ID读取）
//        Task reportBackTask = taskService.createTaskQuery().taskAssignee(currentUserId).singleResult();
//        assertNotNull(formService.getRenderedTaskForm(reportBackTask.getId()));
//        variables = new HashMap<String, String>();
//        variables.put("reportBackDate", sdf.format(ca.getTime()));
//        formService.submitTaskFormData(reportBackTask.getId(), variables);

        // 验证流程是否已经结束
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().finished().singleResult();
        assertNotNull(historicProcessInstance);

        // 读取历史变量
        Map<String, Object> historyVariables = packageVariables(processInstance);

        // 验证执行结果
        assertEquals("ok", historyVariables.get("result"));

    }

    /**
     * 查询过期任务
     * @throws Exception
     */
    @Test
    @Deployment(resources = {"chapter6/leave-timeLimit-money/leave-formkey-ext.bpmn", "chapter6/leave-timeLimit-money/leave-start.form",
            "chapter6/leave-timeLimit-money/approve-deptLeader.form", "chapter6/leave-timeLimit-money/approve-hr.form", "chapter6/leave-timeLimit-money/report-back.form",
            "chapter6/leave-timeLimit-money/modify-apply.form"})
    public void dueDate() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, String> variables = new HashMap<String, String>();
        Calendar ca = Calendar.getInstance();
        String startDate = sdf.format(ca.getTime());
        ca.add(Calendar.DAY_OF_MONTH, 2); // 当前日期加2天
        String endDate = sdf.format(ca.getTime());

        // 启动流程
        variables.put("startDate", startDate);
        variables.put("endDate", endDate);
        variables.put("reason", "公休");

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

        // 读取启动表单
        Object renderedStartForm = formService.getRenderedStartForm(processDefinition.getId());
        assertNotNull(renderedStartForm);

        // 启动流程
        // 设置当前用户
        String currentUserId = "henryyan";
        identityService.setAuthenticatedUserId(currentUserId);
        ProcessInstance processInstance = formService.submitStartFormData(processDefinition.getId(), variables);
        assertNotNull(processInstance);

        // 部门领导审批通过
        Task deptLeaderTask = taskService.createTaskQuery().taskCandidateGroup("deptLeader").singleResult();
        assertNotNull(formService.getRenderedTaskForm(deptLeaderTask.getId()));
        variables = new HashMap<String, String>();
        variables.put("deptLeaderApproved", "true");
        formService.submitTaskFormData(deptLeaderTask.getId(), variables);

        // 人事审批通过
        Task hrTask = taskService.createTaskQuery().taskCandidateGroup("hr").singleResult();
        assertNotNull(formService.getRenderedTaskForm(hrTask.getId()));
        variables = new HashMap<String, String>();
        variables.put("hrApproved", "true");
        //手动设置支付金额
        variables.put("amountMoney", "19999");
        formService.submitTaskFormData(hrTask.getId(), variables);
        //判断支付金额是否>1万元
        // 财务打款通过
        Task caiwuTask = taskService.createTaskQuery().taskCandidateGroup("caiwu").singleResult();
        printTask(caiwuTask);

        //设置5天前就过期了
        DateTime dateTime = DateTime.now();
        DateTime minusDays = dateTime.minusDays(5);
        taskService.setDueDate(caiwuTask.getId(),minusDays.toDate());
        //查询今天以前的过期任务
        List<Task> listTask = taskService.createTaskQuery().taskDueBefore(new Date()).list();
        for (Task task : listTask) {
            printTask(task);
        }

        // 销假（根据申请人的用户ID读取）
//        Task reportBackTask = taskService.createTaskQuery().taskAssignee(currentUserId).singleResult();
//        assertNotNull(formService.getRenderedTaskForm(reportBackTask.getId()));
//        variables = new HashMap<String, String>();
//        variables.put("reportBackDate", sdf.format(ca.getTime()));
//        formService.submitTaskFormData(reportBackTask.getId(), variables);

        // 验证流程是否已经结束
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().finished().singleResult();
        assertNotNull(historicProcessInstance);

        // 读取历史变量
        Map<String, Object> historyVariables = packageVariables(processInstance);

        // 验证执行结果
        assertEquals("ok", historyVariables.get("result"));

    }

    /**
     * 任意流程的跳转
     */
    @Test
    public void taskJump(){
        // 当前任务
        String taskId="ddd";
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String assignee = "下一个自由跳转人";
        taskService.setAssignee(taskId,assignee);
        // 自由跳转
        String taskDefKey="目标-任务名称";
        //moveActivityIdTo的两个参数，源任务key,目标任务key
        runtimeService.createChangeActivityStateBuilder().processInstanceId(task.getProcessInstanceId()).moveActivityIdTo(task.getTaskDefinitionKey(), taskDefKey).changeState();

    }


    /**
     * 读取历史变量并封装到Map中
     */
    private Map<String, Object> packageVariables(ProcessInstance processInstance) {
        Map<String, Object> historyVariables = new HashMap<String, Object>();
        List<HistoricDetail> list = historyService.createHistoricDetailQuery().processInstanceId(processInstance.getId()).list();
        for (HistoricDetail historicDetail : list) {
            if (historicDetail instanceof HistoricFormProperty) {
                // 表单中的字段
                HistoricFormProperty field = (HistoricFormProperty) historicDetail;
                historyVariables.put(field.getPropertyId(), field.getPropertyValue());
                System.out.println("form field: taskId=" + field.getTaskId() + ", " + field.getPropertyId() + " = " + field.getPropertyValue());
            } else if (historicDetail instanceof HistoricVariableUpdate) {
                HistoricVariableUpdate variable = (HistoricVariableUpdate) historicDetail;
                historyVariables.put(variable.getVariableName(), variable.getValue());
                System.out.println("variable: " + variable.getVariableName() + " = " + variable.getValue());
            }
        }
        return historyVariables;
    }
    private void printTask(Task task){
        System.out.println(task.getName()+" : " + task.getDueDate());
    }
}
