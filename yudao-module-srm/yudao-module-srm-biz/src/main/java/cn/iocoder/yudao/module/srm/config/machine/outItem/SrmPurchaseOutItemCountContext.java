package cn.iocoder.yudao.module.srm.config.machine.outItem;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SrmPurchaseOutItemCountContext {
    //到货项ID
    private Long outItemId;

    //到货项差额
    private BigDecimal inCount;

    //退货项差额
    private BigDecimal outCount;
}
