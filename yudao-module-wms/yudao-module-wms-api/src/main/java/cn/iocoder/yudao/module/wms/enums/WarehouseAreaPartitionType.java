package cn.iocoder.yudao.module.wms.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 分区类型
 **/
@RequiredArgsConstructor
@Getter
public enum WarehouseAreaPartitionType implements ArrayValuable<Integer>, DictEnum {


    PICK (1, "标准品"),
    STORE(2, "不良品");

    public static final Integer[] VALUES = Arrays.stream(values()).map(WarehouseAreaPartitionType::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static WarehouseAreaPartitionType parse(Integer value) {
        for (WarehouseAreaPartitionType e : WarehouseAreaPartitionType.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static WarehouseAreaPartitionType parse(String nameOrLabel) {
        for (WarehouseAreaPartitionType e : WarehouseAreaPartitionType.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (WarehouseAreaPartitionType e : WarehouseAreaPartitionType.values()) {
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
