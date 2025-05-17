package cn.iocoder.yudao.module.pay.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 支付渠道的编码的枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PayChannelEnum implements ArrayValuable<String> {

    WX_PUB("wx_pub", "微信 JSAPI 支付"), // 公众号网页
    WX_LITE("wx_lite", "微信小程序支付"),
    WX_APP("wx_app", "微信 App 支付"),
    WX_NATIVE("wx_native", "微信 Native 支付"),
    WX_WAP("wx_wap", "微信 Wap 网站支付"), // H5 网页
    WX_BAR("wx_bar", "微信付款码支付"),

    ALIPAY_PC("alipay_pc", "支付宝 PC 网站支付"),
    ALIPAY_WAP("alipay_wap", "支付宝 Wap 网站支付"),
    ALIPAY_APP("alipay_app", "支付宝App 支付"),
    ALIPAY_QR("alipay_qr", "支付宝扫码支付"),
    ALIPAY_BAR("alipay_bar", "支付宝条码支付"),

    MOCK("mock", "模拟支付"),

    WALLET("wallet", "钱包支付");

    public static final String[] ARRAYS = Arrays.stream(values()).map(PayChannelEnum::getCode).toArray(String[]::new);

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

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static PayChannelEnum getByCode(String code) {
        return ArrayUtil.firstMatch(o -> o.getCode().equals(code), values());
    }

    public static boolean isAlipay(String channelCode) {
        return StrUtil.startWith(channelCode, "alipay_");
    }

    public static boolean isWeixin(String channelCode) {
        return StrUtil.startWith(channelCode, "wx_");
    }

}
