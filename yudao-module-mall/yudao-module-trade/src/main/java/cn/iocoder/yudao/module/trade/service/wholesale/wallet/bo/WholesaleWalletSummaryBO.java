package cn.iocoder.yudao.module.trade.service.wholesale.wallet.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 批发商钱包摘要 BO
 *
 * @author deepay
 */
@Data
public class WholesaleWalletSummaryBO {

    @Schema(description = "钱包编号")
    private Long walletId;

    @Schema(description = "余额（分）")
    private Integer balance;

    @Schema(description = "余额（元，两位小数）")
    private String balanceYuan;

    @Schema(description = "累计充值（分）")
    private Integer totalRecharge;

    @Schema(description = "累计消费（分）")
    private Integer totalExpense;

    @Schema(description = "冻结金额（分）")
    private Integer freezePrice;

    @Schema(description = "用户编号")
    private Long userId;

}
