package cn.iocoder.yudao.module.srm.config.machine;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购订单-入库数量变动DTO，inCount变动数量属性。
 */
@Data
@Builder
public class SrmOrderInCountContext {

    //订单项
    private Long orderItemId;
    //入库差额
    private BigDecimal inCount;


    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //申请项->申请单下的子项
    private Long applyItemId;

    //退货差额
    private BigDecimal returnCount;
}
