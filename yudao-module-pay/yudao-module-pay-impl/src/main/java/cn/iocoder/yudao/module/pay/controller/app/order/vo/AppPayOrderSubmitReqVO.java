package cn.iocoder.yudao.module.pay.controller.app.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@ApiModel("用户 APP - 支付订单提交 Request VO")
@Data
@Accessors(chain = true)
public class AppPayOrderSubmitReqVO {

    @ApiModelProperty(value = "支付单编号", required = true, example = "1024")
    @NotNull(message = "支付单编号不能为空")
    private Long id;

    @ApiModelProperty(value = "支付渠道", required = true, example = "wx_pub")
    @NotEmpty(message = "支付渠道不能为空")
    private String channelCode;

    @ApiModelProperty(value = "支付渠道的额外参数", notes = "例如说，微信公众号需要传递 openid 参数")
    private Map<String, String> channelExtras;

}
