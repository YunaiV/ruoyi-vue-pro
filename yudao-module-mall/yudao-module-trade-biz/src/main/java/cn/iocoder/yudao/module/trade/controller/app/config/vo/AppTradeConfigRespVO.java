package cn.iocoder.yudao.module.trade.controller.app.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 交易配置 Response VO")
@Data
public class AppTradeConfigRespVO {

    // ========== 售后相关 ==========

    @Schema(description = "售后的退款理由", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> afterSaleRefundReasons;

    @Schema(description = "售后的退货理由", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> afterSaleReturnReasons;

    // ========== 分销相关 ==========

    @Schema(description = "分销海报地址数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> brokeragePosterUrls;

    @Schema(description = "佣金冻结时间（天）", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer brokerageFrozenDays;

    @Schema(description = "佣金提现最小金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer brokerageWithdrawMinPrice;

    @Schema(description = "提现方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2]")
    private List<Integer> brokerageWithdrawTypes;

}
