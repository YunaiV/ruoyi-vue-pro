package cn.iocoder.yudao.module.wms.enums.stock;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 库存流水方向
 **/
@RequiredArgsConstructor
@Getter
public enum WmsStockFlowDirection implements ArrayValuable<Integer>, DictEnum {

    OUT(-1, "流出"),
    IN(1, "流入"),
   ;

    public static final Integer[] VALUES = Arrays.stream(values()).map(WmsStockFlowDirection::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static WmsStockFlowDirection parse(Integer value) {
        for (WmsStockFlowDirection e : WmsStockFlowDirection.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static WmsStockFlowDirection parse(String nameOrLabel) {
        for (WmsStockFlowDirection e : WmsStockFlowDirection.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (WmsStockFlowDirection e : WmsStockFlowDirection.values()) {
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
