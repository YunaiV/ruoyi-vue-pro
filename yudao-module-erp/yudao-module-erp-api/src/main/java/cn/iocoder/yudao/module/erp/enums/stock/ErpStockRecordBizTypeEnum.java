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
