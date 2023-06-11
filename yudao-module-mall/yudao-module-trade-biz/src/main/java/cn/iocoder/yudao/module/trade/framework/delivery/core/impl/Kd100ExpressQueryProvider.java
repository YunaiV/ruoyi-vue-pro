package cn.iocoder.yudao.module.trade.framework.delivery.core.impl;

import cn.iocoder.yudao.module.trade.framework.delivery.config.TradeExpressQueryProperties;
import cn.iocoder.yudao.module.trade.framework.delivery.core.ExpressQueryProvider;
import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.ExpressQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.ExpressQueryRespDTO;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 快递 100 服务商 TODO
 * @author jason
 */
public class Kd100ExpressQueryProvider implements ExpressQueryProvider {

    private final RestTemplate restTemplate;

    private final TradeExpressQueryProperties.Kd100Config config;

    public Kd100ExpressQueryProvider(RestTemplate restTemplate, TradeExpressQueryProperties.Kd100Config config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    @Override
    public List<ExpressQueryRespDTO> realTimeQueryExpress(ExpressQueryReqDTO reqDTO) {
        return null;
    }
}
