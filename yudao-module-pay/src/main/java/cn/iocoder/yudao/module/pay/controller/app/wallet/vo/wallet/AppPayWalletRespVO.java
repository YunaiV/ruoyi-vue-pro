package cn.iocoder.yudao.module.pay.controller.app.wallet.vo.wallet;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 用户钱包 Response VO")
@Data
public class AppPayWalletRespVO {

    @Schema(description = "钱包余额，单位分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer balance;

    @Schema(description = "累计支出，单位分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private Integer totalExpense;

    @Schema(description = "累计充值，单位分", requiredMode = Schema.RequiredMode.REQUIRED, example = "2000")
    private Integer totalRecharge;

}
