package cn.iocoder.yudao.module.shop.controller.app.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@ApiModel("用户 APP - 商城订单创建 Response VO")
@Data
@Builder
@AllArgsConstructor
public class AppShopOrderCreateRespVO {

    @ApiModelProperty(value = "商城订单编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "支付订单编号", required = true, example = "2048")
    private Long payOrderId;

}
