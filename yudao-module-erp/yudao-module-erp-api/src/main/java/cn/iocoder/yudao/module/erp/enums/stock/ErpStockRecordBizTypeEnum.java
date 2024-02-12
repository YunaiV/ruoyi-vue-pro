package cn.iocoder.yudao.module.erp.enums.stock;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * ERP 库存明细 - 业务类型枚举
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum ErpStockRecordBizTypeEnum implements IntArrayValuable {

    OTHER_IN(10, "其它入库"),
    OTHER_IN_CANCEL(11, "其它入库（作废）"),

    OTHER_OUT(20, "其它出库"),
    OTHER_OUT_CANCEL(21, "其它出库（作废）"),

    MOVE_IN(30, "调拨入库"),
    MOVE_IN_CANCEL(31, "调拨入库（作废）"),
    MOVE_OUT(32, "调拨出库"),
    MOVE_OUT_CANCEL(33, "调拨出库（作废）"),

    CHECK_MORE_IN(40, "盘盈入库"),
    CHECK_MORE_IN_CANCEL(41, "盘盈入库（作废）"),
    CHECK_LESS_OUT(42, "盘亏出库"),
    CHECK_LESS_OUT_CANCEL(43, "盘亏出库（作废）"),

    SALE_OUT(50, "销售出库"),
    SALE_OUT_CANCEL(51, "销售出库（作废）"),

    SALE_RETURN(60, "销售退货入库"),
    SALE_RETURN_CANCEL(61, "销售退货入库（作废）"),

    PURCHASE_IN(70, "采购入库"),
    PURCHASE_IN_CANCEL(71, "采购入库（作废）"),

    PURCHASE_RETURN(80, "采购退货出库"),
    PURCHASE_RETURN_CANCEL(81, "采购退货出库（作废）"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(ErpStockRecordBizTypeEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
