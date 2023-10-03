package cn.iocoder.yudao.module.trade.api.order;

import cn.iocoder.yudao.module.trade.api.order.dto.TradeOrderSummaryRespDTO;

import java.time.LocalDateTime;

/**
 * 订单 API 接口
 *
 * @author HUIHUI
 */
public interface TradeOrderApi {

    /**
     * 获取订单状态
     *
     * @param id 订单编号
     * @return 订单状态
     */
    Integer getOrderStatus(Long id);

    /**
     * 获取订单统计
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 订单统计结果
     */
    TradeOrderSummaryRespDTO getOrderSummary(LocalDateTime beginTime, LocalDateTime endTime);

}
