package cn.iocoder.yudao.module.wms.enums.stock.check;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

/**
 * 盘点状态
 **/
@RequiredArgsConstructor
@Getter
public enum WmsStockCheckStatus implements ArrayValuable<Integer>, DictEnum {

    BALANCED(0, "盘平"),
    LOSS(1, "盘亏"),
    SURPLUS(2, "盘盈"),
   ;

    public static final Integer[] VALUES = Arrays.stream(values()).map(WmsStockCheckStatus::getValue).toArray(Integer[]::new);

    public static String getType() {
        return WmsStockCheckStatus.class.getSimpleName();
    }

    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static WmsStockCheckStatus parse(Integer value) {
        for (WmsStockCheckStatus e : WmsStockCheckStatus.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static WmsStockCheckStatus parse(String nameOrLabel) {
        for (WmsStockCheckStatus e : WmsStockCheckStatus.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (WmsStockCheckStatus e : WmsStockCheckStatus.values()) {
            if(e.getLabel().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        return null;
    }

    public boolean matchAny(WmsStockCheckStatus... status) {
        for (WmsStockCheckStatus s : status) {
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
