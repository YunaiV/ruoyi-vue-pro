package cn.iocoder.yudao.module.statistics.service.trade;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.pay.api.wallet.PayWalletApi;
import cn.iocoder.yudao.module.pay.api.wallet.dto.WalletSummaryRespDTO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeStatisticsComparisonRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeSummaryRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeTrendSummaryRespVO;
import cn.iocoder.yudao.module.statistics.convert.trade.TradeStatisticsConvert;
import cn.iocoder.yudao.module.statistics.dal.dataobject.trade.TradeStatisticsDO;
import cn.iocoder.yudao.module.statistics.dal.mysql.trade.TradeStatisticsMapper;
import cn.iocoder.yudao.module.statistics.service.trade.bo.TradeSummaryRespBO;
import cn.iocoder.yudao.module.trade.api.aftersale.TradeAfterSaleApi;
import cn.iocoder.yudao.module.trade.api.aftersale.dto.AfterSaleSummaryRespDTO;
import cn.iocoder.yudao.module.trade.api.brokerage.TradeBrokerageApi;
import cn.iocoder.yudao.module.trade.api.order.TradeOrderApi;
import cn.iocoder.yudao.module.trade.api.order.dto.TradeOrderSummaryRespDTO;
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

    // TODO @疯狂：统计逻辑，自己服务 mapper 去统计，不要调用其它服务 API；
    // 主要的考虑点，其它服务是在线的业务，统计是离线业务，尽量不占用他们的 db 资源；
    // 统计服务，从建议使用从库，或者从 mysql 抽取到单独的 clickhouse 或者其它的大数据组件；

    @Resource
    private TradeOrderApi tradeOrderApi;
    @Resource
    private TradeAfterSaleApi tradeAfterSaleApi;
    @Resource
    private TradeBrokerageApi tradeBrokerageApi;
    @Resource
    private PayWalletApi payWalletApi;

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

    @Override
    public String statisticsYesterdayTrade() {
        // 处理统计参数
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime beginTime = LocalDateTimeUtil.beginOfDay(yesterday);
        LocalDateTime endTime = LocalDateTimeUtil.endOfDay(yesterday);
        // 统计
        StopWatch stopWatch = new StopWatch("交易统计");
        stopWatch.start("统计订单");
        TradeOrderSummaryRespDTO orderSummary = tradeOrderApi.getOrderSummary(beginTime, endTime);
        stopWatch.stop();

        stopWatch.start("统计售后");
        AfterSaleSummaryRespDTO afterSaleSummary = tradeAfterSaleApi.getAfterSaleSummary(beginTime, endTime);
        stopWatch.stop();

        stopWatch.start("统计佣金");
        Integer brokerageSettlementPrice = tradeBrokerageApi.getBrokerageSettlementPriceSummary(beginTime, endTime);
        stopWatch.stop();

        stopWatch.start("统计充值");
        WalletSummaryRespDTO walletSummary = payWalletApi.getWalletSummary(beginTime, endTime);
        stopWatch.stop();
        // 插入数据
        TradeStatisticsDO entity = TradeStatisticsConvert.INSTANCE.convert(yesterday, orderSummary, afterSaleSummary, brokerageSettlementPrice, walletSummary);
        tradeStatisticsMapper.insert(entity);
        // 返回计时结果
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
