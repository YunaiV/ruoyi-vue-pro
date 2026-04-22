package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付日志表 deepay_payment_log（Phase 10）。
 *
 * <p>每次回调（含重复回调）都写入一条，用于对账、排查、幂等验证。</p>
 */
@TableName("deepay_payment_log")
@Data
public class DeepayPaymentLogDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 支付渠道订单号（Jeepay payOrderId / 我方 payment_id） */
    private String paymentId;

    /** 关联的 deepay_order.id */
    private String orderId;

    /**
     * 处理状态：
     * PAID         — 成功处理
     * IDEMPOTENT   — 重复回调（已处理）
     * SIGN_FAIL    — 签名验证失败
     * AMOUNT_MISMATCH — 金额不符
     * ORDER_NOT_FOUND — 订单不存在
     * REFUND       — 退款
     */
    private String status;

    /** 实收金额（元） */
    private BigDecimal amount;

    /** 回调原始报文（截取前2000字符） */
    private String callbackRaw;

    private LocalDateTime createdAt;
}
