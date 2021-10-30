package cn.iocoder.yudao.framework.extension.pay.domain;

import lombok.*;

import java.io.Serializable;

/**
 * @description 下单: 预支付交易单返回结果
 * @author Qingchen
 * @version 1.0.0
 * @date 2021-08-30 10:43
 * @class cn.iocoder.yudao.framework.extension.pay.domain.TransactionsResult.java
 */
@Data
@AllArgsConstructor
public class TransactionsResult implements Serializable {

    /**
     * 预支付交易会话标识
     */
    private String prepayId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 系统内部支付单号
     */
    private String paymentNo;

    /**
     * 支付渠道：微信 or 支付宝
     */
    private String channel;


}
