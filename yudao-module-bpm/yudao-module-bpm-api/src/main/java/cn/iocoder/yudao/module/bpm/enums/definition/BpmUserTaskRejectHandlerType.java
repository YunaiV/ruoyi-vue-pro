package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * BPM 用户任务拒绝处理类型枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmUserTaskRejectHandlerType {

    // TODO @jason：是不是收敛成 2 个：FINISH_PROCESS => 1. 直接结束流程；RETURN_PRE_USER_TASK => 2. 驳回到指定节点（RETURN_USER_TASK【去掉 PRE】）
    FINISH_PROCESS(1, "终止流程"),
    RETURN_PRE_USER_TASK(2, "驳回到指定任务节点"),

    FINISH_PROCESS_BY_REJECT_NUMBER(3, "按拒绝人数终止流程"), // 用于会签
    FINISH_TASK(4, "结束任务"); // 待实现，可能会用于意见分支

    private final Integer type;
    private final String name;

    public static BpmUserTaskRejectHandlerType typeOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
