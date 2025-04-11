package cn.iocoder.yudao.module.srm.enums.common;

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
public enum SrmBizTypeEnum implements ArrayValuable<Integer> {

    PURCHASE_ORDER(10, "采购订单"),
    PURCHASE_IN(11, "采购入库"),
    PURCHASE_RETURN(12, "采购退货"),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(SrmBizTypeEnum::getType).toArray(Integer[]::new);

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
