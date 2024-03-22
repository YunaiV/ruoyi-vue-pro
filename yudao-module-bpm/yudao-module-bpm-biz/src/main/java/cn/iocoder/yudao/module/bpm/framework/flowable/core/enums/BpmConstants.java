package cn.iocoder.yudao.module.bpm.framework.flowable.core.enums;

import org.flowable.engine.runtime.ProcessInstance;

/**
 * BPM 通用常量
 *
 * @author 芋道源码
 */
public class BpmConstants {

    /**
     * 流程实例的变量 - 状态
     *
     * @see ProcessInstance#getProcessVariables()
     */
    public static final String PROCESS_INSTANCE_VARIABLE_STATUS = "PROCESS_STATUS";
    /**
     * 流程实例的变量 - 发起用户选择的审批人 Map
     *
     * @see ProcessInstance#getProcessVariables()
     */
    public static final String PROCESS_INSTANCE_VARIABLE_START_USER_SELECT_ASSIGNEES = "PROCESS_START_USER_SELECT_ASSIGNEES";

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

}
