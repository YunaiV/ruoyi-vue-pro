package cn.iocoder.yudao.module.fms.enums.config;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * FMS 科目类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum FmsSubjectTypeEnum implements ArrayValuable<Integer> {

    ASSET(1, "资产"),
    LIABILITY(2, "负债"),
    EQUITY(3, "所有者权益"),
    COST(4, "成本"),
    PROFIT_LOSS(5, "损益"),
    COMMON(6, "共同");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(FmsSubjectTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static FmsSubjectTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
