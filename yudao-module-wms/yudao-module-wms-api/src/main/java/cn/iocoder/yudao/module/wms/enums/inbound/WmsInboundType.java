package cn.iocoder.yudao.module.wms.enums.inbound;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 入库单类型
 **/
@RequiredArgsConstructor
@Getter
public enum WmsInboundType implements ArrayValuable<Integer>, DictEnum {

    MANUAL(1, "手工入库"),
    PURCHASE(2, "采购入库"),
    STOCKCHECK(3, "盘点入库"),
    TRANSFER(4, "调拨入库"),
    EXCHANGE(6, " 换货入库"),
   ;

    public static final Integer[] VALUES = Arrays.stream(values()).map(WmsInboundType::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static WmsInboundType parse(Integer value) {
        for (WmsInboundType e : WmsInboundType.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static WmsInboundType parse(String nameOrLabel) {
        for (WmsInboundType e : WmsInboundType.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (WmsInboundType e : WmsInboundType.values()) {
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
