package cn.iocoder.yudao.module.statistics.controller.admin.trade.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 交易状况统计 Response VO")
@Data
public class TradeTrendSummaryRespVO {

    @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023-12-16")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate date;

    @Schema(description = "营业额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer turnoverPrice; // 营业额 = 商品支付金额 + 充值金额

    @Schema(description = "订单支付金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer orderPayPrice;

    @Schema(description = "余额支付金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer walletPayPrice;

    @Schema(description = "订单退款金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer afterSaleRefundPrice;

    @Schema(description = "支付佣金金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer brokerageSettlementPrice;

    @Schema(description = "充值金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer rechargePrice;

    @Schema(description = "支出金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer expensePrice; // 余额支付金额 + 支付佣金金额 + 商品退款金额

}
