package cn.iocoder.yudao.framework.pay.core.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.impl.alipay.AlipayPayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXPayClientConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付渠道的编码的枚举
 * 枚举值
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PayChannelEnum {

    WX_PUB("wx_pub", "微信 JSAPI 支付"), // 公众号的网页
    WX_LITE("wx_lite","微信小程序支付"),
    WX_APP("wx_app", "微信 App 支付"),

    ALIPAY_PC("alipay_pc", "支付宝 PC 网站支付"),
    ALIPAY_WAP("alipay_wap", "支付宝 Wap 网站支付"),
    ALIPAY_APP("alipay_app", "支付宝App 支付"),
    ALIPAY_QR("alipay_qr", "支付宝扫码支付");

    /**
     * 编码
     *
     * 参考 https://www.pingxx.com/api/支付渠道属性值.html
     */
    private String code;
    /**
     * 名字
     */
    private String name;

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

    // TODO @aquan：加一个 configClass 字段，不用 switch 的方式哈。不然新增一个支付方式，需要改的方法有点多
    /**
     * 根据编码得到支付类
     *
     * @param code 编码
     * @return 支付配置类
     */
    public static Class<? extends PayClientConfig> findByCodeGetClass(String code) {
        switch (PayChannelEnum.getByCode(code)){
            case WX_PUB:
            case WX_LITE:
            case WX_APP:
                return WXPayClientConfig.class;
            case ALIPAY_PC:
            case ALIPAY_WAP:
            case ALIPAY_APP:
            case ALIPAY_QR:
                return AlipayPayClientConfig.class;
        }
        return null;
    }

}
