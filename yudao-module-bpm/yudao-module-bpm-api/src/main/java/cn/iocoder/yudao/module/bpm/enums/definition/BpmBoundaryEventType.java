package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * BPM 边界事件 (boundary event) 自定义类型枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmBoundaryEventType {

    USER_TASK_TIMEOUT(1,"用户任务超时"),
    USER_TASK_REJECT_POST_PROCESS(2, "用户任务拒绝后处理");

    private final Integer type;
    private final String name;

    public static BpmBoundaryEventType typeOf(Integer type) {
        return ArrayUtil.firstMatch(eventType -> eventType.getType().equals(type), values());
    }
}
