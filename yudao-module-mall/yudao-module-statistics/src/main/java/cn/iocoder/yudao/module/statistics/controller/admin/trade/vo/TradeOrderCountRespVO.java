package cn.iocoder.yudao.module.statistics.controller.admin.trade.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 交易订单数量 Response VO")
@Data
public class TradeOrderCountRespVO {

    @Schema(description = "待发货", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long undelivered;

    @Schema(description = "待核销", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long pickUp;

    @Schema(description = "退款中", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long afterSaleApply;

    @Schema(description = "提现待审核", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long auditingWithdraw;

}
