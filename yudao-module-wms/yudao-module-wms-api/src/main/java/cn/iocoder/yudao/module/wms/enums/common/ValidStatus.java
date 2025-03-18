package cn.iocoder.yudao.module.wms.enums.common;

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
public enum ValidStatus implements ArrayValuable<Integer>, DictEnum {

    INVALID(0, "不可用"),
    VALID(1, "可用");

    public static final Integer[] VALUES = Arrays.stream(values()).map(ValidStatus::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static ValidStatus parse(Integer value) {
        for (ValidStatus e : ValidStatus.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static ValidStatus parse(String nameOrLabel) {
        for (ValidStatus e : ValidStatus.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (ValidStatus e : ValidStatus.values()) {
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
