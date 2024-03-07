package cn.iocoder.yudao.framework.pay.core.enums.channel;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.pay.core.client.impl.NonePayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.impl.alipay.AlipayPayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.impl.weixin.WxPayClientConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付渠道的编码的枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PayChannelEnum {

    WX_PUB("wx_pub", "微信 JSAPI 支付", WxPayClientConfig.class), // 公众号网页
    WX_LITE("wx_lite", "微信小程序支付", WxPayClientConfig.class),
    WX_APP("wx_app", "微信 App 支付", WxPayClientConfig.class),
    WX_NATIVE("wx_native", "微信 Native 支付", WxPayClientConfig.class),
    WX_WAP("wx_wap", "微信 Wap 网站支付", WxPayClientConfig.class), // H5 网页
    WX_BAR("wx_bar", "微信付款码支付", WxPayClientConfig.class),

    ALIPAY_PC("alipay_pc", "支付宝 PC 网站支付", AlipayPayClientConfig.class),
    ALIPAY_WAP("alipay_wap", "支付宝 Wap 网站支付", AlipayPayClientConfig.class),
    ALIPAY_APP("alipay_app", "支付宝App 支付", AlipayPayClientConfig.class),
    ALIPAY_QR("alipay_qr", "支付宝扫码支付", AlipayPayClientConfig.class),
    ALIPAY_BAR("alipay_bar", "支付宝条码支付", AlipayPayClientConfig.class),
    MOCK("mock", "模拟支付", NonePayClientConfig.class),

    WALLET("wallet", "钱包支付", NonePayClientConfig.class);

    /**
     * 编码
     *
     * 参考 <a href="https://www.pingxx.com/api/支付渠道属性值.html">支付渠道属性值</a>
     */
    private final String code;
    /**
     * 名字
     */
    private final String name;

    /**
     * 配置类
     */
    private final Class<? extends PayClientConfig> configClass;

    /**
     * 微信支付
     */
    public static final String WECHAT = "WECHAT";

    /**
     * 支付宝支付
     */
    public static final String ALIPAY = "ALIPAY";

    public static PayChannelEnum getByCode(String code) {
        return ArrayUtil.firstMatch(o -> o.getCode().equals(code), values());
    }

    public static boolean isAlipay(String channelCode) {
        return channelCode != null && channelCode.startsWith("alipay");
    }
}
