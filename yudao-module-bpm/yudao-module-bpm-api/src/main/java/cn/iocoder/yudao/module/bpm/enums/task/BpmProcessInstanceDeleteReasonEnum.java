package cn.iocoder.yudao.module.bpm.enums.task;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程实例的删除原因
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum BpmProcessInstanceDeleteReasonEnum {

    REJECT_TASK("不通过任务，原因：{}"), // 修改文案时，需要注意 isRejectReason 方法
    CANCEL_TASK("主动取消任务，原因：{}"),

    // ========== 流程任务的独有原因 ==========
    MULTI_TASK_END("系统自动取消，原因：多任务审批已经满足条件，无需审批该任务"), // 多实例满足 condition 而结束时，其它任务实例任务会被取消，对应的删除原因是 MI_END

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
        return StrUtil.startWith(reason, "不通过任务，原因：");
    }

    /**
     * 将 Flowable 的删除原因，翻译成对应的中文原因
     *
     * @param reason 原始原因
     * @return 原因
     */
    public static String translateReason(String reason) {
        if (StrUtil.isEmpty(reason)) {
            return reason;
        }
        switch (reason) {
            case "MI_END": return MULTI_TASK_END.getReason();
            default: return reason;
        }
    }

}
