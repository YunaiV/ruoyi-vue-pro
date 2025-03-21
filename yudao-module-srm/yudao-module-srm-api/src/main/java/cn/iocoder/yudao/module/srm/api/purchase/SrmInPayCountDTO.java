package cn.iocoder.yudao.module.srm.api.purchase;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 入库单 付款金额调整
 */
@Data
@Builder
public class SrmInPayCountDTO {
    private Long InItemId;
    private BigDecimal payCount;
}
