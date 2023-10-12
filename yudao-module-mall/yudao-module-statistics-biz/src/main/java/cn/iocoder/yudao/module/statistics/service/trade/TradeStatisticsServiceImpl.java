package cn.iocoder.yudao.module.statistics.service.trade;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.statistics.service.pay.PayWalletStatisticsService;
import cn.iocoder.yudao.module.statistics.service.trade.bo.WalletSummaryRespBO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeStatisticsComparisonRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeSummaryRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeTrendSummaryRespVO;
import cn.iocoder.yudao.module.statistics.convert.trade.TradeStatisticsConvert;
import cn.iocoder.yudao.module.statistics.dal.dataobject.trade.TradeStatisticsDO;
import cn.iocoder.yudao.module.statistics.dal.mysql.trade.TradeStatisticsMapper;
import cn.iocoder.yudao.module.statistics.service.trade.bo.TradeSummaryRespBO;
import cn.iocoder.yudao.module.statistics.service.trade.bo.AfterSaleSummaryRespBO;
import cn.iocoder.yudao.module.statistics.service.trade.bo.TradeOrderSummaryRespBO;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
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

    @Resource
    private TradeOrderStatisticsService tradeOrderStatisticsService;
    @Resource
    private AfterSaleStatisticsService afterSaleStatisticsService;
    @Resource
    private BrokerageStatisticsService brokerageStatisticsService;
    @Resource
    private PayWalletStatisticsService payWalletStatisticsService;

    @Override
    public TradeStatisticsComparisonRespVO<TradeSummaryRespVO> getTradeSummaryComparison() {
        // 1.1 昨天的数据
        TradeSummaryRespBO yesterdayData = getTradeSummaryByDays(-1);
        // 1.2 前天的数据（用于对照昨天的数据）
        TradeSummaryRespBO beforeYesterdayData = getTradeSummaryByDays(-2);
        // 2.1 本月数据
        TradeSummaryRespBO monthData = getTradeSummaryByMonths(0);
        // 2.2 上月数据（用于对照本月的数据）
        TradeSummaryRespBO lastMonthData = getTradeSummaryByMonths(-1);
        // 转换返回
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
    public Integer getExpensePrice(LocalDateTime beginTime, LocalDateTime endTime) {
        return tradeStatisticsMapper.selectExpensePriceByTimeBetween(beginTime, endTime);
    }

    @Override
    public List<TradeTrendSummaryRespVO> getTradeStatisticsList(LocalDateTime beginTime, LocalDateTime endTime) {
        return tradeStatisticsMapper.selectListByTimeBetween(beginTime, endTime);
    }

    @Override
    public String statisticsYesterdayTrade() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime beginTime = LocalDateTimeUtil.beginOfDay(yesterday);
        LocalDateTime endTime = LocalDateTimeUtil.endOfDay(yesterday);
        // 1.1 统计订单
        StopWatch stopWatch = new StopWatch("交易统计");
        stopWatch.start("统计订单");
        TradeOrderSummaryRespBO orderSummary = tradeOrderStatisticsService.getOrderSummary(beginTime, endTime);
        stopWatch.stop();
        // 1.2 统计售后
        stopWatch.start("统计售后");
        AfterSaleSummaryRespBO afterSaleSummary = afterSaleStatisticsService.getAfterSaleSummary(beginTime, endTime);
        stopWatch.stop();
        // 1.3 统计佣金
        stopWatch.start("统计佣金");
        Integer brokerageSettlementPrice = brokerageStatisticsService.getBrokerageSettlementPriceSummary(beginTime, endTime);
        stopWatch.stop();
        // 1.4 统计充值
        stopWatch.start("统计充值");
        WalletSummaryRespBO walletSummary = payWalletStatisticsService.getWalletSummary(beginTime, endTime);
        stopWatch.stop();

        // 2. 插入数据
        TradeStatisticsDO entity = TradeStatisticsConvert.INSTANCE.convert(yesterday, orderSummary, afterSaleSummary,
                brokerageSettlementPrice, walletSummary);
        tradeStatisticsMapper.insert(entity);
        return stopWatch.prettyPrint();
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
        LocalDateTime monthDate = LocalDateTime.now().plusMonths(months);
        return tradeStatisticsMapper.selectOrderCreateCountSumAndOrderPayPriceSumByTimeBetween(
                LocalDateTimeUtils.beginOfMonth(monthDate), LocalDateTimeUtils.endOfMonth(monthDate));
    }

}
