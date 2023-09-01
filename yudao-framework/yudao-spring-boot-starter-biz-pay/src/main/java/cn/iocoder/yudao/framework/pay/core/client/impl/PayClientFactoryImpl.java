package cn.iocoder.yudao.framework.pay.core.client.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.client.impl.alipay.*;
import cn.iocoder.yudao.framework.pay.core.client.impl.mock.MockPayClient;
import cn.iocoder.yudao.framework.pay.core.client.impl.weixin.*;
import cn.iocoder.yudao.framework.pay.core.enums.channel.PayChannelEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static cn.iocoder.yudao.framework.pay.core.enums.channel.PayChannelEnum.*;

/**
 * 支付客户端的工厂实现类
 *
 * @author 芋道源码
 */
@Slf4j
public class PayClientFactoryImpl implements PayClientFactory {

    /**
     * 支付客户端 Map
     *
     * key：渠道编号
     */
    private final ConcurrentMap<Long, AbstractPayClient<?>> clients = new ConcurrentHashMap<>();

    /**
     * 支付客户端 Class Map
     */
    private final Map<PayChannelEnum, Class<?>> clientClass = new ConcurrentHashMap<>();

    public PayClientFactoryImpl() {
        // 微信支付客户端
        clientClass.put(WX_PUB, WxPubPayClient.class);
        clientClass.put(WX_LITE, WxLitePayClient.class);
        clientClass.put(WX_APP, WxAppPayClient.class);
        clientClass.put(WX_BAR, WxBarPayClient.class);
        clientClass.put(WX_NATIVE, WxNativePayClient.class);
        // 支付包支付客户端
        clientClass.put(ALIPAY_WAP, AlipayWapPayClient.class);
        clientClass.put(ALIPAY_QR, AlipayQrPayClient.class);
        clientClass.put(ALIPAY_APP, AlipayAppPayClient.class);
        clientClass.put(ALIPAY_PC, AlipayPcPayClient.class);
        clientClass.put(ALIPAY_BAR, AlipayBarPayClient.class);
        // Mock 支付客户端
        clientClass.put(MOCK, MockPayClient.class);
    }

    @Override
    public void registerPayClientClass(PayChannelEnum channel, Class<?> payClientClass) {
        clientClass.put(channel, payClientClass);
    }

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
    private <Config extends PayClientConfig> AbstractPayClient<Config> createPayClient(Long channelId, String channelCode,
                                                                                       Config config) {
        PayChannelEnum channelEnum = PayChannelEnum.getByCode(channelCode);
        Assert.notNull(channelEnum, String.format("支付渠道(%s) 为空", channelCode));
        Class<?> payClientClass = clientClass.get(channelEnum);
        Assert.notNull(payClientClass, String.format("支付渠道(%s) Class 为空", channelCode));
        return (AbstractPayClient<Config>) ReflectUtil.newInstance(payClientClass, channelId, config);
    }

}
