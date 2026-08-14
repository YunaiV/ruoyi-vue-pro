package cn.iocoder.yudao.module.hrm.enums.performance.config;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 绩效计分方式枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmPerformanceScoreCalculationEnum implements ArrayValuable<Integer> {

    WEIGHTED(1, "加权计算");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmPerformanceScoreCalculationEnum::getCalculation).toArray(Integer[]::new);

    /**
     * 计分方式
     */
    private final Integer calculation;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmPerformanceScoreCalculationEnum valueOf(Integer calculation) {
        return ArrayUtil.firstMatch(item -> item.getCalculation().equals(calculation), values());
    }

}
