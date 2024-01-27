package cn.iocoder.yudao.module.erp.enums.sale;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * ERP 销售订单的状态枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum ErpSaleOrderStatusEnum implements IntArrayValuable {

    AUDIT_NONE(0, "未审核"),
    AUDIT_PASS(10, "已审核"),
    SALE_PART(20, "部分销售"),
    SALE_ALL(21, "完成销售"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(ErpSaleOrderStatusEnum::getStatus).toArray();

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
