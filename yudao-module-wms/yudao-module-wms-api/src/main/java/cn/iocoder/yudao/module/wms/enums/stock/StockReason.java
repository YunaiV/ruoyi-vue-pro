package cn.iocoder.yudao.module.wms.enums.stock;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 流水发生的原因
 **/
@RequiredArgsConstructor
@Getter
public enum StockReason implements ArrayValuable<Integer>, DictEnum {

    INBOUND(1, "入库"),
    PICKUP(2, "拣货"),

   ;

    public static final Integer[] VALUES = Arrays.stream(values()).map(StockReason::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static StockReason parse(Integer value) {
        for (StockReason e : StockReason.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static StockReason parse(String nameOrLabel) {
        for (StockReason e : StockReason.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (StockReason e : StockReason.values()) {
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
