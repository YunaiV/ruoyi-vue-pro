package cn.iocoder.yudao.module.srm.config.machine.inItem;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SrmPurchaseInItemCountContext {

    //到货项ID
    private Long arriveItemId;

    //到货项差额
    private BigDecimal inCount;

    //退货项差额
    private BigDecimal outCount;
}
