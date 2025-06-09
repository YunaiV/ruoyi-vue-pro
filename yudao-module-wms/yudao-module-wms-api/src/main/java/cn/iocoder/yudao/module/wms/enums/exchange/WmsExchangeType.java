package cn.iocoder.yudao.module.wms.enums.exchange;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 换货单类型
 **/
@RequiredArgsConstructor
@Getter
public enum WmsExchangeType implements ArrayValuable<Integer>, DictEnum {

    TO_ITEM(1, "良品转次品"),
    TO_GOOD(2, "次品转良品"),
    // STOCKCHECK(3, "拆套"),
    // STOCKCHECK(3, "合套"),
   ;

    public static final Integer[] VALUES = Arrays.stream(values()).map(WmsExchangeType::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static WmsExchangeType parse(Integer value) {
        for (WmsExchangeType e : WmsExchangeType.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static WmsExchangeType parse(String nameOrLabel) {
        for (WmsExchangeType e : WmsExchangeType.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (WmsExchangeType e : WmsExchangeType.values()) {
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
