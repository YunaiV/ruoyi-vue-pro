package cn.iocoder.yudao.module.wms.enums.common;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 来源单据类型
 **/
@RequiredArgsConstructor
@Getter
public enum BillType implements ArrayValuable<Integer>, DictEnum {

    INBOUND(0, "出库单"),
    OUTBOUND(1, "入库单");

    public static final Integer[] VALUES = Arrays.stream(values()).map(BillType::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static BillType parse(Integer value) {
        for (BillType e : BillType.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static BillType parse(String nameOrLabel) {
        for (BillType e : BillType.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (BillType e : BillType.values()) {
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
