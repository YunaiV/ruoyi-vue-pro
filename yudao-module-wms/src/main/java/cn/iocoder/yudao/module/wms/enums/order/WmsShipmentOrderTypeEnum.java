package cn.iocoder.yudao.module.wms.enums.order;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * WMS 出库单类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum WmsShipmentOrderTypeEnum implements ArrayValuable<Integer> {

    RETURN(WmsOrderTypeConstants.SHIPMENT_RETURN, "退货出库"),
    SALE(WmsOrderTypeConstants.SHIPMENT_SALE, "销售出库"),
    PRODUCTION(WmsOrderTypeConstants.SHIPMENT_PRODUCTION, "生产出库");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(WmsShipmentOrderTypeEnum::getType).toArray(Integer[]::new);

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
