package cn.iocoder.yudao.module.mes.enums.pro;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 工单类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesProWorkOrderTypeEnum implements ArrayValuable<Integer> {

    SELF(1, "自行生产"),
    OUTSOURCE(2, "代工"),
    PURCHASE(3, "采购");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesProWorkOrderTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型值
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
