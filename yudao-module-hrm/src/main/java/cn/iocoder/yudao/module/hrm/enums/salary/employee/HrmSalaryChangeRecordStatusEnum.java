package cn.iocoder.yudao.module.hrm.enums.salary.employee;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 定薪调薪记录状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmSalaryChangeRecordStatusEnum implements ArrayValuable<Integer> {

    PENDING(0, "待生效"),
    EFFECTIVE(1, "已生效"),
    CANCELLED(2, "已取消");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmSalaryChangeRecordStatusEnum::getStatus).toArray(Integer[]::new);

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

    public static HrmSalaryChangeRecordStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

}
