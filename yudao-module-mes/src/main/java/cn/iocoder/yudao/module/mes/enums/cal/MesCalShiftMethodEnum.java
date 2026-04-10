package cn.iocoder.yudao.module.mes.enums.cal;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 倒班方式枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesCalShiftMethodEnum implements ArrayValuable<Integer> {

    QUARTER(1, "按季度"),
    MONTH(2, "按月"),
    WEEK(3, "按周"),
    DAY(4, "按天");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesCalShiftMethodEnum::getMethod).toArray(Integer[]::new);

    /**
     * 方式值
     */
    private final Integer method;
    /**
     * 方式名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
