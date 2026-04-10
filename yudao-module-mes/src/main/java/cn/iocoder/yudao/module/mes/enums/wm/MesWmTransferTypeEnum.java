package cn.iocoder.yudao.module.mes.enums.wm;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * MES 转移单类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MesWmTransferTypeEnum implements ArrayValuable<Integer> {

    /**
     * 内部调拨
     */
    INNER(1, "内部调拨"),
    /**
     * 外部调拨
     */
    OUTER(2, "外部调拨");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MesWmTransferTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型值
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    public static MesWmTransferTypeEnum valueOf(Integer type) {
        return Arrays.stream(values()).filter(e -> e.getType().equals(type)).findFirst().orElse(null);
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
