package cn.iocoder.yudao.module.wms.enums.stock;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 库存类型
 **/
@RequiredArgsConstructor
@Getter
public enum WmsStockType implements ArrayValuable<Integer>, DictEnum {

    WAREHOUSE(1, "仓库库存"),
    BIN(2, "仓位库存"),
    OWNERSHIP(3, "所有者库存"),
   ;

    public static final Integer[] VALUES = Arrays.stream(values()).map(WmsStockType::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static WmsStockType parse(Integer value) {
        for (WmsStockType e : WmsStockType.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static WmsStockType parse(String nameOrLabel) {
        for (WmsStockType e : WmsStockType.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (WmsStockType e : WmsStockType.values()) {
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
