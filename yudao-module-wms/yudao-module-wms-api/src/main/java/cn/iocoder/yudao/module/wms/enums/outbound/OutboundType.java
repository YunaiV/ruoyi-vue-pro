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
public enum OutboundType implements ArrayValuable<Integer>, DictEnum {

    MANUAL(1, "手工出库"),
    ORDER(2, "订单出库"),
   ;

    public static final Integer[] VALUES = Arrays.stream(values()).map(OutboundType::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static OutboundType parse(Integer value) {
        for (OutboundType e : OutboundType.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static OutboundType parse(String nameOrLabel) {
        for (OutboundType e : OutboundType.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (OutboundType e : OutboundType.values()) {
            if(e.getLabel().equalsIgnoreCase(nameOrLabel)) {
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
