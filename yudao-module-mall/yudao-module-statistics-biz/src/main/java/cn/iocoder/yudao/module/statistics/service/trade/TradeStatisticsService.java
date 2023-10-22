package cn.iocoder.yudao.module.statistics.service.trade;

import cn.iocoder.yudao.module.statistics.controller.admin.common.vo.DataComparisonRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.trade.vo.TradeTrendSummaryRespVO;
import cn.iocoder.yudao.module.statistics.dal.dataobject.trade.TradeStatisticsDO;
import cn.iocoder.yudao.module.statistics.service.trade.bo.TradeSummaryRespBO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易统计 Service 接口
 *
 * @author owen
 */
public interface TradeStatisticsService {

    /**
     * 获得交易状况统计对照
     *
     * @return 统计数据对照
     */
    DataComparisonRespVO<TradeTrendSummaryRespVO> getTradeTrendSummaryComparison(
            LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获得交易状况统计
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 统计数据对照
     */
    Integer getExpensePrice(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获得交易状况明细
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 统计数据列表
     */
    List<TradeStatisticsDO> getTradeStatisticsList(LocalDateTime beginTime, LocalDateTime endTime);

    // TODO 芋艿：已经 review；
    /**
     * 统计指定天数的交易数据
     *
     * @return 统计结果
     */
    String statisticsTrade(Integer days);

    // TODO 芋艿：已经 review
    /**
     * 统计指定日期的交易数据
     *
     * @param days 增加的天数
     * @return 交易数据
     */
    TradeSummaryRespBO getTradeSummaryByDays(int days);

    // TODO 芋艿：已经 review
    /**
     * 统计指定月份的交易数据
     *
     * @param months 增加的月数
     * @return 交易数据
     */
    TradeSummaryRespBO getTradeSummaryByMonths(int months);

}
