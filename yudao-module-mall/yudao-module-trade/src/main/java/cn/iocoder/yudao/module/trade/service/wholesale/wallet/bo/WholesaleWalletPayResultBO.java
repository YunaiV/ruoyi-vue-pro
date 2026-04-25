package cn.iocoder.yudao.module.trade.service.wholesale.wallet.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 批发商钱包直接扣款结果 BO
 *
 * @author deepay
 */
@Data
public class WholesaleWalletPayResultBO {

    @Schema(description = "是否成功")
    private boolean success;

    @Schema(description = "扣款前余额（分）")
    private Integer balanceBefore;

    @Schema(description = "扣款后余额（分）")
    private Integer balanceAfter;

    @Schema(description = "扣款金额（分）")
    private Integer deductedPrice;

    @Schema(description = "业务单号（merchantOrderId）")
    private String merchantOrderId;

    @Schema(description = "失败原因（success=false 时有值）")
    private String failReason;

    @Schema(description = "提示消息")
    private String message;

}
