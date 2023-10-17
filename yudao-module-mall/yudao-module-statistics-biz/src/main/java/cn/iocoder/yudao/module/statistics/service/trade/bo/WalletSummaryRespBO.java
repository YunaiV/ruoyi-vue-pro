package cn.iocoder.yudao.module.statistics.service.trade.bo;

import lombok.Data;

/**
 * 钱包统计 Response DTO
 *
 * @author owen
 */
@Data
public class WalletSummaryRespBO {

    /**
     * 总支付金额（余额），单位：分
     */
    private Integer walletPayPrice;

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
