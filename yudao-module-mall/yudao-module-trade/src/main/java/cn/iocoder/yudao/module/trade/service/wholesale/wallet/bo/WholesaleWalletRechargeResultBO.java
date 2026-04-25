package cn.iocoder.yudao.module.trade.service.wholesale.wallet.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 批发商钱包充值（发起充值支付单）结果 BO
 *
 * @author deepay
 */
@Data
public class WholesaleWalletRechargeResultBO {

    @Schema(description = "充值支付单编号（通过 /pay/order/submit 选渠道完成充值）")
    private Long payOrderId;

    @Schema(description = "充值金额（分）")
    private Integer rechargePrice;

    @Schema(description = "过期时间（ISO 8601）")
    private String expireTime;

    @Schema(description = "下一步操作提示")
    private String nextAction;

}
