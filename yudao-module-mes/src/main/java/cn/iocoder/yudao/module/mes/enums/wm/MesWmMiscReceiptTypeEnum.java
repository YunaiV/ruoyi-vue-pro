package cn.iocoder.yudao.module.mes.enums.wm;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 杂项入库类型枚举
 */
@Getter
@AllArgsConstructor
public enum MesWmMiscReceiptTypeEnum implements ArrayValuable<Integer> {

    ADJUST(1, "库存调整");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesWmMiscReceiptTypeEnum::getType).toArray(Integer[]::new);

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
