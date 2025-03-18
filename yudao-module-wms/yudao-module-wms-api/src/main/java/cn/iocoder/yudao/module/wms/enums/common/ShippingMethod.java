package cn.iocoder.yudao.module.wms.enums.common;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 运输方式
 **/
@RequiredArgsConstructor
@Getter
public enum ShippingMethod implements ArrayValuable<Integer>, DictEnum {

    OCEAN(0, "海运"),
    RAIL(1, "铁路"),
    AIR(2, "空运"),
    TRUCK(3, "集卡");

    public static final Integer[] VALUES = Arrays.stream(values()).map(ShippingMethod::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static ShippingMethod parse(Integer value) {
        for (ShippingMethod e : ShippingMethod.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static ShippingMethod parse(String nameOrLabel) {
        for (ShippingMethod e : ShippingMethod.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (ShippingMethod e : ShippingMethod.values()) {
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
