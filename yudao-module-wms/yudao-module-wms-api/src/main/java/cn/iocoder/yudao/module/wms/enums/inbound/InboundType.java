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
public enum InboundType implements ArrayValuable<Integer>, DictEnum {

    MANUAL(1, "手工"),
   ;

    public static final Integer[] VALUES = Arrays.stream(values()).map(InboundType::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static InboundType parse(Integer value) {
        for (InboundType e : InboundType.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static InboundType parse(String nameOrLabel) {
        for (InboundType e : InboundType.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (InboundType e : InboundType.values()) {
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
