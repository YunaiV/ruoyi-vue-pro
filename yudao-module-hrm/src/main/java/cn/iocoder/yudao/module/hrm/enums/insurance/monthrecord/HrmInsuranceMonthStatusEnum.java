package cn.iocoder.yudao.module.hrm.enums.insurance.monthrecord;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 社保月度记录状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmInsuranceMonthStatusEnum implements ArrayValuable<Integer> {

    UNARCHIVED(0, "未归档"),
    ARCHIVED(1, "已归档");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmInsuranceMonthStatusEnum::getStatus).toArray(Integer[]::new);

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

    public static HrmInsuranceMonthStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

}
