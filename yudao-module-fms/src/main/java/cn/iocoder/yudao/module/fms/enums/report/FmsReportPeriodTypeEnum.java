package cn.iocoder.yudao.module.fms.enums.report;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * FMS 报表期间类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum FmsReportPeriodTypeEnum implements ArrayValuable<Integer> {

    MONTH(1, "月度"),
    QUARTER(2, "季度");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(FmsReportPeriodTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static FmsReportPeriodTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
