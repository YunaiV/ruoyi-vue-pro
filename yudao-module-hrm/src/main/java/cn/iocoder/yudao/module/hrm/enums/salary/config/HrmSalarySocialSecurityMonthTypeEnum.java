package cn.iocoder.yudao.module.hrm.enums.salary.config;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 薪资对应社保月份类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmSalarySocialSecurityMonthTypeEnum implements ArrayValuable<Integer> {

    PREVIOUS_MONTH(0, "上月"),
    CURRENT_MONTH(1, "当月"),
    NEXT_MONTH(2, "次月");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmSalarySocialSecurityMonthTypeEnum::getType).toArray(Integer[]::new);

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

    public static HrmSalarySocialSecurityMonthTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
