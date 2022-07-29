package cn.iocoder.yudao.framework.pay.core.client.impl;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.client.impl.alipay.AlipayPayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.impl.alipay.AlipayPcPayClient;
import cn.iocoder.yudao.framework.pay.core.client.impl.alipay.AlipayQrPayClient;
import cn.iocoder.yudao.framework.pay.core.client.impl.alipay.AlipayWapPayClient;
import cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXLitePayClient;
import cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXNativePayClient;
import cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXPayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXPubPayClient;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 支付客户端的工厂实现类
 *
 * @author 芋道源码
 */
@Slf4j
public class PayClientFactoryImpl implements PayClientFactory {

    /**
     * 支付客户端 Map
     * key：渠道编号
     */
    private final ConcurrentMap<Long, AbstractPayClient<?>> clients = new ConcurrentHashMap<>();

    @Override
    public PayClient getPayClient(Long channelId) {
        AbstractPayClient<?> client = clients.get(channelId);
        if (client == null) {
            log.error("[getPayClient][渠道编号({}) 找不到客户端]", channelId);
        }
        return client;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Config extends PayClientConfig> void createOrUpdatePayClient(Long channelId, String channelCode,
                                                                         Config config) {
        AbstractPayClient<Config> client = (AbstractPayClient<Config>) clients.get(channelId);
        if (client == null) {
            client = this.createPayClient(channelId, channelCode, config);
            client.init();
            clients.put(client.getId(), client);
        } else {
            client.refresh(config);
        }
    }

    @SuppressWarnings("unchecked")
    private <Config extends PayClientConfig> AbstractPayClient<Config> createPayClient(
            Long channelId, String channelCode, Config config) {
        PayChannelEnum channelEnum = PayChannelEnum.getByCode(channelCode);
        Assert.notNull(channelEnum, String.format("支付渠道(%s) 为空", channelEnum));
        // 创建客户端
        // TODO @芋艿 WX_LITE WX_APP 如果不添加在 项目启动的时候去初始化会报错无法启动。所以我手动加了两个，具体需要你来配
        switch (channelEnum) {
            case WX_PUB: return (AbstractPayClient<Config>) new WXPubPayClient(channelId, (WXPayClientConfig) config);
            case WX_LITE: return (AbstractPayClient<Config>) new WXLitePayClient(channelId, (WXPayClientConfig) config); //微信小程序请求支付
            case WX_APP: return (AbstractPayClient<Config>) new WXPubPayClient(channelId, (WXPayClientConfig) config);
            case WX_NATIVE: return (AbstractPayClient<Config>) new WXNativePayClient(channelId, (WXPayClientConfig) config);
            case ALIPAY_WAP: return (AbstractPayClient<Config>) new AlipayWapPayClient(channelId, (AlipayPayClientConfig) config);
            case ALIPAY_QR: return (AbstractPayClient<Config>) new AlipayQrPayClient(channelId, (AlipayPayClientConfig) config);
            case ALIPAY_APP: return (AbstractPayClient<Config>) new AlipayQrPayClient(channelId, (AlipayPayClientConfig) config);
            case ALIPAY_PC: return (AbstractPayClient<Config>) new AlipayPcPayClient(channelId, (AlipayPayClientConfig) config);
        }
        // 创建失败，错误日志 + 抛出异常
        log.error("[createPayClient][配置({}) 找不到合适的客户端实现]", config);
        throw new IllegalArgumentException(String.format("配置(%s) 找不到合适的客户端实现", config));
    }

}
