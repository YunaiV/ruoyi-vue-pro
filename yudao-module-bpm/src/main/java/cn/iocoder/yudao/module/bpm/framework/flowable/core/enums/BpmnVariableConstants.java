package cn.iocoder.yudao.module.bpm.framework.flowable.core.enums;

import org.flowable.engine.runtime.ProcessInstance;

/**
 * BPM Variable 通用常量
 *
 * @author 芋道源码
 */
public class BpmnVariableConstants {

    /**
     * 流程实例的变量 - 状态
     *
     * @see ProcessInstance#getProcessVariables()
     */
    public static final String PROCESS_INSTANCE_VARIABLE_STATUS = "PROCESS_STATUS";
    /**
     * 流程实例的变量 - 理由
     *
     * 例如说：审批不通过的理由（目前审核通过暂时不会记录）
     *
     * @see ProcessInstance#getProcessVariables()
     */
    public static final String PROCESS_INSTANCE_VARIABLE_REASON = "PROCESS_REASON";
    /**
     * 流程实例的变量 - 发起用户选择的审批人 Map
     *
     * @see ProcessInstance#getProcessVariables()
     * @see BpmTaskCandidateStrategyEnum#START_USER_SELECT
     */
    public static final String PROCESS_INSTANCE_VARIABLE_START_USER_SELECT_ASSIGNEES = "PROCESS_START_USER_SELECT_ASSIGNEES";
    /**
     * 流程实例的变量 - 审批人选择的审批人 Map
     *
     * @see ProcessInstance#getProcessVariables()
     * @see BpmTaskCandidateStrategyEnum#APPROVE_USER_SELECT
     */
    public static final String PROCESS_INSTANCE_VARIABLE_APPROVE_USER_SELECT_ASSIGNEES = "PROCESS_APPROVE_USER_SELECT_ASSIGNEES";
    /**
     * 流程实例的变量 - 发起用户 ID
     *
     * @see ProcessInstance#getProcessVariables()
     */
    public static final String PROCESS_INSTANCE_VARIABLE_START_USER_ID = "PROCESS_START_USER_ID";

    /**
     * 流程实例的变量 - 用于判断流程实例变量节点是否驳回：格式 RETURN_FLAG_{节点 id}
     *
     * 目的是：退回到发起节点时，因为审批人与发起人相同，所以被自动通过。但是，此时还是希望不要自动通过
     *
     * @see ProcessInstance#getProcessVariables()
     */
    public static final String PROCESS_INSTANCE_VARIABLE_RETURN_FLAG = "RETURN_FLAG_%s";

    /**
     * 流程实例的变量前缀 - 用于退回操作，记录需要预测的节点：格式 NEED_SIMULATE_TASK_{节点定义 id}
     *
     * 目的是：退回操作，预测节点会不准，在流程变量中记录需要预测的节点，来辅助预测
     */
    public static final String PROCESS_INSTANCE_VARIABLE_NEED_SIMULATE_PREFIX = "NEED_SIMULATE_TASK_";

    /**
     * 流程实例的变量 - 是否跳过表达式
     *
     * @see ProcessInstance#getProcessVariables()
     * @see <a href="https://blog.csdn.net/weixin_42065235/article/details/126039993">Flowable/Activiti之SkipExpression 完成自动审批</a>
     */
    public static final String PROCESS_INSTANCE_SKIP_EXPRESSION_ENABLED = "_FLOWABLE_SKIP_EXPRESSION_ENABLED";

    /**
     * 流程实例的变量 - 用于判断流程是否需要跳过发起人节点
     *
     * @see ProcessInstance#getProcessVariables()
     */
    public static final String PROCESS_INSTANCE_VARIABLE_SKIP_START_USER_NODE = "PROCESS_SKIP_START_USER_NODE";

    /**
     * 流程实例的变量 - 流程开始时间
     *
     * 【非存储变量】用于部分需要 format 的场景，例如说：流程实例的自定义标题
     */
    public static final String PROCESS_START_TIME = "PROCESS_START_TIME";
    /**
     * 流程实例的变量 - 流程定义名称
     */
    public static final String PROCESS_DEFINITION_NAME = "PROCESS_DEFINITION_NAME";

    /**
     * 任务的变量 - 状态
     *
     * @see org.flowable.task.api.Task#getTaskLocalVariables()
     */
    public static final String TASK_VARIABLE_STATUS = "TASK_STATUS";
    /**
     * 任务的变量 - 理由
     *
     * 例如说：审批通过、不通过的理由
     *
     * @see org.flowable.task.api.Task#getTaskLocalVariables()
     */
    public static final String TASK_VARIABLE_REASON = "TASK_REASON";
    /**
     * 任务变量 - 签名图片 URL
     */
    public static final String TASK_SIGN_PIC_URL = "TASK_SIGN_PIC_URL";

}
