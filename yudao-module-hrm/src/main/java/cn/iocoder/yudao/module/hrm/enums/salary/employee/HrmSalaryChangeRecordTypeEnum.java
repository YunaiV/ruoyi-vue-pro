package cn.iocoder.yudao.module.hrm.enums.salary.employee;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 定薪调薪记录类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmSalaryChangeRecordTypeEnum implements ArrayValuable<Integer> {

    SALARY_SET(1, "定薪"),
    SALARY_ADJUSTMENT(2, "调薪");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmSalaryChangeRecordTypeEnum::getType).toArray(Integer[]::new);

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

    public static HrmSalaryChangeRecordTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
