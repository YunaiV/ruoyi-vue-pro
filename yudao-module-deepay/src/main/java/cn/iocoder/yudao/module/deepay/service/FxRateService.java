package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayFxRateDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayFxRateMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FxRateService — 多货币汇率服务（以 EUR 为基准）。
 *
 * <h3>设计原则</h3>
 * <ol>
 *   <li><b>基准货币 EUR</b>：所有价格内部用 EUR 存储，展示时通过此服务转换</li>
 *   <li><b>不实时调外部</b>：汇率由 {@link FxRateScheduler} 每小时写库，此服务读库缓存</li>
 *   <li><b>三级 Fallback</b>：内存缓存 → 数据库 → 硬编码默认值（保证绝不炸）</li>
 * </ol>
 *
 * <h3>使用</h3>
 * <pre>
 * // 将 €29.90 转换为用户币种
 * BigDecimal displayPrice = fxRateService.convert(new BigDecimal("29.90"), "USD");
 * // → $32.29
 *
 * // 获取 Jeepay 支付金额（分）
 * long amountCents = fxRateService.toPaymentCents(new BigDecimal("29.90"), "USD");
 * // → 3229
 * </pre>
 */
@Service
public class FxRateService {

    private static final Logger log = LoggerFactory.getLogger(FxRateService.class);

    /** EUR = 1（基准，恒不变） */
    public static final String BASE_CURRENCY = "EUR";

    /**
     * 硬编码 Fallback 汇率（当DB无数据时使用）。
     * 保证系统即使汇率源宕机也不会崩溃，但会有轻微偏差。
     */
    private static final Map<String, BigDecimal> FALLBACK_RATES;

    static {
        Map<String, BigDecimal> m = new HashMap<>();
        m.put("EUR", new BigDecimal("1.000000"));
        m.put("USD", new BigDecimal("1.080000"));
        m.put("CNY", new BigDecimal("7.800000"));
        m.put("GBP", new BigDecimal("0.860000"));
        m.put("AED", new BigDecimal("3.970000"));
        m.put("JPY", new BigDecimal("163.000000"));
        m.put("KRW", new BigDecimal("1450.000000"));
        m.put("SGD", new BigDecimal("1.460000"));
        FALLBACK_RATES = Collections.unmodifiableMap(m);
    }

    /** 内存汇率缓存（FxRateScheduler 每小时刷新） */
    private final ConcurrentHashMap<String, BigDecimal> cache = new ConcurrentHashMap<>();

    @Resource
    private DeepayFxRateMapper fxRateMapper;

    // ====================================================================
    // 初始化：从 DB 预热缓存
    // ====================================================================

    @PostConstruct
    public void init() {
        try {
            fxRateMapper.selectList(null).forEach(r -> cache.put(r.getCurrency(), r.getRate()));
            log.info("[FxRateService] 汇率缓存预热完成，共 {} 条", cache.size());
        } catch (Exception e) {
            log.warn("[FxRateService] DB 预热失败，使用 Fallback 汇率", e);
        }
        // 确保 EUR=1 始终在缓存
        cache.putIfAbsent(BASE_CURRENCY, BigDecimal.ONE);
    }

    // ====================================================================
    // 核心 API
    // ====================================================================

    /**
     * 获取 EUR→targetCurrency 汇率（三级 Fallback）。
     *
     * @param targetCurrency 目标货币代码（USD / CNY / GBP …）
     * @return 汇率（1 EUR = ? targetCurrency）
     */
    public BigDecimal getRate(String targetCurrency) {
        if (targetCurrency == null || BASE_CURRENCY.equalsIgnoreCase(targetCurrency)) {
            return BigDecimal.ONE;
        }
        String upper = targetCurrency.toUpperCase();

        // 1. 内存缓存
        BigDecimal cached = cache.get(upper);
        if (cached != null) return cached;

        // 2. DB 实时查
        try {
            DeepayFxRateDO fx = fxRateMapper.selectByCurrency(upper);
            if (fx != null && fx.getRate() != null) {
                cache.put(upper, fx.getRate());
                return fx.getRate();
            }
        } catch (Exception e) {
            log.warn("[FxRateService] DB查汇率失败 currency={}", upper, e);
        }

        // 3. 硬编码 Fallback
        BigDecimal fallback = FALLBACK_RATES.get(upper);
        if (fallback != null) {
            log.debug("[FxRateService] 使用Fallback汇率 {}={}", upper, fallback);
            return fallback;
        }

        // 4. 最终兜底：返回 1（展示价=EUR价，不炸）
        log.warn("[FxRateService] 未知货币 {}，返回 rate=1", upper);
        return BigDecimal.ONE;
    }

    /**
     * 将 EUR 金额转换为目标货币金额（展示价）。
     *
     * @param eurAmount      EUR 基准金额
     * @param targetCurrency 目标货币（USD / CNY / GBP …）
     * @return 目标货币金额，保留 2 位小数
     */
    public BigDecimal convert(BigDecimal eurAmount, String targetCurrency) {
        if (eurAmount == null) return BigDecimal.ZERO;
        BigDecimal rate = getRate(targetCurrency);
        return eurAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 将目标货币金额换回 EUR（用于回调分析，统一 EUR 计算利润）。
     *
     * @param displayAmount  展示货币金额（e.g. USD）
     * @param sourceCurrency 来源货币（USD / CNY …）
     * @return EUR 金额
     */
    public BigDecimal convertToEur(BigDecimal displayAmount, String sourceCurrency) {
        if (displayAmount == null) return BigDecimal.ZERO;
        if (BASE_CURRENCY.equalsIgnoreCase(sourceCurrency)) return displayAmount;
        BigDecimal rate = getRate(sourceCurrency);
        if (rate.compareTo(BigDecimal.ZERO) == 0) return displayAmount;
        return displayAmount.divide(rate, 2, RoundingMode.HALF_UP);
    }

    /**
     * 计算 Jeepay 支付金额（分）。
     * Jeepay 不论什么货币，金额都用"分"（最小单位）。
     *
     * <pre>
     * €19.99 → 1999
     * $21.59 → 2159
     * ¥155.96 → 15596
     * </pre>
     *
     * @param eurAmount      EUR 基准金额
     * @param targetCurrency 目标货币
     * @return Jeepay amount（分，Long）
     */
    public long toPaymentCents(BigDecimal eurAmount, String targetCurrency) {
        BigDecimal display = convert(eurAmount, targetCurrency);
        return display.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }

    // ====================================================================
    // 缓存刷新（由 FxRateScheduler 调用）
    // ====================================================================

    /**
     * 刷新内存缓存（Scheduler 每小时调用）。
     */
    public void refreshCache(String currency, BigDecimal rate) {
        if (currency != null && rate != null) {
            cache.put(currency.toUpperCase(), rate);
        }
    }

    public Map<String, BigDecimal> getCacheSnapshot() {
        return Collections.unmodifiableMap(new HashMap<>(cache));
    }
}
