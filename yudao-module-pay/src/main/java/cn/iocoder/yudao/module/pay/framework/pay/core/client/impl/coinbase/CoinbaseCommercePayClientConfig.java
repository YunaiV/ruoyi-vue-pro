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
