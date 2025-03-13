package cn.iocoder.yudao.module.erp.api.purchase;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 入库单 付款金额调整
 */
@Data
@Builder
public class ErpInPayCountDTO {
    private Long InItemId;
    private BigDecimal payCount;
}
