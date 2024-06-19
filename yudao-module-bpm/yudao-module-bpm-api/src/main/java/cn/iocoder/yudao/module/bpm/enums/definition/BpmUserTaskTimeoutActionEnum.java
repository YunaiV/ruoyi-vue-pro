package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 用户任务超时处理执行动作枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmUserTaskTimeoutActionEnum implements IntArrayValuable {

    AUTO_REMINDER(1,"自动提醒"),
    AUTO_APPROVE(2, "自动同意"),
    AUTO_REJECT(3, "自动拒绝");

    private final Integer action;
    private final String name;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(BpmUserTaskTimeoutActionEnum::getAction).toArray();

    public static BpmUserTaskTimeoutActionEnum actionOf(Integer action) {
        return ArrayUtil.firstMatch(item -> item.getAction().equals(action), values());
    }

    @Override
    public int[] array() {
        return ARRAYS;
    }
}
