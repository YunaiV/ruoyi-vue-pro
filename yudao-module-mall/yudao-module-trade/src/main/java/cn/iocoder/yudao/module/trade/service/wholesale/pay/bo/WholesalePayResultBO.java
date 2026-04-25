package cn.iocoder.yudao.module.trade.service.wholesale.pay.bo;

import lombok.Data;

/**
 * 批发订单支付结果 BO
 *
 * @author deepay
 */
@Data
public class WholesalePayResultBO {

    /** 支付单编号（来自 PayOrderApi） */
    private Long payOrderId;

    /** 商户订单号 */
    private String merchantOrderId;

    /** 支付金额（分） */
    private Integer price;

    /** 所用支付渠道：wallet / alipay / wechat_pay / bank_transfer */
    private String channelUsed;

    /** 钱包余额（分，wallet 支付时返回） */
    private Integer walletBalance;

    /** 钱包是否余额充足 */
    private Boolean walletSufficient;

    /** 支付过期时间（ISO） */
    private String expireTime;

    /** 支付状态：WAITING / SUCCESS / CLOSED */
    private String status;

    /** 附加说明 */
    private String message;

}
