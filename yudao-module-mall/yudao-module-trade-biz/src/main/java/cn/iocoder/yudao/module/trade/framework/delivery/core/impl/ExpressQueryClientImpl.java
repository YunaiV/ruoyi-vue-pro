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

/**
 * 快递查询客户端实现
 *
 * @author jason
 */
@Component
@Slf4j
public class ExpressQueryClientImpl  implements ExpressQueryClient  {
    @Resource
    private ExpressQueryProviderFactory expressQueryProviderFactory;
    @Resource
    private TradeExpressQueryProperties tradeExpressQueryProperties;

    private ExpressQueryProvider expressQueryProvider;
    @PostConstruct
    private void init(){
        ExpressQueryProviderEnum queryProvider = tradeExpressQueryProperties.getExpressQueryProvider();
        if (queryProvider == null) {
            // 如果未设置，默认使用快递鸟
            queryProvider = KD_NIAO;
        }
        expressQueryProvider = expressQueryProviderFactory.getOrCreateExpressQueryProvider(queryProvider);
        if (expressQueryProvider == null) {
            // 记录错误日志
            log.error("获取创建快递查询服务商{}失败，请检查相关配置", queryProvider);
        }
        Assert.notNull(expressQueryProvider, "快递查询服务商不能为空");

    }
    @Override
    public List<ExpressQueryRespDTO> realTimeQuery(ExpressQueryReqDTO reqDTO) {
        return expressQueryProvider.realTimeQueryExpress(reqDTO);
    }
}
