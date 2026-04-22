package cn.iocoder.yudao.module.deepay.service.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PaymentPluginManager — 支付插件注册中心。
 *
 * <p>Spring 自动注入所有 {@link PaymentPlugin} 实现，按 {@link PaymentPlugin#getType()} 注册。
 * 业务层通过 {@link #get(String)} 获取对应插件，无需关心 Bean 名称。</p>
 *
 * <pre>
 * 示例：
 *   PaymentPlugin jeepay = manager.get("jeepay");
 *   PaymentPlugin mock   = manager.get("mock");
 * </pre>
 */
@Component
public class PaymentPluginManager {

    private static final Logger log = LoggerFactory.getLogger(PaymentPluginManager.class);

    private final Map<String, PaymentPlugin> registry = new ConcurrentHashMap<>();

    /**
     * Spring 自动注入所有 {@link PaymentPlugin} Bean（JeepayPlugin / MockPlugin / StripePlugin…）。
     */
    @Resource
    public void setPlugins(List<PaymentPlugin> plugins) {
        for (PaymentPlugin p : plugins) {
            registry.put(p.getType(), p);
            log.info("[PaymentPluginManager] 已注册插件 type={} class={}",
                    p.getType(), p.getClass().getSimpleName());
        }
    }

    /**
     * 按类型获取插件。
     *
     * @param type 插件类型（jeepay / mock / stripe / paypal）
     * @return 插件实例，找不到返回 null
     */
    public PaymentPlugin get(String type) {
        return registry.get(type);
    }

    /**
     * 获取第一个可用插件（兜底，避免 NPE）。
     */
    public PaymentPlugin getDefault() {
        if (registry.isEmpty()) throw new IllegalStateException("无可用 PaymentPlugin");
        // 优先 jeepay，其次任意
        PaymentPlugin jeepay = registry.get("jeepay");
        return jeepay != null ? jeepay : registry.values().iterator().next();
    }
}
