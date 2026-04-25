package cn.iocoder.yudao.module.trade.controller.app.wholesale.wallet.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 用户 App - 批发商钱包充值发起 Request VO
 *
 * @author deepay
 */
@Schema(description = "用户 App - 批发商钱包充值发起 Request VO")
@Data
public class AppWholesaleWalletRechargeReqVO {

    @Schema(description = "充值金额（元，整数）",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "500")
    @NotNull(message = "充值金额不能为空")
    @Min(value = 1, message = "充值金额至少 1 元")
    private Integer rechargeYuan;

    @Schema(description = "用户 IP（选填，网关可自动获取）", example = "192.168.1.100")
    private String userIp;

}
