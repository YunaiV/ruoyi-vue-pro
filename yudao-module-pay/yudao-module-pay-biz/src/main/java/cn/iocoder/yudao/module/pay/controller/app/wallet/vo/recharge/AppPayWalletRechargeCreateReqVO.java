package cn.iocoder.yudao.module.pay.controller.app.wallet.vo.recharge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

@Schema(description = "用户 APP - 创建钱包充值 Request VO")
@Data
public class AppPayWalletRechargeCreateReqVO {

    @Schema(description = "支付金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "支付金额必须大于零")
    private Integer payPrice;

    @Schema(description = "钱包赠送金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    @NotNull(message = "钱包赠送金额不能为空")
    @DecimalMin(value = "0",  message = "钱包赠送金额必须大于等于零")
    private Integer walletBonus;
}
