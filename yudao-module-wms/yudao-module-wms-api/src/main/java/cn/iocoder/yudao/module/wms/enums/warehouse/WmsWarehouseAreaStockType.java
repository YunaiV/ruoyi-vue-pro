package cn.iocoder.yudao.module.wms.enums.warehouse;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 存货类型
 **/
@RequiredArgsConstructor
@Getter
public enum WmsWarehouseAreaStockType implements ArrayValuable<Integer>, DictEnum {


    PICK (1, "拣货"),
    STORE(2, "存储");

    public static final Integer[] VALUES = Arrays.stream(values()).map(WmsWarehouseAreaStockType::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static WmsWarehouseAreaStockType parse(Integer value) {
        for (WmsWarehouseAreaStockType e : WmsWarehouseAreaStockType.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static WmsWarehouseAreaStockType parse(String nameOrLabel) {
        for (WmsWarehouseAreaStockType e : WmsWarehouseAreaStockType.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (WmsWarehouseAreaStockType e : WmsWarehouseAreaStockType.values()) {
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
