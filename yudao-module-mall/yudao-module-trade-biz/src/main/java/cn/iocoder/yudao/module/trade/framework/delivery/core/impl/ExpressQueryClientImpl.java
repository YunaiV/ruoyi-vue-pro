package cn.iocoder.yudao.module.trade.framework.delivery.core.impl;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.module.trade.framework.delivery.config.TradeExpressQueryProperties;
import cn.iocoder.yudao.module.trade.framework.delivery.core.ExpressQueryClient;
import cn.iocoder.yudao.module.trade.framework.delivery.core.ExpressQueryProvider;
import cn.iocoder.yudao.module.trade.framework.delivery.core.ExpressQueryProviderEnum;
import cn.iocoder.yudao.module.trade.framework.delivery.core.ExpressQueryProviderFactory;
import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.ExpressQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.ExpressQueryRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.module.trade.framework.delivery.core.ExpressQueryProviderEnum.KD_NIAO;

// TODO @jason：可以把整体包结构调整下；参考 sms client 的方式；
// + config
// + core
//      client
//         + dto
//         + impl：里面可以放 kdniaoclient、kd100client
//         ExpressClient
//         ExpressClientFactory: 通过它直接获取默认和创建默认的 Client
//      enums
/**
 * 快递查询客户端实现
 *
 * @author jason
 */
@Component
@Slf4j
public class ExpressQueryClientImpl implements ExpressQueryClient  {

    @Resource
    private ExpressQueryProviderFactory expressQueryProviderFactory;
    @Resource
    private TradeExpressQueryProperties tradeExpressQueryProperties;

    private ExpressQueryProvider expressQueryProvider;

    @PostConstruct
    private void init() {
        // 如果未设置，默认使用快递鸟
        ExpressQueryProviderEnum queryProvider = tradeExpressQueryProperties.getExpressQueryProvider();
        if (queryProvider == null) {
            queryProvider = KD_NIAO;
        }
        // 创建客户端
        expressQueryProvider = expressQueryProviderFactory.getOrCreateExpressQueryProvider(queryProvider);
        if (expressQueryProvider == null) {
            log.error("获取创建快递查询服务商{}失败，请检查相关配置", queryProvider);
        }
        Assert.notNull(expressQueryProvider, "快递查询服务商不能为空");
    }

    @Override
    public List<ExpressQueryRespDTO> realTimeQuery(ExpressQueryReqDTO reqDTO) {
        return expressQueryProvider.realTimeQueryExpress(reqDTO);
    }

}
