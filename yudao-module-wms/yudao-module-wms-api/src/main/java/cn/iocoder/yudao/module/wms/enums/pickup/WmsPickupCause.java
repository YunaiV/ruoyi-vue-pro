package cn.iocoder.yudao.module.wms.enums.pickup;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 状态，WMS通用的对象有效状态
 **/
@RequiredArgsConstructor
@Getter
public enum WmsPickupCause implements ArrayValuable<Integer>, DictEnum {

    PICKUP(1, "拣货"),
    STOCKCHECK(2, "盘点"),
    BIN_MOVE(3, "移库位");

    public static final Integer[] VALUES = Arrays.stream(values()).map(WmsPickupCause::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static WmsPickupCause parse(Integer value) {
        for (WmsPickupCause e : WmsPickupCause.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static WmsPickupCause parse(String nameOrLabel) {
        for (WmsPickupCause e : WmsPickupCause.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (WmsPickupCause e : WmsPickupCause.values()) {
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
