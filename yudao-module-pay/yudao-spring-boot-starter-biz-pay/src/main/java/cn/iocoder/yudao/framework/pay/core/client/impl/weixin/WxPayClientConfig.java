package cn.iocoder.yudao.framework.pay.core.client.impl.weixin;

import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import lombok.Data;

import javax.validation.Validator;
import javax.validation.constraints.NotBlank;

/**
 * 微信支付的 PayClientConfig 实现类
 * 属性主要来自 {@link com.github.binarywang.wxpay.config.WxPayConfig} 的必要属性
 *
 * @author 芋道源码
 */
@Data
public class WxPayClientConfig implements PayClientConfig {

    /**
     * API 版本 - V2
     *
     * <a href="https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=4_1">V2 协议说明</a>
     */
    public static final String API_VERSION_V2 = "v2";
    /**
     * API 版本 - V3
     *
     * <a href="https://pay.weixin.qq.com/wiki/doc/apiv3/wechatpay/wechatpay-1.shtml">V3 协议说明</a>
     */
    public static final String API_VERSION_V3 = "v3";

    /**
     * 公众号或者小程序的 appid
     *
     * 只有公众号或小程序需要该字段
     */
    @NotBlank(message = "APPID 不能为空", groups = {V2.class, V3.class})
    private String appId;
    /**
     * 商户号
     */
    @NotBlank(message = "商户号不能为空", groups = {V2.class, V3.class})
    private String mchId;
    /**
     * API 版本
     */
    @NotBlank(message = "API 版本不能为空", groups = {V2.class, V3.class})
    private String apiVersion;

    // ========== V2 版本的参数 ==========

    /**
     * 商户密钥
     */
    @NotBlank(message = "商户密钥不能为空", groups = V2.class)
    private String mchKey;
    /**
     * apiclient_cert.p12 证书文件的对应字符串【base64 格式】
     *
     * 为什么采用 base64 格式？因为 p12 读取后是二进制，需要转换成 base64 格式才好传输和存储
     */
    @NotBlank(message = "apiclient_cert.p12 不能为空", groups = V2.class)
    private String keyContent;

    // ========== V3 版本的参数 ==========
    /**
     * apiclient_key.pem 证书文件的对应字符串
     */
    @NotBlank(message = "apiclient_key 不能为空", groups = V3.class)
    private String privateKeyContent;
    /**
     * apiV3 密钥值
     */
    @NotBlank(message = "apiV3 密钥值不能为空", groups = V3.class)
    private String apiV3Key;
    /**
     * 证书序列号
     */
    @NotBlank(message = "证书序列号不能为空", groups = V3.class)
    private String certSerialNo;

    @Deprecated // TODO 芋艿：V2.3.0 进行移除
    private String privateCertContent;

    /**
     * 分组校验 v2版本
     */
    public interface V2 {
    }

    /**
     * 分组校验 v3版本
     */
    public interface V3 {
    }

    @Override
    public void validate(Validator validator) {
        ValidationUtils.validate(validator, this,
                API_VERSION_V2.equals(this.getApiVersion()) ? V2.class : V3.class);
    }

}
