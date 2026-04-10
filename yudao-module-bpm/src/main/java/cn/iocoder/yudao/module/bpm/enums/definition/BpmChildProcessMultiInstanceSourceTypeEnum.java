package cn.iocoder.yudao.module.bpm.enums.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * BPM 子流程多实例来源类型枚举
 *
 * @author Lesan
 */
@Getter
@AllArgsConstructor
public enum BpmChildProcessMultiInstanceSourceTypeEnum implements ArrayValuable<Integer> {

    FIXED_QUANTITY(1, "固定数量"),
    NUMBER_FORM(2, "数字表单"),
    MULTIPLE_FORM(3, "多选表单");

    private final Integer type;
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BpmChildProcessMultiInstanceSourceTypeEnum::getType).toArray(Integer[]::new);

    public static BpmChildProcessMultiInstanceSourceTypeEnum typeOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
