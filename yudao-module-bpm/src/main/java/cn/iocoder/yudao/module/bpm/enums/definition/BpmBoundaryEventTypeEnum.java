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
public enum BpmBoundaryEventTypeEnum {

    USER_TASK_TIMEOUT(1, "用户任务超时"),
    DELAY_TIMER_TIMEOUT(2, "延迟器超时"),
    CHILD_PROCESS_TIMEOUT(3, "子流程超时");

    private final Integer type;
    private final String name;

    public static BpmBoundaryEventTypeEnum typeOf(Integer type) {
        return ArrayUtil.firstMatch(eventType -> eventType.getType().equals(type), values());
    }

}
