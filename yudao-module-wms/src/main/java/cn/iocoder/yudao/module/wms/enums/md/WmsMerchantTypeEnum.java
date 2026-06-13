package cn.iocoder.yudao.module.wms.enums.md;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * WMS 往来企业类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum WmsMerchantTypeEnum implements ArrayValuable<Integer> {

    CUSTOMER(1, "客户"),
    SUPPLIER(2, "供应商"),
    CUSTOMER_SUPPLIER(3, "客户/供应商");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(WmsMerchantTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
