package cn.iocoder.yudao.framework.pay.core.client.impl.wx;

import cn.hutool.core.io.IoUtil;
import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import lombok.Data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

// TODO 芋艿：参数校验
/**
 * 微信支付的 PayClientConfig 实现类
 * 属性主要来自 {@link com.github.binarywang.wxpay.config.WxPayConfig} 的必要属性
 *
 * @author 芋道源码
 */
@Data
public class WXPayClientConfig implements PayClientConfig {

    // TODO 芋艿：V2 or V3 客户端
    /**
     * API 版本 - V2
     *
     * https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=4_1
     */
    public static final String API_VERSION_V2 = "v2";
    /**
     * API 版本 - V3
     *
     * https://pay.weixin.qq.com/wiki/doc/apiv3/wechatpay/wechatpay-1.shtml
     */
    public static final String API_VERSION_V3 = "v3";

    /**
     * 公众号或者小程序的 appid
     */
    private String appId;
    /**
     * 商户号
     */
    private String mchId;
    /**
     * API 版本
     */
    private String apiVersion;

    // ========== V2 版本的参数 ==========

    /**
     * 商户密钥
     */
    private String mchKey;
//    /**
//     * apiclient_cert.p12 证书文件的绝对路径或者以 classpath: 开头的类路径.
//     * 对应的字符串
//     *
//     * 注意，可通过 {@link #main(String[])} 读取
//     */
//    private String keyContent;

    // ========== V3 版本的参数 ==========
    /**
     * apiclient_key.pem 证书文件的绝对路径或者以 classpath: 开头的类路径.
     * 对应的字符串
     *
     * 注意，可通过 {@link #main(String[])} 读取
     */
    private String privateKeyContent;
    /**
     * apiclient_cert.pem 证书文件的绝对路径或者以 classpath: 开头的类路径.
     * 对应的字符串
     *
     * 注意，可通过 {@link #main(String[])} 读取
     */
    private String privateCertContent;
    /**
     * apiV3 秘钥值
     */
    private String apiV3Key;

    public static void main(String[] args) throws FileNotFoundException {
        String path = "/Users/yunai/Downloads/wx_pay/apiclient_cert.p12";
//        String path = "/Users/yunai/Downloads/wx_pay/apiclient_key.pem";
//        String path = "/Users/yunai/Downloads/wx_pay/apiclient_cert.pem";
        System.out.println(IoUtil.readUtf8(new FileInputStream(path)));
    }

}
