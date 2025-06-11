package cn.iocoder.yudao.module.wms.enums.outbound;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 出库单类型
 **/
@RequiredArgsConstructor
@Getter
public enum WmsOutboundType implements ArrayValuable<Integer>, DictEnum {

    MANUAL(1, "手工出库"),
    ORDER(2, "订单出库"),
    STOCKCHECK(3, "盘点出库"),

    FIRST_MILE(4, "头程单出库"),
    TRANSFER(5, "调拨出库"),
    EXCHANGE(6, "换货出库");

    public static final Integer[] VALUES = Arrays.stream(values()).map(WmsOutboundType::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static WmsOutboundType parse(Integer value) {
        for (WmsOutboundType e : WmsOutboundType.values()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static WmsOutboundType parse(String nameOrLabel) {
        for (WmsOutboundType e : WmsOutboundType.values()) {
            if (e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (WmsOutboundType e : WmsOutboundType.values()) {
            if (e.getLabel().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        return null;
    }

    @Override
    public Integer[] array() {
        return VALUES;
    }
}
