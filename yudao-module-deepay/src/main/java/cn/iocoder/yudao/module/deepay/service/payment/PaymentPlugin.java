package cn.iocoder.yudao.module.deepay.service.payment;

import java.math.BigDecimal;
import java.util.Map;

/**
 * PaymentPlugin — 支付渠道插件接口（Plugin Layer）。
 *
 * <p>每个支付渠道实现此接口。系统通过 {@link PaymentPluginManager} 按类型路由。
 * 业务层（FinanceAgent / OrderFlowAgent）只依赖 {@code PaymentService}，完全不知道 Jeepay 存在。</p>
 *
 * <pre>
 * Orchestrator
 *   └─ OrderFlowAgent
 *        └─ PaymentService（统一入口）
 *             └─ PaymentPluginManager
 *                  ├─ JeepayPlugin   ← deepay.payment.provider=jeepay
 *                  ├─ StripePlugin   ← 未来扩展
 *                  └─ MockPlugin     ← 默认 / 测试
 * </pre>
 */
public interface PaymentPlugin {

    /**
     * 插件类型标识，对应 {@code deepay.payment.provider} 配置值。
     * 例：jeepay / stripe / paypal / mock
     */
    String getType();

    /**
     * 向支付网关创建支付单。
     *
     * @param req 支付请求（outTradeNo / amount元 / subject / notifyUrl）
     * @return {@link PaymentResp}（paymentId + payUrl）
     */
    PaymentResp createOrder(PaymentRequest req);

    /**
     * 验证回调签名。
     *
     * @param data 回调全部参数（含 sign 字段）
     * @return true=合法；false=拒绝
     */
    boolean verifyCallback(Map<String, String> data);

    /**
     * 解析回调支付状态。
     *
     * @param data 回调参数
     * @return {@link PaymentStatus}
     */
    PaymentStatus parseStatus(Map<String, String> data);

    /**
     * 从回调参数提取我方订单号（即 outTradeNo）。
     */
    String extractOutTradeNo(Map<String, String> data);

    /**
     * 从回调参数提取实收金额（元）。
     */
    BigDecimal extractPaidAmount(Map<String, String> data);

    // ================================================================
    // 内嵌枚举 / VO
    // ================================================================

    enum PaymentStatus {
        /** 支付成功（Jeepay state=2） */  PAID,
        /** 等待支付（Jeepay state=1） */  WAIT,
        /** 支付失败（Jeepay state=3） */  FAIL,
        /** 状态未知 */                    UNKNOWN
    }
}
