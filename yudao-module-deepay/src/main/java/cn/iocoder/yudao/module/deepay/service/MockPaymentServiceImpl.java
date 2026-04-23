package cn.iocoder.yudao.module.deepay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * MockPaymentServiceImpl — 本地开发 / 测试用模拟支付（默认激活）。
 *
 * <p>当 {@code deepay.payment.provider} 未设置或不为 {@code jeepay} 时自动生效。
 * 生产环境设置 {@code deepay.payment.provider=jeepay} 即切换到 Jeepay。</p>
 *
 * <pre>
 * # application.yml
 * deepay:
 *   payment:
 *     provider: jeepay   # mock(默认) | jeepay | stripe | paypal
 * </pre>
 */
@Service
@ConditionalOnMissingBean(name = "jeepayPaymentServiceImpl")
public class MockPaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentServiceImpl.class);

    @Override
    public String createPayment(String outTradeNo, BigDecimal amount,
                                String subject, String notifyUrl) {
        String paymentId = "MOCK-PAY-" + outTradeNo + "-" + System.currentTimeMillis();
        log.info("[MockPayment] 模拟支付单已创建 outTradeNo={} amount={} paymentId={}",
                outTradeNo, amount, paymentId);
        return paymentId;
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        // 本地 Mock 跳过签名校验
        log.debug("[MockPayment] 跳过签名验证（Mock模式）");
        return true;
    }

    @Override
    public String extractOutTradeNo(Map<String, String> params) {
        return params.getOrDefault("outTradeNo", params.get("paymentId"));
    }

    @Override
    public BigDecimal extractPaidAmount(Map<String, String> params) {
        String v = params.get("amount");
        if (v == null) return BigDecimal.ZERO;
        try { return new BigDecimal(v); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}
