package cn.iocoder.yudao.framework.pay.core.client.impl.weixin;

import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * 微信支付（小程序）的 PayClient 实现类
 *
 * 由于公众号和小程序的微信支付逻辑一致，所以直接进行继承
 *
 * @author zwy
 */
@Slf4j
public class WXLitePayClient extends WxPubPayClient {

    public WXLitePayClient(Long channelId, WxPayClientConfig config) {
        super(channelId, PayChannelEnum.WX_LITE.getCode(), config);
    }

}
