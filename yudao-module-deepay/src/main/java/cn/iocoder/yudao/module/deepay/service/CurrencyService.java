package cn.iocoder.yudao.module.deepay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * CurrencyService — 多货币换算（Phase 9 STEP 19）。
 *
 * <p>基准货币：CNY（人民币）。
 * 汇率表可通过配置或外部 API 更新，当前使用内置静态汇率（可随时替换）。</p>
 */
@Service
public class CurrencyService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyService.class);

    /** 内置静态汇率（CNY 基准，可替换为实时汇率 API） */
    private static final Map<String, BigDecimal> RATES = new HashMap<>();

    static {
        RATES.put("CNY", BigDecimal.ONE);
        RATES.put("EUR", new BigDecimal("0.13"));   // 1 CNY ≈ 0.13 EUR
        RATES.put("USD", new BigDecimal("0.14"));   // 1 CNY ≈ 0.14 USD
        RATES.put("GBP", new BigDecimal("0.11"));
        RATES.put("AED", new BigDecimal("0.51"));   // 中东市场（迪拉姆）
        RATES.put("JPY", new BigDecimal("21.0"));
        RATES.put("KRW", new BigDecimal("185.0"));
    }

    /**
     * 将 CNY 价格转换为目标货币。
     *
     * @param cnyAmount      人民币金额
     * @param targetCurrency 目标货币代码（EUR / USD / GBP / AED 等）
     * @return 换算后金额（保留 2 位小数）
     */
    public BigDecimal convert(BigDecimal cnyAmount, String targetCurrency) {
        if (cnyAmount == null) return BigDecimal.ZERO;
        if (targetCurrency == null || "CNY".equalsIgnoreCase(targetCurrency)) return cnyAmount;
        BigDecimal rate = RATES.get(targetCurrency.toUpperCase());
        if (rate == null) {
            log.warn("[CurrencyService] 不支持的货币 {}，原样返回 CNY", targetCurrency);
            return cnyAmount;
        }
        BigDecimal result = cnyAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        log.debug("[CurrencyService] {} CNY → {} {}", cnyAmount, result, targetCurrency);
        return result;
    }

    /**
     * 简化版：EUR 默认转换。
     */
    public BigDecimal toEur(BigDecimal cnyAmount) {
        return convert(cnyAmount, "EUR");
    }

    /**
     * 获取所有支持的货币代码。
     */
    public Set<String> supportedCurrencies() {
        return RATES.keySet();
    }
}
