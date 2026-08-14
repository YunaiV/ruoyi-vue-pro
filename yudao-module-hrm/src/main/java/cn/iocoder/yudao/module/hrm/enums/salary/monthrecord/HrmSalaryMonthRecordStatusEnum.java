package cn.iocoder.yudao.module.hrm.enums.salary.monthrecord;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 月度工资表状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmSalaryMonthRecordStatusEnum implements ArrayValuable<Integer> {

    UNCOMPUTED(5, "未核算"),
    HISTORY(10, "已归档"),
    COMPUTED(11, "已核算");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmSalaryMonthRecordStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static HrmSalaryMonthRecordStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

}
