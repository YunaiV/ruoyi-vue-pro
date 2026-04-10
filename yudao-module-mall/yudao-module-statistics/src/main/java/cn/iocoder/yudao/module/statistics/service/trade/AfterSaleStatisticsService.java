package cn.iocoder.yudao.module.statistics.service.trade;

import cn.iocoder.yudao.module.statistics.service.trade.bo.AfterSaleSummaryRespBO;
import cn.iocoder.yudao.module.trade.enums.aftersale.AfterSaleStatusEnum;

import java.time.LocalDateTime;

/**
 * 售后统计 Service 接口
 *
 * @author owen
 */
public interface AfterSaleStatisticsService {

    /**
     * 获取售后单统计
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 售后统计结果
     */
    AfterSaleSummaryRespBO getAfterSaleSummary(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取指定状态的售后订单数量
     *
     * @param status 售后状态
     * @return 售后订单数量
     */
    Long getCountByStatus(AfterSaleStatusEnum status);

}
