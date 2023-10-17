package cn.iocoder.yudao.module.statistics.service.trade;

import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeStatisticsComparisonRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeSummaryRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeTrendSummaryRespVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易统计 Service 接口
 *
 * @author owen
 */
public interface TradeStatisticsService {

    /**
     * 获得交易统计
     *
     * @return 统计数据对照
     */
    TradeStatisticsComparisonRespVO<TradeSummaryRespVO> getTradeSummaryComparison();

    /**
     * 获得交易状况统计对照
     *
     * @return 统计数据对照
     */
    TradeStatisticsComparisonRespVO<TradeTrendSummaryRespVO> getTradeTrendSummaryComparison(
            LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获得交易状况统计
     *
     * @return 统计数据对照
     */
    Integer getExpensePrice(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获得交易状况明细
     *
     * @return 统计数据列表
     */
    List<TradeTrendSummaryRespVO> getTradeStatisticsList(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 统计昨日交易
     *
     * @return 耗时
     */
    String statisticsYesterdayTrade();

}
