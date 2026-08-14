package cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - FMS 试算平衡 Response VO")
@Data
public class FmsTrialBalanceRespVO {

    @Schema(description = "期初借方金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000.00")
    private BigDecimal openingDebitAmount;

    @Schema(description = "期初贷方金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000.00")
    private BigDecimal openingCreditAmount;

    @Schema(description = "期初差额", requiredMode = Schema.RequiredMode.REQUIRED, example = "0.00")
    private BigDecimal openingDifferenceAmount;

    @Schema(description = "本年累计借方金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal yearDebitAmount;

    @Schema(description = "本年累计贷方金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal yearCreditAmount;

    @Schema(description = "本年累计差额", requiredMode = Schema.RequiredMode.REQUIRED, example = "0.00")
    private BigDecimal yearDifferenceAmount;

    @Schema(description = "是否平衡", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean balanced;

}
