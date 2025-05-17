package cn.iocoder.yudao.module.pay.controller.app.wallet.vo.recharge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 用户充值套餐 Response VO")
@Data
public class AppPayWalletPackageRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;
    @Schema(description = "套餐名", requiredMode = Schema.RequiredMode.REQUIRED, example = "小套餐")
    private String name;

    @Schema(description = "支付金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer payPrice;
    @Schema(description = "赠送金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer bonusPrice;

}
