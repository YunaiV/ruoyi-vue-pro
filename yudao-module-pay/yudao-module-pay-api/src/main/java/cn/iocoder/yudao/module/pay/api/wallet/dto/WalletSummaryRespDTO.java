package cn.iocoder.yudao.module.pay.api.wallet.dto;

import lombok.Data;

/**
 * 钱包统计 Response DTO
 *
 * @author owen
 */
@Data
public class WalletSummaryRespDTO {
    /**
     * 总支付金额（余额），单位：分
     */
    private Integer orderWalletPayPrice;

    /**
     * 充值订单数
     */
    private Integer rechargePayCount;
    /**
     * 充值金额，单位：分
     */
    private Integer rechargePayPrice;
    /**
     * 充值退款订单数
     */
    private Integer rechargeRefundCount;
    /**
     * 充值退款金额，单位：分
     */
    private Integer rechargeRefundPrice;

}
