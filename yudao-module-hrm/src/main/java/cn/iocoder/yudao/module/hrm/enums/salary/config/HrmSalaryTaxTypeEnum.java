package cn.iocoder.yudao.module.hrm.enums.salary.config;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 薪资计税类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmSalaryTaxTypeEnum implements ArrayValuable<Integer> {

    SALARY(1, "工资薪金所得税"),
    REMUNERATION(2, "劳务报酬所得税"),
    NONE(3, "不计税");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmSalaryTaxTypeEnum::getType).toArray(Integer[]::new);

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

    public static HrmSalaryTaxTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
