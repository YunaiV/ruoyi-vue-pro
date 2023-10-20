package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 交易订单统计 Response VO")
@Data
public class TradeOrderSummaryRespVO {

    @Schema(description = "订单数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long orderCount;

    @Schema(description = "订单金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long orderPayPrice;

    @Schema(description = "退款单数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long afterSaleCount;

    @Schema(description = "退款金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long afterSalePrice;

}
