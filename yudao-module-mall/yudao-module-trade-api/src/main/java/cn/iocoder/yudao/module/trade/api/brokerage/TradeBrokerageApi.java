package cn.iocoder.yudao.module.trade.api.brokerage;

import java.time.LocalDateTime;

/**
 * 分销 API 接口
 *
 * @author owen
 */
public interface TradeBrokerageApi {

    /**
     * 获取已结算的佣金金额
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 已结算的佣金金额
     */
    Integer getBrokerageSettlementPriceSummary(LocalDateTime beginTime, LocalDateTime endTime);

}
