package cn.iocoder.yudao.module.wms.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * WMS 入库单类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum WmsReceiptOrderTypeEnum implements ArrayValuable<Integer> {

    PRODUCTION(WmsOrderTypeConstants.RECEIPT_PRODUCTION, "生产入库"),
    PURCHASE(WmsOrderTypeConstants.RECEIPT_PURCHASE, "采购入库"),
    RETURN(WmsOrderTypeConstants.RECEIPT_RETURN, "退货入库"),
    GIVE_BACK(WmsOrderTypeConstants.RECEIPT_GIVE_BACK, "归还入库");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(WmsReceiptOrderTypeEnum::getType).toArray(Integer[]::new);

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
