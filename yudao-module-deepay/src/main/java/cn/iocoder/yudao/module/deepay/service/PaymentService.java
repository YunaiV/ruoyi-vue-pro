package cn.iocoder.yudao.module.deepay.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 统一支付层接口 — 支付中台（Phase 10）。
 *
 * <p>所有 Agent / Controller 只依赖此接口，不直接调用 Jeepay / Stripe / 支付宝 API。
 * 切换支付渠道只需替换实现类，FinanceAgent / OrderFlowAgent 无需改动。</p>
 *
 * <h3>扩展路线</h3>
 * <ul>
 *   <li>Jeepay（微信/支付宝聚合）→ JeepayPaymentServiceImpl</li>
 *   <li>Stripe（欧美）→ StripePaymentServiceImpl</li>
 *   <li>PayPal → PayPalPaymentServiceImpl</li>
 * </ul>
 */
public interface PaymentService {

    /**
     * 创建支付单。
     *
     * @param outTradeNo 系统侧唯一订单号（通常为 orderId 或 chainCode+ts）
     * @param amount     支付金额（元，接口内部转换为"分"）
     * @param subject    商品标题
     * @param notifyUrl  异步回调地址
     * @return 支付渠道返回的 paymentId / payUrl / prepayId（由实现决定格式）
     */
    String createPayment(String outTradeNo, BigDecimal amount, String subject, String notifyUrl);

    /**
     * 验证回调签名。
     *
     * @param params 回调参数（全部字段，含 sign）
     * @return true=签名有效；false=伪造请求，应拒绝
     */
    boolean verifyCallback(Map<String, String> params);

    /**
     * 从回调参数中提取我方订单号（outTradeNo）。
     *
     * @param params 回调参数
     * @return 我方订单号
     */
    String extractOutTradeNo(Map<String, String> params);

    /**
     * 从回调参数中提取实收金额（元）。
     *
     * @param params 回调参数
     * @return 实收金额
     */
    BigDecimal extractPaidAmount(Map<String, String> params);
}
