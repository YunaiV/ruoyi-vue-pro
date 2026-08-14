package cn.iocoder.yudao.module.hrm.enums.salary.config;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 薪资项类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmSalaryOptionTypeEnum implements ArrayValuable<Integer> {

    MINUS(0, "减项"),
    ADD(1, "加项"),
    CALCULATED(2, "计算项");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmSalaryOptionTypeEnum::getType).toArray(Integer[]::new);

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

    public static HrmSalaryOptionTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
