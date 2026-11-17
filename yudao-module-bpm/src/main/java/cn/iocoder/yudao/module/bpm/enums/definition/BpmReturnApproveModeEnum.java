package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 退回到审批节点后的推进方式。
 */
@Getter
@AllArgsConstructor
public enum BpmReturnApproveModeEnum implements ArrayValuable<Integer> {

    RESTART(1, "重新依次审批"),
    RESUME_SOURCE(2, "从当前节点审批");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BpmReturnApproveModeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    public static BpmReturnApproveModeEnum typeOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
