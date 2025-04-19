package cn.iocoder.yudao.module.wms.enums.inbound;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

/**
 * 入库状态
 **/
@RequiredArgsConstructor
@Getter
public enum WmsInboundStatus implements ArrayValuable<Integer>, DictEnum {

    NONE(0, "未入库"),
    PART(1, "部分入库"),
    ALL(2, "已入库"),
   ;

    public static final Integer[] VALUES = Arrays.stream(values()).map(WmsInboundStatus::getValue).toArray(Integer[]::new);

    public static String getType() {
        return WmsInboundStatus.class.getSimpleName();
    }

    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static WmsInboundStatus parse(Integer value) {
        for (WmsInboundStatus e : WmsInboundStatus.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static WmsInboundStatus parse(String nameOrLabel) {
        for (WmsInboundStatus e : WmsInboundStatus.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (WmsInboundStatus e : WmsInboundStatus.values()) {
            if(e.getLabel().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        return null;
    }

    public boolean matchAny(WmsInboundStatus... status) {
        for (WmsInboundStatus s : status) {
            if(s==this) {
                return true;
            }
        }
        return false;
    }

    public boolean matchAny(Integer... values) {
        for (Integer v : values) {
            if(Objects.equals(v, this.getValue())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Integer[] array() {
        return VALUES;
    }

    public static enum Event {
        SUBMIT,AGREE, REJECT;
    }
}
