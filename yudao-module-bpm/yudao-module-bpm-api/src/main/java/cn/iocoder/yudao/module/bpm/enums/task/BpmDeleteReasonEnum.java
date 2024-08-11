package cn.iocoder.yudao.module.bpm.enums.task;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程实例/任务的删除原因枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum BpmDeleteReasonEnum {

    // ========== 流程实例的独有原因 ==========

    REJECT_TASK("审批不通过任务，原因：{}"), // 场景：用户审批不通过任务。修改文案时，需要注意 isRejectReason 方法
    CANCEL_PROCESS_INSTANCE_BY_START_USER("用户主动取消流程，原因：{}"), // 场景：用户主动取消流程
    CANCEL_PROCESS_INSTANCE_BY_ADMIN("管理员【{}】取消流程，原因：{}"), // 场景：管理员取消流程

    // ========== 流程任务的独有原因 ==========

    CANCEL_BY_SYSTEM("系统自动取消"), // 场景：非常多，比如说：1）多任务审批已经满足条件，无需审批该任务；2）流程实例被取消，无需审批该任务；等等
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

    // ========== 逻辑 ==========

    public static boolean isRejectReason(String reason) {
        return StrUtil.startWith(reason, "审批不通过任务，原因：");
    }

}
