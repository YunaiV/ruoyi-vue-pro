package cn.iocoder.yudao.module.hrm.enums.salary.employee;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 定薪调薪原因枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmSalaryChangeReasonEnum implements ArrayValuable<Integer> {

    ENTRY_SALARY(0, "入职定薪"),
    ENTRY_CONFIRM(1, "入职核定"),
    REGULAR(2, "转正"),
    PROMOTION(3, "晋升"),
    TRANSFER(4, "调动"),
    MID_YEAR_ADJUSTMENT(5, "年中调薪"),
    ANNUAL_ADJUSTMENT(6, "年度调薪"),
    SPECIAL_ADJUSTMENT(7, "特别调薪"),
    OTHER(8, "其他");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmSalaryChangeReasonEnum::getReason).toArray(Integer[]::new);

    /**
     * 原因
     */
    private final Integer reason;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmSalaryChangeReasonEnum valueOf(Integer reason) {
        return ArrayUtil.firstMatch(item -> item.getReason().equals(reason), values());
    }

    public static HrmSalaryChangeReasonEnum valueOfName(String name) {
        return ArrayUtil.firstMatch(item -> item.getName().equals(name), values());
    }

}
