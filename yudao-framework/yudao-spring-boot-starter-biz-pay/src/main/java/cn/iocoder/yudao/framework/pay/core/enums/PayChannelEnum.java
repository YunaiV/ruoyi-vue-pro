package cn.iocoder.yudao.framework.pay.core.enums;

import cn.hutool.core.util.ArrayUtil;
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
    // TODO @芋艿 这个地方你写的是 wx_lit 是不是少写了一个e？ 还是我这里多加了一个e
    // TODO @aquan：这里就是 lite 哈，轻量
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

    public static PayChannelEnum getByCode(String code) {
        return ArrayUtil.firstMatch(o -> o.getCode().equals(code), values());
    }

}
