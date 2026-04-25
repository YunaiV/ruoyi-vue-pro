package cn.iocoder.yudao.module.trade.controller.app.wholesale.wallet.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 用户 App - 批发商钱包直接扣款 Request VO
 *
 * @author deepay
 */
@Schema(description = "用户 App - 批发商钱包直接扣款 Request VO")
@Data
public class AppWholesaleWalletPayReqVO {

    @Schema(description = "商户订单号（去重用）",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "WH_1001_1715000000000")
    @NotBlank(message = "商户订单号不能为空")
    private String merchantOrderId;

    @Schema(description = "支付金额（分）",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "1000000")
    @NotNull(message = "支付金额不能为空")
    @Min(value = 1, message = "支付金额必须大于 0")
    private Integer priceInFen;

}
