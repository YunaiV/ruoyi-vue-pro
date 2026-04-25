package cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.coinbase;

import cn.iocoder.yudao.module.pay.framework.pay.core.client.PayClientConfig;
import lombok.Data;

import javax.validation.Validator;
import javax.validation.constraints.NotBlank;

/**
 * Coinbase Commerce 支付渠道配置
 * <p>
 * 管理后台在 pay_channel 表中配置以下字段（JSON 序列化存储）：
 * <ul>
 *   <li>{@code apiKey}        — Coinbase Commerce API Key（从 Commerce 控制台创建）</li>
 *   <li>{@code webhookSecret} — Webhook 共享密钥（用于签名验证）</li>
 *   <li>{@code currency}      — 计价货币，默认 USD；支持 CNY 时会换算</li>
 *   <li>{@code redirectUrl}   — 支付完成后跳转地址（可选）</li>
 * </ul>
 *
 * @author deepay
 */
@Data
public class CoinbaseCommercePayClientConfig implements PayClientConfig {

    /**
     * Coinbase Commerce API Key
     */
    @NotBlank(message = "Coinbase Commerce API Key 不能为空")
    private String apiKey;

    /**
     * Webhook 共享密钥（用于 HMAC-SHA256 签名验证回调）
     */
    @NotBlank(message = "Coinbase Commerce Webhook Secret 不能为空")
    private String webhookSecret;

    /**
     * 计价货币，默认 USD
     */
    private String currency = "USD";

    /**
     * 支付成功后跳转 URL（可选）
     */
    private String redirectUrl;

    /**
     * 取消支付后跳转 URL（可选）
     */
    private String cancelUrl;

    /**
     * 人民币兑 USD 汇率（1 CNY = 1/X USD；仅当 currency=USD 且订单金额为人民币时使用）
     * <b>请在管理后台配置实时汇率，或对接汇率 API 后定期更新此值。</b>
     * 默认值 7.20（即 1 USD ≈ 7.20 CNY）。
     */
    private java.math.BigDecimal cnyToUsdRate = new java.math.BigDecimal("7.20");

    @Override
    public void validate(Validator validator) {
        // 使用 Bean Validation 验证必填字段
        var violations = validator.validate(this);
        if (!violations.isEmpty()) {
            throw new javax.validation.ValidationException(
                    violations.iterator().next().getMessage());
        }
    }

}
