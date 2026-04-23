package cn.iocoder.yudao.module.deepay.service;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * UserCurrencyService — 自动识别用户币种（IP → 国家 → 货币）。
 *
 * <h3>调用方式</h3>
 * <pre>
 * // 从 HttpServletRequest 自动识别（推荐）
 * String currency = userCurrencyService.detect(request);
 *
 * // 从 IP 字符串识别
 * String currency = userCurrencyService.detect("8.8.8.8");
 * </pre>
 *
 * <h3>优先级</h3>
 * <ol>
 *   <li>请求头 {@code X-User-Currency}（允许前端覆盖，但不信任金额）</li>
 *   <li>IP → 国家 → 货币（主路径）</li>
 *   <li>默认 EUR</li>
 * </ol>
 */
@Service
public class UserCurrencyService {

    @Resource private IpGeoService    ipGeoService;
    @Resource private FxRateService   fxRateService;

    /**
     * 从 HttpServletRequest 识别货币代码。
     *
     * @param request HTTP 请求
     * @return ISO 4217 货币代码（EUR / USD / CNY …）
     */
    public String detect(HttpServletRequest request) {
        // 允许前端通过 Header 指定货币（但绝不信任前端传的金额）
        String headerCurrency = request.getHeader("X-User-Currency");
        if (headerCurrency != null && !headerCurrency.isBlank()
                && fxRateService.getRate(headerCurrency.toUpperCase()).compareTo(java.math.BigDecimal.ZERO) > 0) {
            return headerCurrency.toUpperCase();
        }
        String ip = extractIp(request);
        return detect(ip);
    }

    /**
     * 从 IP 地址识别货币代码。
     *
     * @param ip 客户端 IP
     * @return ISO 4217 货币代码
     */
    public String detect(String ip) {
        String country = ipGeoService.detectCountry(ip);
        return CurrencyMapping.getCurrency(country);
    }

    // ====================================================================
    // helpers
    // ====================================================================

    /**
     * 提取真实客户端 IP（处理反向代理）。
     */
    public static String extractIp(HttpServletRequest request) {
        // 反向代理头（优先）
        String[] headers = {
            "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR", "X-Real-IP"
        };
        for (String h : headers) {
            String ip = request.getHeader(h);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For 可能是逗号分隔的链路，取第一个
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
