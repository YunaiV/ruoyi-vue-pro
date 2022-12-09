package cn.iocoder.yudao.module.pay.service.notify.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Schema(title = "退款单的通知 Request VO", description = "业务方接入退款回调时，使用该 VO 对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayRefundOrderReqVO {

    @Schema(title = "商户退款单编号", required = true, example = "10")
    @NotEmpty(message = "商户退款单编号不能为空")
    private String merchantOrderId;

    @Schema(title = "支付退款编号", required = true, example = "20")
    @NotNull(message = "支付退款编号不能为空")
    private Long payRefundId;

    @Schema(title = "退款状态(成功，失败)", required = true, example = "10")
    private Integer status;

}
