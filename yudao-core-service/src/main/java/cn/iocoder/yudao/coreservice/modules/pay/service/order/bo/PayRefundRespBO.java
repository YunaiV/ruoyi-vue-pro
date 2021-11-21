package cn.iocoder.yudao.coreservice.modules.pay.service.order.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 退款申请单 Response DTO
 */
@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayRefundRespBO {

    /**
     * 支付退款单编号， 自增
     */
    private Long refundId;
}
