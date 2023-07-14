package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import lombok.Data;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * 支付宝的 PayClientConfig 实现类
 * 属性主要来自 {@link com.alipay.api.AlipayConfig} 的必要属性
 *
 * @author 芋道源码
 */
@Data
public class AlipayPayClientConfig implements PayClientConfig {

    /**
     * 公钥类型 - 公钥模式
     */
    public static final Integer MODE_PUBLIC_KEY = 1;
    /**
     * 公钥类型 - 证书模式
     */
    public static final Integer MODE_CERTIFICATE = 2;

    /**
     * 签名算法类型 - RSA
     */
    public static final String SIGN_TYPE_DEFAULT = "RSA2";

    /**
     * 网关地址
     *
     * 1. <a href="https://openapi.alipay.com/gateway.do">生产环境</a>
     * 2. <a href="https://openapi-sandbox.dl.alipaydev.com/gateway.do">沙箱环境</a>
     */
    @NotBlank(message = "网关地址不能为空", groups = {ModePublicKey.class, ModeCertificate.class})
    private String serverUrl;

    /**
     * 开放平台上创建的应用的 ID
     */
    @NotBlank(message = "开放平台上创建的应用的 ID不能为空", groups = {ModePublicKey.class, ModeCertificate.class})
    private String appId;

    /**
     * 签名算法类型，推荐：RSA2
     * <p>
     * {@link #SIGN_TYPE_DEFAULT}
     */
    @NotBlank(message = "签名算法类型不能为空", groups = {ModePublicKey.class, ModeCertificate.class})
    private String signType;

    /**
     * 公钥类型
     * 1. {@link #MODE_PUBLIC_KEY} 情况，privateKey + alipayPublicKey
     * 2. {@link #MODE_CERTIFICATE} 情况，appCertContent + alipayPublicCertContent + rootCertContent
     */
    @NotNull(message = "公钥类型不能为空", groups = {ModePublicKey.class, ModeCertificate.class})
    private Integer mode;

    // ========== 公钥模式 ==========
    /**
     * 商户私钥
     */
    @NotBlank(message = "商户私钥不能为空", groups = {ModePublicKey.class})
    private String privateKey;

    /**
     * 支付宝公钥字符串
     */
    @NotBlank(message = "支付宝公钥字符串不能为空", groups = {ModePublicKey.class})
    private String alipayPublicKey;

    // ========== 证书模式 ==========
    /**
     * 指定商户公钥应用证书内容字符串
     */
    @NotBlank(message = "指定商户公钥应用证书内容不能为空", groups = {ModeCertificate.class})
    private String appCertContent;
    /**
     * 指定支付宝公钥证书内容字符串
     */
    @NotBlank(message = "指定支付宝公钥证书内容不能为空", groups = {ModeCertificate.class})
    private String alipayPublicCertContent;
    /**
     * 指定根证书内容字符串
     */
    @NotBlank(message = "指定根证书内容字符串不能为空", groups = {ModeCertificate.class})
    private String rootCertContent;

    public interface ModePublicKey {
    }

    public interface ModeCertificate {
    }

    @Override
    public void validate(Validator validator) {
        ValidationUtils.validate(validator, this,
                MODE_PUBLIC_KEY.equals(this.getMode()) ? ModePublicKey.class : ModeCertificate.class);
    }

}
