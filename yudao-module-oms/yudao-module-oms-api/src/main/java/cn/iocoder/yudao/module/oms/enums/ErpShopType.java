package cn.iocoder.yudao.module.oms.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Getter
public enum ErpShopType  implements ArrayValuable<Integer> {

    ONLINE("0", "线上"),
    PHYSICAL("1", "线下");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ErpShopType::getCode).toArray(Integer[]::new);

    /**
     * 存储状态码和描述的字段
     **/
    private final String code;
    private final String description;

    /**
     * 获取状态码
     **/
    public String getCode() {
        return code;
    }

    /**
     * 获取状态描述
     **/
    public String getDescription() {
        return description;
    }

    /**
     * 根据状态码获取状态枚举
     **/
    public static ErpShopType fromCode(Integer code) {
        if(code==null) {
            return null;
        }
        for (ErpShopType shopType : ErpShopType.values()) {
            if(Objects.equals(shopType.code,code)) {
                return shopType;
            }
        }
        return null;
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }


}
