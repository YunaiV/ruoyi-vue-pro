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

    TERMINATION(1, "终止流程"),
    RETURN_PRE_USER_TASK(2, "驳回到用户任务");

    private final Integer type;
    private final String name;

    public static BpmUserTaskRejectHandlerType typeOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }
}
