package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 用户任务超时处理类型枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmUserTaskTimeoutHandlerType implements IntArrayValuable {

    REMINDER(1,"自动提醒"),
    APPROVE(2, "自动同意"),
    REJECT(3, "自动拒绝");

    // TODO @jason：type 是不是更合适哈；
    private final Integer action;
    private final String name;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(BpmUserTaskTimeoutHandlerType::getAction).toArray();

    public static BpmUserTaskTimeoutHandlerType typeOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getAction().equals(type), values());
    }

    @Override
    public int[] array() {
        return ARRAYS;
    }
}
