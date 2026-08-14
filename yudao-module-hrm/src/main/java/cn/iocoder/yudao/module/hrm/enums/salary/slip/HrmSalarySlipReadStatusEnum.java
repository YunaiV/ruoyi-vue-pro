package cn.iocoder.yudao.module.hrm.enums.salary.slip;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * HRM 工资条阅读状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum HrmSalarySlipReadStatusEnum implements ArrayValuable<Integer> {

    UNREAD(0, "未读"),
    READ(1, "已读");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(HrmSalarySlipReadStatusEnum::getStatus).toArray(Integer[]::new);

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

    public static HrmSalarySlipReadStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

}
