package cn.iocoder.yudao.module.statistics.service.trade;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeStatisticsComparisonRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeSummaryRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeTrendSummaryRespVO;
import cn.iocoder.yudao.module.statistics.convert.trade.TradeStatisticsConvert;
import cn.iocoder.yudao.module.statistics.dal.mysql.trade.TradeStatisticsMapper;
import cn.iocoder.yudao.module.statistics.service.trade.bo.TradeSummaryRespBO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * 交易统计 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class TradeStatisticsServiceImpl implements TradeStatisticsService {

    @Resource
    private TradeStatisticsMapper tradeStatisticsMapper;

    @Override
    public TradeStatisticsComparisonRespVO<TradeSummaryRespVO> getTradeSummaryComparison() {
        // 昨天的数据
        TradeSummaryRespBO yesterdayData = getTradeSummaryByDays(-1);
        // 前天的数据（用于对照昨天的数据）
        TradeSummaryRespBO beforeYesterdayData = getTradeSummaryByDays(-2);

        // 本月数据;
        TradeSummaryRespBO monthData = getTradeSummaryByMonths(0);
        // 上月数据（用于对照本月的数据）
        TradeSummaryRespBO lastMonthData = getTradeSummaryByMonths(-1);

        return TradeStatisticsConvert.INSTANCE.convert(yesterdayData, beforeYesterdayData, monthData, lastMonthData);
    }

    @Override
    public TradeStatisticsComparisonRespVO<TradeTrendSummaryRespVO> getTradeTrendSummaryComparison(LocalDateTime beginTime,
                                                                                                   LocalDateTime endTime) {
        // 统计数据
        TradeTrendSummaryRespVO value = tradeStatisticsMapper.selectByTimeBetween(beginTime, endTime);
        // 对照数据
        LocalDateTime referenceBeginTime = beginTime.minus(Duration.between(beginTime, endTime));
        TradeTrendSummaryRespVO reference = tradeStatisticsMapper.selectByTimeBetween(referenceBeginTime, beginTime);
        return TradeStatisticsConvert.INSTANCE.convert(value, reference);
    }

    @Override
    public List<TradeTrendSummaryRespVO> getTradeStatisticsList(LocalDateTime beginTime, LocalDateTime endTime) {
        return tradeStatisticsMapper.selectListByTimeBetween(beginTime, endTime);
    }

    /**
     * 统计指定日期的交易数据
     *
     * @param days 增加的天数
     * @return 交易数据
     */
    private TradeSummaryRespBO getTradeSummaryByDays(int days) {
        LocalDateTime date = LocalDateTime.now().plusDays(days);
        return tradeStatisticsMapper.selectOrderCreateCountSumAndOrderPayPriceSumByTimeBetween(
                LocalDateTimeUtil.beginOfDay(date), LocalDateTimeUtil.endOfDay(date));
    }

    /**
     * 统计指定月份的交易数据
     *
     * @param months 增加的月数
     * @return 交易数据
     */
    private TradeSummaryRespBO getTradeSummaryByMonths(int months) {
        // TODO @疯狂：可以在 LocalDateUtils 封装方法；获得月份的开始；以及结束两个方法；然后这里就可以直接调用了
        // 月份开始时间
        LocalDateTime beginOfMonth = LocalDateTime.now()
                .plusMonths(months)
                .with(TemporalAdjusters.firstDayOfMonth())
                .with(LocalTime.MIN);
        // 月份截止时间
        LocalDateTime endOfToday = LocalDateTime.now()
                .plusMonths(months)
                .with(TemporalAdjusters.lastDayOfMonth())
                .with(LocalTime.MAX);
        return tradeStatisticsMapper.selectOrderCreateCountSumAndOrderPayPriceSumByTimeBetween(beginOfMonth, endOfToday);
    }

}
