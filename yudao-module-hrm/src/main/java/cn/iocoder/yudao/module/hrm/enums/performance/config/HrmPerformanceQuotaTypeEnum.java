package cn.iocoder.yudao.module.hrm.enums.performance.config;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 绩效指标类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmPerformanceQuotaTypeEnum implements ArrayValuable<Integer> {

    PERFORMANCE(1, "业绩指标"),
    BEHAVIOR(2, "行为态度指标");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmPerformanceQuotaTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmPerformanceQuotaTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
