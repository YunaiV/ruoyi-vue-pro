package cn.iocoder.yudao.module.bpm.enums.task;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程实例/任务的的处理原因枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum BpmReasonEnum {

    // ========== 流程实例的独有原因 ==========

    REJECT_TASK("审批不通过任务，原因：{}"), // 场景：用户审批不通过任务。修改文案时，需要注意 isRejectReason 方法
    CANCEL_PROCESS_INSTANCE_BY_START_USER("用户主动取消流程，原因：{}"), // 场景：用户主动取消流程
    CANCEL_PROCESS_INSTANCE_BY_ADMIN("管理员【{}】取消流程，原因：{}"), // 场景：管理员取消流程

    // ========== 流程任务的独有原因 ==========

    CANCEL_BY_SYSTEM("系统自动取消"), // 场景：非常多，比如说：1）多任务审批已经满足条件，无需审批该任务；2）流程实例被取消，无需审批该任务；等等
    TIMEOUT_APPROVE("审批超时，系统自动通过"),
    TIMEOUT_REJECT("审批超时，系统自动不通过"),
    ASSIGN_START_USER_APPROVE("审批人与提交人为同一人时，自动通过"),
    ASSIGN_START_USER_APPROVE_WHEN_SKIP("审批人与提交人为同一人时，自动通过"),
    ASSIGN_START_USER_APPROVE_WHEN_DEPT_LEADER_NOT_FOUND("审批人与提交人为同一人时，找不到部门负责人，自动通过"),
    ASSIGN_START_USER_TRANSFER_DEPT_LEADER("审批人与提交人为同一人时，转交给部门负责人审批"),
    ASSIGN_EMPTY_APPROVE("审批人为空，自动通过"),
    ASSIGN_EMPTY_REJECT("审批人为空，自动不通过"),
    APPROVE_TYPE_AUTO_APPROVE("非人工审核，自动通过"),
    APPROVE_TYPE_AUTO_REJECT("非人工审核，自动不通过"),
    ;

    private final String reason;

    /**
     * 格式化理由
     *
     * @param args 参数
     * @return 理由
     */
    public String format(Object... args) {
        return StrUtil.format(reason, args);
    }

}
