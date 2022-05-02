package cn.iocoder.yudao.module.pay.service.notify.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel(value = "退款单的通知 Request VO", description = "业务方接入退款回调时，使用该 VO 对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayRefundOrderReqVO {

    @ApiModelProperty(value = "商户退款单编号", required = true, example = "10")
    @NotEmpty(message = "商户退款单编号不能为空")
    private String merchantOrderId;

    @ApiModelProperty(value = "支付退款编号", required = true, example = "20")
    @NotNull(message = "支付退款编号不能为空")
    private Long payRefundId;

    @ApiModelProperty(value = "退款状态(成功，失败)", required = true, example = "10")
    private Integer status;

}
