package cn.iocoder.yudao.module.pay.service.notify.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel(value = "支付单的通知 Request VO", description = "业务方接入支付回调时，使用该 VO 对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayNotifyOrderReqVO {

    @ApiModelProperty(value = "商户订单编号", required = true, example = "10")
    @NotEmpty(message = "商户订单号不能为空")
    private String merchantOrderId;

    @ApiModelProperty(value = "支付订单编号", required = true, example = "20")
    @NotNull(message = "支付订单编号不能为空")
    private Long payOrderId;

}
