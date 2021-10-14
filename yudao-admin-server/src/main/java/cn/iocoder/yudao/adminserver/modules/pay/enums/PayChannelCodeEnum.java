package cn.iocoder.yudao.adminserver.modules.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付渠道的编码的枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PayChannelCodeEnum {

    wx_pub("wx_pub", "微信 JSAPI 支付");

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

}
