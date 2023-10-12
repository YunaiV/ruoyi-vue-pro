package cn.iocoder.yudao.module.statistics.service.trade;

import java.time.LocalDateTime;

/**
 * 分销统计 Service 接口
 *
 * @author owen
 */
public interface BrokerageStatisticsService {

    /**
     * 获取已结算的佣金金额
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 已结算的佣金金额
     */
    Integer getBrokerageSettlementPriceSummary(LocalDateTime beginTime, LocalDateTime endTime);

}
