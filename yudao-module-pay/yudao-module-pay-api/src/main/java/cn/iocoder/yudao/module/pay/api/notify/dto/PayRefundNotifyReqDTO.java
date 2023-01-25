package cn.iocoder.yudao.module.pay.api.notify.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 退款单的通知 Request DTO
 *
 * @author 芋道源码
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayRefundNotifyReqDTO {

    /**
     * 商户退款单编号
     */
    @NotEmpty(message = "商户退款单编号不能为空")
    private String merchantOrderId;

    /**
     * 支付退款编号
     */
    @NotNull(message = "支付退款编号不能为空")
    private Long payRefundId;

    /**
     * 退款状态
     *
     * (成功，失败) TODO 芋艿：枚举
     */
    @NotNull(message = "退款状态不能为空")
    private Integer status;

}
