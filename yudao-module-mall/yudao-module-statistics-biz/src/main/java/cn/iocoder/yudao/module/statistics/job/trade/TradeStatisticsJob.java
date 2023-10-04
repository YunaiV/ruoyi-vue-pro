package cn.iocoder.yudao.module.statistics.job.trade;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.statistics.service.trade.TradeStatisticsService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 交易统计 Job
 *
 * @author owen
 */
@Component
public class TradeStatisticsJob implements JobHandler {

    @Resource
    private TradeStatisticsService tradeStatisticsService;

    @Override
    @TenantJob
    public String execute(String param) {
        String times = tradeStatisticsService.statisticsYesterdayTrade();
        return StrUtil.format("交易统计耗时: {}", times);
    }

}
