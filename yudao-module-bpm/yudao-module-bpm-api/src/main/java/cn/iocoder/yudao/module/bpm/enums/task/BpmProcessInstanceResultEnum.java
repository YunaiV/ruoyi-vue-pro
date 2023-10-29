package cn.iocoder.yudao.module.bpm.enums.task;

import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

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

    BACK(5, "驳回"), // 退回
    DELEGATE(6, "委派"),
    /**
     * 【加签】源任务已经审批完成，但是它使用了后加签，后加签的任务未完成，源任务就会是这个状态
     * 相当于是 通过 APPROVE 的特殊状态
     * 例如：A审批， A 后加签了 B，并且审批通过了任务，但是 B 还未审批，则当前任务状态为“待后加签任务完成”
     */
    SIGN_AFTER(7, "待后加签任务完成"),
    /**
     * 【加签】源任务未审批，但是向前加签了，所以源任务状态变为“待前加签任务完成”
     * 相当于是 处理中 PROCESS 的特殊状态
     * 例如：A 审批， A 前加签了 B，B 还未审核
     */
    SIGN_BEFORE(8, "待前加签任务完成"),
    /**
     * 【加签】后加签任务被创建时的初始状态
     * 相当于是 处理中 PROCESS 的特殊状态
     * 因为需要源任务先完成，才能到后加签的人来审批，所以加了一个状态区分
     */
    WAIT_BEFORE_TASK(9, "待前置任务完成");

    /**
     * 能被减签的状态
     */
    public static final List<Integer> CAN_SUB_SIGN_STATUS_LIST = Arrays.asList(PROCESS.result, WAIT_BEFORE_TASK.result);

    /**
     * 结果
     * <p>
     * 如果新增时，注意 {@link #isEndResult(Integer)} 是否需要变更
     */
    private final Integer result;
    /**
     * 描述
     */
    private final String desc;

    /**
     * 判断该结果是否已经处于 End 最终结果
     * <p>
     * 主要用于一些结果更新的逻辑，如果已经是最终结果，就不再进行更新
     *
     * @param result 结果
     * @return 是否
     */
    public static boolean isEndResult(Integer result) {
        return ObjectUtils.equalsAny(result, APPROVE.getResult(), REJECT.getResult(),
                CANCEL.getResult(), BACK.getResult(),
                SIGN_AFTER.getResult());
    }

}
