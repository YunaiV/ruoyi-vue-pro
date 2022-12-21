package cn.iocoder.yudao.module.pay.service.notify.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Schema(description = "支付单的通知 Request VO,业务方接入支付回调时，使用该 VO 对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayNotifyOrderReqVO {

    @Schema(description = "商户订单编号", required = true, example = "10")
    @NotEmpty(message = "商户订单号不能为空")
    private String merchantOrderId;

    @Schema(description = "支付订单编号", required = true, example = "20")
    @NotNull(message = "支付订单编号不能为空")
    private Long payOrderId;

}
