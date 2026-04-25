package cn.iocoder.yudao.module.pay.service.blockchain.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单存证数据 DTO
 *
 * @author deepay
 */
@Data
public class OrderProofDTO {

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 货币代码
     */
    private String currency;

    /**
     * 支付时间（ISO 字符串格式，保证哈希确定性）
     */
    private String paymentTime;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 商品信息摘要
     */
    private String productInfo;

    /**
     * 渠道流水号
     */
    private String transactionId;

}
