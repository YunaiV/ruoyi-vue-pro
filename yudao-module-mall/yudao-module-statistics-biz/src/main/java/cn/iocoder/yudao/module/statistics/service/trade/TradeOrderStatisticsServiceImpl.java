package cn.iocoder.yudao.module.statistics.service.trade;

import cn.iocoder.yudao.module.statistics.dal.mysql.trade.TradeOrderStatisticsMapper;
import cn.iocoder.yudao.module.statistics.service.trade.bo.TradeOrderSummaryRespBO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 交易订单统计 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class TradeOrderStatisticsServiceImpl implements TradeOrderStatisticsService {

    @Resource
    private TradeOrderStatisticsMapper tradeOrderStatisticsMapper;

    @Override
    public TradeOrderSummaryRespBO getOrderSummary(LocalDateTime beginTime, LocalDateTime endTime) {
        return tradeOrderStatisticsMapper.selectSummaryByPayTimeBetween(beginTime, endTime);
    }

}
