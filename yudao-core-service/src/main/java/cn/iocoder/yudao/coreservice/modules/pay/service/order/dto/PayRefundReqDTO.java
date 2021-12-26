package cn.iocoder.yudao.coreservice.modules.pay.service.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 退款申请单 Request DTO
 */
@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayRefundReqDTO {

    /**
     * 支付订单编号自增
     */
    private Long payOrderId;

    /**
     * 退款金额
     */
    private Long amount;

    /**
     * 退款原因
     */
    private String reason;


    /**
     * 商户退款订单号
     */
    private String merchantRefundNo;

    /**
     * 用户 IP
     */
    private String userIp;
}
