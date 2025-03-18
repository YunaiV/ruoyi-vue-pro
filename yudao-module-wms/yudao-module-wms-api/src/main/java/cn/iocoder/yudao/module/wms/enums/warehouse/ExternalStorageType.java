package cn.iocoder.yudao.module.wms.enums.warehouse;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.enums.DictEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 外部仓类型
 **/
@RequiredArgsConstructor
@Getter
public enum ExternalStorageType implements ArrayValuable<Integer>, DictEnum {

    THIRD_PARTY(1, "三方仓"),
    PLATFORM(2, "平台仓");

    public static final Integer[] VALUES = Arrays.stream(values()).map(ExternalStorageType::getValue).toArray(Integer[]::new);


    private final Integer value;
    private final String label;

    /**
     * 按 value 匹配枚举，name 优先
     **/
    public static ExternalStorageType parse(Integer value) {
        for (ExternalStorageType e : ExternalStorageType.values()) {
            if(e.getValue().equals(value)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 按 name 或 label 匹配枚举，name 优先
     **/
    public static ExternalStorageType parse(String nameOrLabel) {
        for (ExternalStorageType e : ExternalStorageType.values()) {
            if(e.name().equalsIgnoreCase(nameOrLabel)) {
                return e;
            }
        }
        for (ExternalStorageType e : ExternalStorageType.values()) {
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
