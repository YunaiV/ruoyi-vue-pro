package cn.iocoder.yudao.module.pay.controller.app.refund.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@ApiModel("用户 APP - 提交退款订单 Response VO")
@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppPayRefundRespVO {

    @ApiModelProperty(value = "退款订单编号", required = true, example = "10")
    private Long refundId;

}
