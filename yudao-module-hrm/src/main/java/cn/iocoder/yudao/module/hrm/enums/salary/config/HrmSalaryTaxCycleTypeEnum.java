package cn.iocoder.yudao.module.hrm.enums.salary.config;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 薪资计税周期类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmSalaryTaxCycleTypeEnum implements ArrayValuable<Integer> {

    DECEMBER_TO_NOVEMBER(1, "上年 12 月至本年 11 月"),
    JANUARY_TO_DECEMBER(2, "本年 1 月至 12 月");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmSalaryTaxCycleTypeEnum::getType).toArray(Integer[]::new);

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

    public static HrmSalaryTaxCycleTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
