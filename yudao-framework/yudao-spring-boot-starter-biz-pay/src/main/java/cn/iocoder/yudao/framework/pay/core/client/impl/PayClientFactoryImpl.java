package cn.iocoder.yudao.framework.pay.core.client.impl;

import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 支付客户端的工厂实现类
 *
 * @author 芋道源码
 */
public class PayClientFactoryImpl implements PayClientFactory {

    /**
     * 支付客户端 Map
     * key：渠道编号
     */
    private final ConcurrentMap<Long, AbstractPayClient> channelIdClients = new ConcurrentHashMap<>();

    @Override
    public PayClient getPayClient(Long channelId) {
        return null;
    }

    @Override
    public void createOrUpdatePayClient(Long channelId, String channelCode, PayClientConfig config) {

    }

}
