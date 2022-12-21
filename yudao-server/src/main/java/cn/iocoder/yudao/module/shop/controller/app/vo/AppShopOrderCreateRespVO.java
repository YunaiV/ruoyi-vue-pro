package cn.iocoder.yudao.module.shop.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Schema(description = "用户 APP - 商城订单创建 Response VO")
@Data
@Builder
@AllArgsConstructor
public class AppShopOrderCreateRespVO {

    @Schema(description = "商城订单编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "支付订单编号", required = true, example = "2048")
    private Long payOrderId;

}
