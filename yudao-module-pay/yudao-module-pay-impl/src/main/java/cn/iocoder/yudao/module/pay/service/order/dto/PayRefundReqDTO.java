package cn.iocoder.yudao.module.pay.service.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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
     * 支付订单编号
     */
    @NotNull(message = "支付订单编号不能为空")
    private Long payOrderId;

    /**
     * 退款金额
     */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "退款金额必须大于零")
    private Long amount;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * 商户退款订单号
     */
    @NotEmpty(message = "商户退款订单号不能为空")
    private String merchantRefundId;

    /**
     * 用户 IP
     */
    private String userIp;
}
