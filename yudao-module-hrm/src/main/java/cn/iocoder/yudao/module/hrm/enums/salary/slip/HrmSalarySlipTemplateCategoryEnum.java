package cn.iocoder.yudao.module.hrm.enums.salary.slip;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 默认工资条模板分类枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmSalarySlipTemplateCategoryEnum implements ArrayValuable<Integer> {

    BASIC(-1, "基本项", 1),
    DETAIL(-2, "明细项", 2);

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmSalarySlipTemplateCategoryEnum::getCode).toArray(Integer[]::new);

    /**
     * 分类编码
     */
    private final Integer code;
    /**
     * 分类名称
     */
    private final String name;
    /**
     * 排序
     */
    private final Integer sort;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmSalarySlipTemplateCategoryEnum valueOf(Integer code) {
        return ArrayUtil.firstMatch(item -> item.getCode().equals(code), values());
    }

}
