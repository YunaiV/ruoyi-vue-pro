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
     * 流程实例的变量 - 用于退回操作，记录需要预测的节点 ids, 变量值类型为 Set
     *
     * 目的是：退回操作，预测节点会不准，在流程变量中记录需要预测的节点，来辅助预测
     */
    public static final String PROCESS_INSTANCE_VARIABLE_NEED_SIMULATE_TASK_IDS = "NEED_SIMULATE_TASK_IDS";

    /**
     * 流程实例的变量 - 用于审批超时【自动跳转】，标记流程实例发生过跳转，变量值类型为 Boolean
     *
     * 目的是：跨分支、往回跳转后，流程实例不再按流程变量从头执行，审批详情改为从当前进行中的节点开始预测
     */
    public static final String PROCESS_INSTANCE_VARIABLE_TIMEOUT_JUMPED = "PROCESS_TIMEOUT_JUMPED";

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
     * 任务的变量 - 退回修改的修改人编号
     *
     * 退回后【从当前节点审批】时，任务处于退回修改中（WAIT_REVISION），由该用户修改表单后重新提交
     */
    public static final String TASK_VARIABLE_REVISION_ASSIGNEE = "TASK_REVISION_ASSIGNEE";
    /**
     * 任务的变量 - 退回修改的目标节点编号
     *
     * 修改人按该节点的表单字段权限修改表单
     */
    public static final String TASK_VARIABLE_REVISION_TARGET_NODE = "TASK_REVISION_TARGET_NODE";
    /**
     * 任务的变量 - 退回修改的批次编号
     *
     * 每次退回修改时重新生成，重新提交后保留：重新提交、审批人审批时，需要与前端详情中的批次一致，避免过期的页面提交
     */
    public static final String TASK_VARIABLE_REVISION_ID = "TASK_REVISION_ID";

    /**
     * 任务变量 - 签名图片 URL
     */
    public static final String TASK_SIGN_PIC_URL = "TASK_SIGN_PIC_URL";

    /**
     * 任务变量 - 委派的归还顺序
     *
     * 逗号分隔的用户编号，最后一个是当前被委派人的直接委派人。多次委派时，审批后逐层归还
     */
    public static final String TASK_DELEGATE_RETURN_USERS = "TASK_DELEGATE_RETURN_USERS";
    /**
     * 任务变量 - 加签等待中的审批人
     *
     * 被委派的任务向前加签时，owner 需要保留给委派归还使用，所以等待恢复审批的用户记录在这里
     */
    public static final String TASK_SIGN_WAIT_ASSIGNEE = "TASK_SIGN_WAIT_ASSIGNEE";

}
