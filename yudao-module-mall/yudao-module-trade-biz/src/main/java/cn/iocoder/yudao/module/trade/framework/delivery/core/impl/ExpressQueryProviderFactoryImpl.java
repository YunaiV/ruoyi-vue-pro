package cn.iocoder.yudao.module.trade.framework.delivery.core.impl;

import cn.iocoder.yudao.module.trade.framework.delivery.config.TradeExpressQueryProperties;
import cn.iocoder.yudao.module.trade.framework.delivery.core.ExpressQueryProvider;
import cn.iocoder.yudao.module.trade.framework.delivery.core.ExpressQueryProviderEnum;
import cn.iocoder.yudao.module.trade.framework.delivery.core.ExpressQueryProviderFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jason
 */
@Component
public class ExpressQueryProviderFactoryImpl implements ExpressQueryProviderFactory {

    private final Map<ExpressQueryProviderEnum, ExpressQueryProvider> providerMap = new ConcurrentHashMap<>(8);
    @Resource
    private TradeExpressQueryProperties tradeExpressQueryProperties;
    @Resource
    private RestTemplate restTemplate;

    @Override
    public ExpressQueryProvider getOrCreateExpressQueryProvider(ExpressQueryProviderEnum queryProviderEnum) {
        return providerMap.computeIfAbsent(queryProviderEnum,
                provider -> createExpressQueryProvider(provider, tradeExpressQueryProperties));
    }

    private ExpressQueryProvider createExpressQueryProvider(ExpressQueryProviderEnum queryProviderEnum,
                                                            TradeExpressQueryProperties tradeExpressQueryProperties) {
        ExpressQueryProvider result = null;
        switch (queryProviderEnum) {
            case KD_NIAO:
                result = new KdNiaoExpressQueryProvider(restTemplate, tradeExpressQueryProperties.getKdNiao());
                break;
            case KD_100:
                result = new Kd100ExpressQueryProvider(restTemplate, tradeExpressQueryProperties.getKd100());
                break;
        }
        return result;
    }
}
