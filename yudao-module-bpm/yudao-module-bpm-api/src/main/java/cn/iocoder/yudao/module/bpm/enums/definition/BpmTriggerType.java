package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * BPM Simple 触发器类型枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum BpmTriggerType implements IntArrayValuable {

    HTTP_REQUEST(1, "发起 HTTP 请求");

    /**
     * 触发器执行动作类型
     */
    private final Integer type;

    /**
     * 触发器执行动作描述
     */
    private final String desc;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(BpmTriggerType::getType).toArray();

    @Override
    public int[] array() {
        return ARRAYS;
    }

    public static BpmTriggerType typeOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }
}
