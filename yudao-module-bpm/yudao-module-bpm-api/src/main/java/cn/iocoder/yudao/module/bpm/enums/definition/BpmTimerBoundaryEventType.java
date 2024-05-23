package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 定时器边界事件类型枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmTimerBoundaryEventType {

    USER_TASK_TIMEOUT(1,"用户任务超时");

    private final Integer type;
    private final String name;

    public static BpmTimerBoundaryEventType typeOf(Integer type) {
        return ArrayUtil.firstMatch(eventType -> eventType.getType().equals(type), values());
    }
}
