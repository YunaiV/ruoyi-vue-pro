package cn.iocoder.yudao.module.erp.enums.common;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * ERP 业务类型枚举
 *
 * @author HUIHUI
 */
@RequiredArgsConstructor
@Getter
public enum ErpBizTypeEnum implements ArrayValuable<Integer> {

    PURCHASE_ORDER(10, "采购订单"),
    PURCHASE_IN(11, "采购入库"),
    PURCHASE_RETURN(12, "采购退货"),

    SALE_ORDER(20, "销售订单"),
    SALE_OUT(21, "销售出库"),
    SALE_RETURN(22, "销售退货"),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ErpBizTypeEnum::getType).toArray(Integer[]::new);

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
