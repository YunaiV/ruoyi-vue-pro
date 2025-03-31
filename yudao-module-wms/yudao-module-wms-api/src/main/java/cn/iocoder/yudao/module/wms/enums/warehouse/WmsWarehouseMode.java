package cn.iocoder.yudao.module.wms.enums.warehouse;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 仓库经营方式
 **/
@RequiredArgsConstructor
@Getter
public enum WmsWarehouseMode implements ArrayValuable<Integer>, DictEnum {

    SELF_OWNED(0, "自营"),
    THIRD_PARTY(1, "三方仓"),
    PLATFORM(2, "平台仓");

    public static final Integer[] VALUES = Arrays.stream(values()).map(WmsWarehouseMode::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static WmsWarehouseMode parse(Integer value) {
        for (WmsWarehouseMode e : WmsWarehouseMode.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static WmsWarehouseMode parse(String nameOrLabel) {
        for (WmsWarehouseMode e : WmsWarehouseMode.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (WmsWarehouseMode e : WmsWarehouseMode.values()) {
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
