package cn.iocoder.yudao.module.deepay.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Jeepay 支付配置属性。
 *
 * <pre>
 * deepay:
 *   payment:
 *     provider: jeepay
 *   jeepay:
 *     api-url: https://your-jeepay-server.com
 *     mch-no: M000001
 *     app-id: your-app-id
 *     api-key: your-api-key
 *     way-code: WX_JSAPI          # 支付方式代码
 *     notify-url: https://your-domain.com/deepay/callback/payment
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "deepay.jeepay")
public class JeepayProperties {

    /** Jeepay 服务端 API 地址（不含路径） */
    private String apiUrl = "http://localhost:9000";

    /** 商户号 */
    private String mchNo = "M000001";

    /** 应用 ID */
    private String appId = "PLACEHOLDER_APP_ID";

    /** 签名密钥 */
    private String apiKey = "PLACEHOLDER_API_KEY";

    /** 默认支付方式代码（AUTO=自动 / WX_JSAPI / ALI_PC / ALI_WAP 等） */
    private String wayCode = "AUTO";

    /**
     * 货币代码（ISO 4217）。全系统统一 EUR（欧元）。
     * Jeepay 支持多币种，此处默认欧元。
     */
    private String currency = "EUR";

    /** 支付成功异步回调地址 */
    private String notifyUrl = "https://your-domain.com/deepay/callback/payment";

    // ---- getters / setters ----

    public String getApiUrl()     { return apiUrl; }
    public void setApiUrl(String v)     { this.apiUrl = v; }

    public String getMchNo()      { return mchNo; }
    public void setMchNo(String v)      { this.mchNo = v; }

    public String getAppId()      { return appId; }
    public void setAppId(String v)      { this.appId = v; }

    public String getApiKey()     { return apiKey; }
    public void setApiKey(String v)     { this.apiKey = v; }

    public String getWayCode()    { return wayCode; }
    public void setWayCode(String v)    { this.wayCode = v; }

    public String getCurrency()   { return currency; }
    public void setCurrency(String v)   { this.currency = v; }

    public String getNotifyUrl()  { return notifyUrl; }
    public void setNotifyUrl(String v)  { this.notifyUrl = v; }
}
