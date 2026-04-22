package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.service.payment.PaymentPlugin;
import cn.iocoder.yudao.module.deepay.service.payment.PaymentResp;
import cn.iocoder.yudao.module.deepay.service.payment.PaymentRequest;

import java.math.BigDecimal;
import java.util.Map;

/**
 * PaymentService — 统一支付入口（业务层唯一依赖）。
 *
 * <p>业务层（OrderFlowAgent / FinanceAgent / Controller）只依赖此接口，
 * 完全不知道 Jeepay 存在。切换支付渠道只改配置，不改业务代码。</p>
 *
 * <pre>
 * deepay:
 *   payment:
 *     provider: jeepay   # 切换为 stripe 或 mock 不影响任何业务代码
 * </pre>
 */
public interface PaymentServiceV2 {

    /**
     * 创建支付单（带货币参数，用于多币种路由）。
     *
     * @param outTradeNo 我方唯一订单号（payment_id）
     * @param amount     支付金额（展示货币，元）
     * @param subject    商品标题
     * @param notifyUrl  异步回调地址
     * @param currency   展示货币（USD / EUR / CNY …）
     * @return {@link PaymentResp}，含 paymentId 和 payUrl
     */
    PaymentResp create(String outTradeNo, BigDecimal amount, String subject,
                        String notifyUrl, String currency);

    /**
     * 创建支付单（EUR 默认，向后兼容）。
     */
    PaymentResp create(String outTradeNo, BigDecimal amount, String subject, String notifyUrl);

    /**
     * 验证回调签名。
     *
     * @param data 回调全部参数（含 sign）
     * @return true=合法
     */
    boolean verify(Map<String, String> data);

    /**
     * 验证支付回调签名（alias，向后兼容 PaymentReconcileAgent）。
     */
    default boolean verifyCallback(Map<String, String> data) {
        return verify(data);
    }

    /**
     * 解析回调支付状态。
     *
     * @param data 回调参数
     * @return {@link PaymentPlugin.PaymentStatus}
     */
    PaymentPlugin.PaymentStatus parseStatus(Map<String, String> data);

    /**
     * 从回调参数提取我方订单号（outTradeNo）。
     */
    String extractOutTradeNo(Map<String, String> data);

    /**
     * 从回调参数提取实收金额（元）。
     */
    BigDecimal extractPaidAmount(Map<String, String> data);
}
