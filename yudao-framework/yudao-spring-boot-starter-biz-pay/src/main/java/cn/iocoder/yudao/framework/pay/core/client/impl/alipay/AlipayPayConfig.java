package cn.iocoder.yudao.framework.pay.core.client.impl.alipay;

import lombok.Data;

// TODO 芋艿：参数校验
/**
 * 支付宝的 PayClientConfig 实现类
 * 属性主要来自 {@link com.alipay.api.AlipayConfig} 的必要属性
 *
 * @author 芋道源码
 */
@Data
public class AlipayPayConfig {

    /**
     * 网关地址 - 线上
     */
    public static final String SERVER_URL_PROD = "https://openapi.alipay.com/gateway.do";
    /**
     * 网关地址 - 沙箱
     */
    public static final String SERVER_URL_SANDBOX = "https://openapi.alipaydev.com/gateway.do";

    /**
     * 公钥类型 - 公钥模式
     */
    private static final Integer MODE_PUBLIC_KEY = 1;
    /**
     * 公钥类型 - 证书模式
     */
    private static final Integer MODE_CERTIFICATE = 2;

    /**
     * 签名算法类型 - RSA
     */
    public static final String SIGN_TYPE_DEFAULT = "RSA2";

    /**
     * 网关地址
     * 1. {@link #SERVER_URL_PROD}
     * 2. {@link #SERVER_URL_SANDBOX}
     */
    private String serverUrl;

    /**
     * 开放平台上创建的应用的 ID
     */
    private String appId;

    /**
     * 签名算法类型，推荐：RSA2
     *
     * {@link #SIGN_TYPE_DEFAULT}
     */
    private String signType;

    /**
     * 公钥类型
     * 1. {@link #MODE_PUBLIC_KEY} 情况，privateKey + alipayPublicKey
     * 2. {@link #MODE_CERTIFICATE} 情况，appCertContent + alipayPublicCertContent + rootCertContent
     */
    private Integer mode;

    // ========== 公钥模式 ==========
    /**
     * 商户私钥
     */
    private String privateKey;
    /**
     * 支付宝公钥字符串
     */
    private String alipayPublicKey;

    // ========== 证书模式 ==========
    /**
     * 指定商户公钥应用证书内容字符串
     */
    private String appCertContent;
    /**
     * 指定支付宝公钥证书内容字符串
     */
    private String alipayPublicCertContent;
    /**
     * 指定根证书内容字符串
     */
    private String rootCertContent;

}
