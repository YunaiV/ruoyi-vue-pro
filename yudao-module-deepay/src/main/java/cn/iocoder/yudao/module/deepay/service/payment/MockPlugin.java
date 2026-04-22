package cn.iocoder.yudao.module.deepay.service.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * MockPlugin — 本地开发 / CI 测试用模拟支付插件（无外部依赖）。
 *
 * <p>当 {@code deepay.payment.provider} 未设为 {@code jeepay} 时自动激活。</p>
 *
 * <pre>
 * # 切换到 Jeepay 真实环境：
 * deepay:
 *   payment:
 *     provider: jeepay
 * </pre>
 */
@Component
@ConditionalOnMissingBean(JeepayPlugin.class)
public class MockPlugin implements PaymentPlugin {

    private static final Logger log = LoggerFactory.getLogger(MockPlugin.class);

    @Override public String getType() { return "mock"; }

    @Override
    public PaymentResp createOrder(PaymentRequest req) {
        String id = "MOCK-" + req.getOutTradeNo() + "-" + System.currentTimeMillis();
        log.info("[MockPlugin] 模拟支付单 id={} amount={}", id, req.getAmount());
        return PaymentResp.ok(req.getOutTradeNo(), id, "/mock/pay?id=" + id);
    }

    @Override
    public boolean verifyCallback(Map<String, String> data) {
        log.debug("[MockPlugin] 跳过签名验证（Mock模式）");
        return true;
    }

    @Override
    public PaymentStatus parseStatus(Map<String, String> data) {
        String state = data.getOrDefault("state", "2");
        return "2".equals(state) ? PaymentStatus.PAID : PaymentStatus.WAIT;
    }

    @Override
    public String extractOutTradeNo(Map<String, String> data) {
        return data.getOrDefault("outTradeNo", data.get("paymentId"));
    }

    @Override
    public BigDecimal extractPaidAmount(Map<String, String> data) {
        String v = data.get("amount");
        if (v == null) return BigDecimal.ZERO;
        try { return new BigDecimal(v); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}
