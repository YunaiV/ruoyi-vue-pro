package cn.iocoder.yudao.module.hrm.enums.insurance.employee;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 员工参保状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmInsuranceEmployeeStatusEnum implements ArrayValuable<Integer> {

    STOPPED(0, "停保"),
    NORMAL(1, "正常");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmInsuranceEmployeeStatusEnum::getStatus).toArray(Integer[]::new);

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

    public static HrmInsuranceEmployeeStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

}
