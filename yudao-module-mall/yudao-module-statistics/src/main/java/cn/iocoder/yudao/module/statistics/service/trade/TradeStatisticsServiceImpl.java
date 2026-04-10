package cn.iocoder.yudao.module.statistics.service.trade;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.statistics.controller.admin.common.vo.DataComparisonRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeTrendSummaryRespVO;
import cn.iocoder.yudao.module.statistics.convert.trade.TradeStatisticsConvert;
import cn.iocoder.yudao.module.statistics.dal.dataobject.trade.TradeStatisticsDO;
import cn.iocoder.yudao.module.statistics.dal.mysql.trade.TradeStatisticsMapper;
import cn.iocoder.yudao.module.statistics.service.pay.PayWalletStatisticsService;
import cn.iocoder.yudao.module.statistics.service.trade.bo.AfterSaleSummaryRespBO;
import cn.iocoder.yudao.module.statistics.service.trade.bo.TradeOrderSummaryRespBO;
import cn.iocoder.yudao.module.statistics.service.trade.bo.TradeSummaryRespBO;
import cn.iocoder.yudao.module.statistics.service.trade.bo.WalletSummaryRespBO;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    public TradeSummaryRespBO getTradeSummaryByDays(int days) {
        LocalDateTime date = LocalDateTime.now().plusDays(days);
        return tradeStatisticsMapper.selectOrderCreateCountSumAndOrderPayPriceSumByTimeBetween(
                LocalDateTimeUtil.beginOfDay(date), LocalDateTimeUtil.endOfDay(date));
    }

    @Override
    public TradeSummaryRespBO getTradeSummaryByMonths(int months) {
        LocalDateTime monthDate = LocalDateTime.now().plusMonths(months);
        return tradeStatisticsMapper.selectOrderCreateCountSumAndOrderPayPriceSumByTimeBetween(
                LocalDateTimeUtils.beginOfMonth(monthDate), LocalDateTimeUtils.endOfMonth(monthDate));
    }

    @Override
    public DataComparisonRespVO<TradeTrendSummaryRespVO> getTradeStatisticsAnalyse(LocalDateTime beginTime,
                                                                                        LocalDateTime endTime) {
        // 统计数据
        TradeTrendSummaryRespVO value = tradeStatisticsMapper.selectVoByTimeBetween(beginTime, endTime);
        // 对照数据
        LocalDateTime referenceBeginTime = beginTime.minus(Duration.between(beginTime, endTime));
        TradeTrendSummaryRespVO reference = tradeStatisticsMapper.selectVoByTimeBetween(referenceBeginTime, beginTime);
        return TradeStatisticsConvert.INSTANCE.convert(value, reference);
    }

    @Override
    public Integer getExpensePrice(LocalDateTime beginTime, LocalDateTime endTime) {
        return tradeStatisticsMapper.selectExpensePriceByTimeBetween(beginTime, endTime);
    }

    @Override
    public List<TradeStatisticsDO> getTradeStatisticsList(LocalDateTime beginTime, LocalDateTime endTime) {
        return tradeStatisticsMapper.selectListByTimeBetween(beginTime, endTime);
    }

    @Override
    public String statisticsTrade(Integer days) {
        LocalDateTime today = LocalDateTime.now();
        return IntStream.rangeClosed(1, days)
                .mapToObj(day -> statisticsTrade(today.minusDays(day)))
                .sorted()
                .collect(Collectors.joining("\n"));
    }

    /**
     * 统计交易数据
     *
     * @param date 需要统计的日期
     * @return 统计结果
     */
    private String statisticsTrade(LocalDateTime date) {
        // 1. 处理统计时间范围
        LocalDateTime beginTime = LocalDateTimeUtil.beginOfDay(date);
        LocalDateTime endTime = LocalDateTimeUtil.endOfDay(date);
        String dateStr = DatePattern.NORM_DATE_FORMATTER.format(date);
        // 2. 检查该日是否已经统计过
        TradeStatisticsDO entity = tradeStatisticsMapper.selectByTimeBetween(beginTime, endTime);
        if (entity != null) {
            return dateStr + " 数据已存在，如果需要重新统计，请先删除对应的数据";
        }

        // 3. 从各个数据表，统计对应数据
        StopWatch stopWatch = new StopWatch(dateStr);
        // 3.1 统计订单
        stopWatch.start("统计订单");
        TradeOrderSummaryRespBO orderSummary = tradeOrderStatisticsService.getOrderSummary(beginTime, endTime);
        stopWatch.stop();
        // 3.2 统计售后
        stopWatch.start("统计售后");
        AfterSaleSummaryRespBO afterSaleSummary = afterSaleStatisticsService.getAfterSaleSummary(beginTime, endTime);
        stopWatch.stop();
        // 3.3 统计佣金
        stopWatch.start("统计佣金");
        Integer brokerageSettlementPrice = brokerageStatisticsService.getBrokerageSettlementPriceSummary(beginTime, endTime);
        stopWatch.stop();
        // 3.4 统计充值
        stopWatch.start("统计充值");
        WalletSummaryRespBO walletSummary = payWalletStatisticsService.getWalletSummary(beginTime, endTime);
        stopWatch.stop();

        // 4. 插入数据
        entity = TradeStatisticsConvert.INSTANCE.convert(date, orderSummary, afterSaleSummary, brokerageSettlementPrice,
                walletSummary);
        tradeStatisticsMapper.insert(entity);
        return stopWatch.prettyPrint();
    }

}
