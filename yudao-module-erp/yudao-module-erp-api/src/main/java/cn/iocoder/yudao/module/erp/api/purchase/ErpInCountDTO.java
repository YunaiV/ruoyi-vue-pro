package cn.iocoder.yudao.module.erp.api.purchase;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ErpInCountDTO {

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
