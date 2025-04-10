package cn.iocoder.yudao.module.srm.api.purchase;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 入库单 付款金额调整DTO
 */
@Data
@Builder
public class SrmPayCountDTO {
    private Long orderItemId;
    //支付金额差异
    private BigDecimal payCountDiff;
}
