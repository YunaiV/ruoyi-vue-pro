package cn.iocoder.yudao.module.hrm.enums.salary.slip;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 工资条模板项类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmSalarySlipTemplateOptionTypeEnum implements ArrayValuable<Integer> {

    CATEGORY(1, "分类"),
    ITEM(2, "薪资项");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmSalarySlipTemplateOptionTypeEnum::getType).toArray(Integer[]::new);

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

    public static HrmSalarySlipTemplateOptionTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
