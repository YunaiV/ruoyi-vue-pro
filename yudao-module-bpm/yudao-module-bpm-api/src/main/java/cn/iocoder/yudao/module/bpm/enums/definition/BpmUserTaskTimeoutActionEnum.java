package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户任务超时处理执行动作枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmUserTaskTimeoutActionEnum {

    AUTO_REMINDER(1,"自动提醒"),
    AUTO_APPROVE(2, "自动同意"),
    AUTO_REJECT(3, "自动拒绝");

    private final Integer action;
    private final String name;

    public static BpmUserTaskTimeoutActionEnum actionOf(Integer action) {
        return ArrayUtil.firstMatch(item -> item.getAction().equals(action), values());
    }
}
