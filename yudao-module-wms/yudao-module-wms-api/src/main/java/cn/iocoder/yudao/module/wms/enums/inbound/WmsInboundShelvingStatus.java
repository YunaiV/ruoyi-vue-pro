package cn.iocoder.yudao.module.wms.enums.inbound;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 入库单上架状态
 **/
@RequiredArgsConstructor
@Getter
public enum WmsInboundShelvingStatus implements ArrayValuable<Integer>, DictEnum {

    NONE(1, "未上架"),
    PARTLY(2, "部分上架"),
    ALL(3, "已上架"),
   ;

    public static final Integer[] VALUES = Arrays.stream(values()).map(WmsInboundShelvingStatus::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static WmsInboundShelvingStatus parse(Integer value) {
        for (WmsInboundShelvingStatus e : WmsInboundShelvingStatus.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static WmsInboundShelvingStatus parse(String nameOrLabel) {
        for (WmsInboundShelvingStatus e : WmsInboundShelvingStatus.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (WmsInboundShelvingStatus e : WmsInboundShelvingStatus.values()) {
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
