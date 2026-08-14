package cn.iocoder.yudao.module.hrm.enums.performance.plan;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 绩效考核周期类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmPerformanceCycleTypeEnum implements ArrayValuable<Integer> {

    MONTH(1, "月度"),
    QUARTER(2, "季度"),
    FIRST_HALF_YEAR(3, "上半年"),
    SECOND_HALF_YEAR(4, "下半年"),
    YEAR(5, "全年"),
    OTHER(6, "其他");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmPerformanceCycleTypeEnum::getType).toArray(Integer[]::new);

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

    public static HrmPerformanceCycleTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
