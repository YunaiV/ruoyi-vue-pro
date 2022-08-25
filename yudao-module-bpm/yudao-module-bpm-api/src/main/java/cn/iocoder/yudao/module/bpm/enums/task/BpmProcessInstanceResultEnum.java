package cn.iocoder.yudao.module.bpm.enums.task;

import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程实例的结果
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmProcessInstanceResultEnum {

    PROCESS(1, "处理中"),
    APPROVE(2, "通过"),
    REJECT(3, "不通过"),
    CANCEL(4, "已取消"),

    // ========== 流程任务独有的状态 ==========

    BACK(5, "退回/驳回");

    /**
     * 结果
     *
     * 如果新增时，注意 {@link #isEndResult(Integer)} 是否需要变更
     */
    private final Integer result;
    /**
     * 描述
     */
    private final String desc;

    /**
     * 判断该结果是否已经处于 End 最终结果
     *
     * 主要用于一些结果更新的逻辑，如果已经是最终结果，就不再进行更新
     *
     * @param result 结果
     * @return 是否
     */
    public static boolean isEndResult(Integer result) {
        return ObjectUtils.equalsAny(result, APPROVE.getResult(), REJECT.getResult(), CANCEL.getResult(), BACK.getResult());
    }

}
