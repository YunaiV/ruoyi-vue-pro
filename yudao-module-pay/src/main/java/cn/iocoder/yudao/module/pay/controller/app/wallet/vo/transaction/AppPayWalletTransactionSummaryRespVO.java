package cn.iocoder.yudao.module.pay.controller.app.wallet.vo.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 钱包流水统计 Request VO")
@Data
public class AppPayWalletTransactionSummaryRespVO {

    @Schema(description = "累计支出，单位分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private Integer totalExpense;

    @Schema(description = "累计收入，单位分", requiredMode = Schema.RequiredMode.REQUIRED, example = "2000")
    private Integer totalIncome;

}
