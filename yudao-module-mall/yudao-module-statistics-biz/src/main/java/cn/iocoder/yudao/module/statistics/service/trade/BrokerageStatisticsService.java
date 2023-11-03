package cn.iocoder.yudao.module.statistics.service.trade;

import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;

import java.time.LocalDateTime;

/**
 * 分销统计 Service 接口
 *
 * @author owen
 */
public interface BrokerageStatisticsService {

    // TODO 芋艿：已经 review
    /**
     * 获取已结算的佣金金额
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 已结算的佣金金额
     */
    Integer getBrokerageSettlementPriceSummary(LocalDateTime beginTime, LocalDateTime endTime);

    // TODO 芋艿：已经 review
    /**
     * 获取指定状态的提现记录数量
     *
     * @param status 提现记录状态
     * @return 提现记录数量
     */
    Long getWithdrawCountByStatus(BrokerageWithdrawStatusEnum status);

}
