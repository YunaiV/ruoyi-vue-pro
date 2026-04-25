package cn.iocoder.yudao.module.trade.controller.app.wholesale.wallet.vo;

import cn.iocoder.yudao.module.trade.service.wholesale.wallet.bo.WholesaleWalletPayResultBO;
import cn.iocoder.yudao.module.trade.service.wholesale.wallet.bo.WholesaleWalletRechargeResultBO;
import cn.iocoder.yudao.module.trade.service.wholesale.wallet.bo.WholesaleWalletSummaryBO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户 App - 批发商钱包 Response VO
 *
 * @author deepay
 */
@Schema(description = "用户 App - 批发商钱包 Response VO")
@Data
public class AppWholesaleWalletRespVO {

    @Schema(description = "钱包摘要（查询余额时返回）")
    private WholesaleWalletSummaryBO summary;

    @Schema(description = "直接扣款结果（钱包支付时返回）")
    private WholesaleWalletPayResultBO deductResult;

    @Schema(description = "充值支付单信息（发起充值时返回）")
    private WholesaleWalletRechargeResultBO rechargeResult;

}
