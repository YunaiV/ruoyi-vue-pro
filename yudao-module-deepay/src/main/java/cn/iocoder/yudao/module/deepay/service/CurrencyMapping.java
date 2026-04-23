package cn.iocoder.yudao.module.deepay.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * CurrencyMapping — 国家代码 → 货币代码静态映射。
 *
 * <h3>设计原则</h3>
 * <ul>
 *   <li>写死优先：稳定可靠，无外部依赖</li>
 *   <li>默认 EUR：未知国家统一回落到欧元</li>
 *   <li>可扩展：后续可从 deepay_country_currency 表动态加载</li>
 * </ul>
 */
public final class CurrencyMapping {

    private CurrencyMapping() {}

    /** 国家代码（ISO 3166-1 alpha-2）→ 货币代码（ISO 4217） */
    private static final Map<String, String> MAP;

    static {
        Map<String, String> m = new HashMap<>();
        // 欧元区
        m.put("DE", "EUR"); m.put("FR", "EUR"); m.put("IT", "EUR");
        m.put("ES", "EUR"); m.put("PT", "EUR"); m.put("NL", "EUR");
        m.put("BE", "EUR"); m.put("AT", "EUR"); m.put("FI", "EUR");
        m.put("IE", "EUR"); m.put("GR", "EUR"); m.put("SK", "EUR");
        m.put("SI", "EUR"); m.put("EE", "EUR"); m.put("LV", "EUR");
        m.put("LT", "EUR"); m.put("LU", "EUR"); m.put("MT", "EUR");
        m.put("CY", "EUR");
        // 其他主要货币
        m.put("US", "USD"); m.put("PR", "USD"); m.put("EC", "USD");
        m.put("GB", "GBP"); m.put("CN", "CNY"); m.put("HK", "HKD");
        m.put("TW", "TWD"); m.put("JP", "JPY"); m.put("KR", "KRW");
        m.put("SG", "SGD"); m.put("AU", "AUD"); m.put("CA", "CAD");
        m.put("CH", "CHF"); m.put("SE", "SEK"); m.put("NO", "NOK");
        m.put("DK", "DKK"); m.put("PL", "PLN"); m.put("CZ", "CZK");
        m.put("HU", "HUF"); m.put("RU", "RUB"); m.put("TR", "TRY");
        // 中东（AED 优先，其余 USD）
        m.put("AE", "AED"); m.put("SA", "SAR"); m.put("QA", "QAR");
        m.put("KW", "KWD"); m.put("BH", "BHD"); m.put("OM", "OMR");
        // 东南亚
        m.put("TH", "THB"); m.put("MY", "MYR"); m.put("ID", "IDR");
        m.put("VN", "VND"); m.put("PH", "PHP"); m.put("IN", "INR");
        MAP = Collections.unmodifiableMap(m);
    }

    /**
     * 根据国家代码返回货币代码，未知国家默认返回 EUR。
     *
     * @param countryCode ISO 3166-1 alpha-2（US / CN / DE …）
     * @return ISO 4217 货币代码
     */
    public static String getCurrency(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) return "EUR";
        return MAP.getOrDefault(countryCode.toUpperCase().trim(), "EUR");
    }
}
