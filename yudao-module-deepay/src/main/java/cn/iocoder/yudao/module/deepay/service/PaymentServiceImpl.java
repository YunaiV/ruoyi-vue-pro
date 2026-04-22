package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.service.payment.PaymentPlugin;
import cn.iocoder.yudao.module.deepay.service.payment.PaymentPluginManager;
import cn.iocoder.yudao.module.deepay.service.payment.PaymentRequest;
import cn.iocoder.yudao.module.deepay.service.payment.PaymentResp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

/**
 * PaymentServiceImpl — PaymentService 实现，委托给 PaymentPluginManager。
 *
 * <h3>配置驱动切换（零代码修改）</h3>
 * <pre>
 * deepay:
 *   payment:
 *     provider: jeepay   # 改为 stripe / mock 即切换渠道
 * </pre>
 */
@Service("paymentServiceV2")
public class PaymentServiceImpl implements PaymentServiceV2 {

    @Value("${deepay.payment.provider:mock}")
    private String provider;

    @Resource
    private PaymentPluginManager pluginManager;

    @Override
    public PaymentResp create(String outTradeNo, BigDecimal amount,
                               String subject, String notifyUrl) {
        return plugin().createOrder(new PaymentRequest(outTradeNo, amount, subject, notifyUrl));
    }

    @Override
    public boolean verify(Map<String, String> data) {
        return plugin().verifyCallback(data);
    }

    @Override
    public PaymentPlugin.PaymentStatus parseStatus(Map<String, String> data) {
        return plugin().parseStatus(data);
    }

    @Override
    public String extractOutTradeNo(Map<String, String> data) {
        return plugin().extractOutTradeNo(data);
    }

    @Override
    public BigDecimal extractPaidAmount(Map<String, String> data) {
        return plugin().extractPaidAmount(data);
    }

    // ----------------------------------------------------------------
    private PaymentPlugin plugin() {
        PaymentPlugin p = pluginManager.get(provider);
        return p != null ? p : pluginManager.getDefault();
    }
}
