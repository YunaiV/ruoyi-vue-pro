package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程节点进度的枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmProcessNodeProgressEnum {
    // 0 未开始
    NOT_START(0,"未开始"),
    // 1 ~ 20 进行中
    RUNNING(1, "进行中"),  // 节点的进行
    // 特殊的进行中状态
    USER_TASK_DELEGATE(10, "委派中"), // 审批节点
    USER_TASK_APPROVING(11, "向后加签审批通过中"), //向后加签 审批通过中.
    USER_TASK_WAIT(12, "待审批"), // 一般用于先前加签

    // 30 ~ 50 已经结束
    // 30 ~ 40 审批节点的结束状态
    USER_TASK_APPROVE(30, "审批通过"), // 审批节点
    USER_TASK_REJECT(31, "审批不通过"), // 审批节点
    USER_TASK_RETURN(32, "已退回"), // 审批节点
    USER_TASK_CANCEL(34, "已取消"), // 审批节点
    // 40 ~ 50 一般节点的接榫状态
    FINISHED(41, "已结束"), // 一般节点的节点的结束状态
    SKIP(42, "跳过"); // 未执行，跳过的节点

    private final Integer status;
    private final String name;

    public static Integer convertBpmnTaskStatus(Integer taskStatus) {
        Integer convertStatus = null;
        if (BpmTaskStatusEnum.RUNNING.getStatus().equals(taskStatus)) {
            convertStatus =  RUNNING.getStatus();
        } else if (BpmTaskStatusEnum.REJECT.getStatus().equals(taskStatus)) {
            convertStatus = USER_TASK_REJECT.getStatus();
        } else if( BpmTaskStatusEnum.APPROVE.getStatus().equals(taskStatus) ) {
            convertStatus = USER_TASK_APPROVE.getStatus();
        } else if (BpmTaskStatusEnum.DELEGATE.getStatus().equals(taskStatus)) {
            convertStatus = USER_TASK_DELEGATE.getStatus();
        } else if (BpmTaskStatusEnum.APPROVING.getStatus().equals(taskStatus)) {
            convertStatus = USER_TASK_APPROVE.getStatus();
        } else if (BpmTaskStatusEnum.CANCEL.getStatus().equals(taskStatus)) {
            convertStatus = USER_TASK_CANCEL.getStatus();
        } else if (BpmTaskStatusEnum.WAIT.getStatus().equals(taskStatus)) {
            convertStatus = USER_TASK_WAIT.getStatus();
        }
        return convertStatus;
    }

    /**
     * 判断用户节点是不是未通过
     *
     * @param status 状态
     */
    public static boolean isUserTaskNotApproved(Integer status) {
        return ObjectUtils.equalsAny(status,
                USER_TASK_REJECT.getStatus(), USER_TASK_RETURN.getStatus(), USER_TASK_CANCEL.getStatus());
    }
}
