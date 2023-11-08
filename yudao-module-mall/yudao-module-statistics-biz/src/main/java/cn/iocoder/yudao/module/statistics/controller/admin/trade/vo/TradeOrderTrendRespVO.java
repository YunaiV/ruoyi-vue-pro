package cn.iocoder.yudao.module.statistics.controller.admin.trade.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 订单量趋势统计 Response VO")
@Data
public class TradeOrderTrendRespVO {

    @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String date;

    @Schema(description = "订单数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer orderPayCount;

    @Schema(description = "订单支付金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer orderPayPrice;

}
