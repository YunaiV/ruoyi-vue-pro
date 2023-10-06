package cn.iocoder.yudao.module.statistics.service.trade;

import cn.iocoder.yudao.module.statistics.service.trade.bo.TradeOrderSummaryRespBO;

import java.time.LocalDateTime;

/**
 * 交易订单统计 Service 接口
 *
 * @author owen
 */
public interface TradeOrderStatisticsService {

    /**
     * 获取订单统计
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 订单统计结果
     */
    TradeOrderSummaryRespBO getOrderSummary(LocalDateTime beginTime, LocalDateTime endTime);

}
