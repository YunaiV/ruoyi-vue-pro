package cn.iocoder.yudao.module.statistics.controller.admin.trade.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 交易统计 Response VO")
@Data
public class TradeSummaryRespVO {

    @Schema(description = "昨日订单数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer yesterdayOrderCount;
    @Schema(description = "昨日支付金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer yesterdayPayPrice;

    @Schema(description = "本月订单数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer monthOrderCount;
    @Schema(description = "本月支付金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer monthPayPrice;

}
