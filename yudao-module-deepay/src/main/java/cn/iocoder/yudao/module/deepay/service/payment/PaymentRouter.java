package cn.iocoder.yudao.module.deepay.service.payment;

/**
 * PaymentRouter — 货币 → Jeepay 支付方式代码（wayCode）路由表。
 *
 * <h3>设计</h3>
 * <ul>
 *   <li>EUR / USD / GBP / AED → STRIPE（欧洲/国际信用卡）</li>
 *   <li>CNY → ALIPAY（支付宝，国内主流）</li>
 *   <li>其余 → AUTO（Jeepay 自动选最优通道）</li>
 * </ul>
 *
 * <h3>Jeepay wayCode 参考</h3>
 * <pre>
 * AUTO         自动路由（推荐，Jeepay内部决策）
 * ALIPAY_PC    支付宝PC
 * ALIPAY_WAP   支付宝H5
 * WX_JSAPI     微信JSAPI
 * WX_H5        微信H5
 * STRIPE       Stripe（欧美信用卡）
 * PAYPAL       PayPal
 * </pre>
 */
public final class PaymentRouter {

    private PaymentRouter() {}

    /**
     * 根据货币代码返回 Jeepay wayCode。
     *
     * @param currency ISO 4217 货币代码（EUR / USD / CNY …）
     * @return Jeepay wayCode 字符串
     */
    public static String route(String currency) {
        if (currency == null) return "AUTO";
        switch (currency.toUpperCase()) {
            case "CNY": return "ALIPAY_WAP";   // 国内用支付宝H5
            case "EUR":
            case "USD":
            case "GBP":
            case "CHF":
            case "AUD":
            case "CAD": return "STRIPE";        // 欧洲/英语区信用卡
            case "AED":
            case "SAR":
            case "QAR": return "STRIPE";        // 中东统一走Stripe（当地Stripe覆盖好）
            case "JPY":
            case "KRW": return "AUTO";          // 东亚让Jeepay自选
            default:    return "AUTO";           // 未知统一AUTO
        }
    }

    /**
     * 判断给定货币是否需要整数金额（无"分"概念的货币）。
     * 例：JPY / KRW / IDR / VND 的最小单位就是1（Jeepay amount = 整数元，不×100）。
     */
    public static boolean isZeroDecimalCurrency(String currency) {
        if (currency == null) return false;
        switch (currency.toUpperCase()) {
            case "JPY": case "KRW": case "IDR": case "VND":
            case "UGX": case "RWF": case "BIF":
                return true;
            default: return false;
        }
    }
}
