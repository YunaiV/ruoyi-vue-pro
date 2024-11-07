package cn.iocoder.yudao.module.bpm.framework.flowable.core.util;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.common.engine.api.variable.VariableContainer;
import org.flowable.common.engine.impl.el.ExpressionManager;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.TaskInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Flowable 相关的工具方法
 *
 * @author 芋道源码
 */
public class FlowableUtils {

    // ========== User 相关的工具方法 ==========

    public static void setAuthenticatedUserId(Long userId) {
        Authentication.setAuthenticatedUserId(String.valueOf(userId));
    }

    public static void clearAuthenticatedUserId() {
        Authentication.setAuthenticatedUserId(null);
    }

    public static String getTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        return tenantId != null ? String.valueOf(tenantId) : ProcessEngineConfiguration.NO_TENANT_ID;
    }

    public static void execute(String tenantIdStr, Runnable runnable) {
        if (ObjectUtil.isEmpty(tenantIdStr)
                || Objects.equals(tenantIdStr, ProcessEngineConfiguration.NO_TENANT_ID)) {
            runnable.run();
        } else {
            Long tenantId = Long.valueOf(tenantIdStr);
            TenantUtils.execute(tenantId, runnable);
        }
    }

    // ========== Execution 相关的工具方法 ==========

    /**
     * 格式化多实例（并签、或签）的 collectionVariable 变量（多实例对应的多审批人列表）
     *
     * @param activityId 活动编号
     * @return collectionVariable 变量
     */
    public static String formatExecutionCollectionVariable(String activityId) {
        return activityId + "_assignees";
    }

    /**
     * 格式化多实例（并签、或签）的 collectionElementVariable 变量（当前实例对应的一个审批人）
     *
     * @param activityId 活动编号
     * @return collectionElementVariable 变量
     */
    public static String formatExecutionCollectionElementVariable(String activityId) {
        return activityId + "_assignee";
    }

    // ========== ProcessInstance 相关的工具方法 ==========

    public static Integer getProcessInstanceStatus(ProcessInstance processInstance) {
        return getProcessInstanceStatus(processInstance.getProcessVariables());
    }

    public static Integer getProcessInstanceStatus(HistoricProcessInstance processInstance) {
        return getProcessInstanceStatus(processInstance.getProcessVariables());
    }

    /**
     * 获得流程实例的状态
     *
     * @param processVariables 流程实例的 variables
     * @return 状态
     */
    private static Integer getProcessInstanceStatus(Map<String, Object> processVariables) {
        return (Integer) processVariables.get(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS);
    }

    /**
     * 获得流程实例的表单
     *
     * @param processInstance 流程实例
     * @return 表单
     */
    public static Map<String, Object> getProcessInstanceFormVariable(HistoricProcessInstance processInstance) {
        Map<String, Object> formVariables = new HashMap<>(processInstance.getProcessVariables());
        filterProcessInstanceFormVariable(formVariables);
        return formVariables;
    }

    /**
     * 过滤流程实例的表单
     *
     * 为什么要过滤？目前使用 processVariables 存储所有流程实例的拓展字段，需要过滤掉一部分的系统字段，从而实现表单的展示
     *
     * @param processVariables 流程实例的 variables
     * @return 过滤后的表单
     */
    public static Map<String, Object> filterProcessInstanceFormVariable(Map<String, Object> processVariables) {
        processVariables.remove(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS);
        return processVariables;
    }

    /**
     * 获得流程实例的发起用户选择的审批人 Map
     *
     * @param processInstance 流程实例
     * @return 发起用户选择的审批人 Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, List<Long>> getStartUserSelectAssignees(ProcessInstance processInstance) {
        return (Map<String, List<Long>>) processInstance.getProcessVariables().get(
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_SELECT_ASSIGNEES);
    }

    // ========== Task 相关的工具方法 ==========

    /**
     * 获得任务的状态
     *
     * @param task 任务
     * @return 状态
     */
    public static Integer getTaskStatus(TaskInfo task) {
        return (Integer) task.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_STATUS);
    }

    /**
     * 获得任务的审批原因
     *
     * @param task 任务
     * @return 审批原因
     */
    public static String getTaskReason(TaskInfo task) {
        return (String) task.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_REASON);
    }

    /**
     * 获得任务的表单
     *
     * @param task 任务
     * @return 表单
     */
    public static Map<String, Object> getTaskFormVariable(TaskInfo task) {
        Map<String, Object> formVariables = new HashMap<>(task.getTaskLocalVariables());
        filterTaskFormVariable(formVariables);
        return formVariables;
    }

    /**
     * 过滤任务的表单
     *
     * 为什么要过滤？目前使用 taskLocalVariables 存储所有任务的拓展字段，需要过滤掉一部分的系统字段，从而实现表单的展示
     *
     * @param taskLocalVariables 任务的 taskLocalVariables
     * @return 过滤后的表单
     */
    public static Map<String, Object> filterTaskFormVariable(Map<String, Object> taskLocalVariables) {
        taskLocalVariables.remove(BpmnVariableConstants.TASK_VARIABLE_STATUS);
        taskLocalVariables.remove(BpmnVariableConstants.TASK_VARIABLE_REASON);
        return taskLocalVariables;
    }

    // ========== Expression 相关的工具方法 ==========

    public static Object getExpressionValue(VariableContainer variableContainer, String expressionString) {
        ProcessEngineConfigurationImpl processEngineConfiguration = CommandContextUtil.getProcessEngineConfiguration();
        assert processEngineConfiguration != null;
        ExpressionManager expressionManager = processEngineConfiguration.getExpressionManager();
        assert expressionManager != null;
        Expression expression = expressionManager.createExpression(expressionString);
        return expression.getValue(variableContainer);
    }

}
