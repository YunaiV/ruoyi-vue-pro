package cn.iocoder.yudao.module.trade.api.order;

import cn.iocoder.yudao.module.trade.api.order.dto.TradeOrderRespDTO;

/**
 * 订单 API 接口
 *
 * @author HUIHUI
 */
public interface TradeOrderApi {

    /**
     * 获取订单通过订单 id
     *
     * @param id id
     * @return 订单信息 Response DTO
     */
    TradeOrderRespDTO getOrder(Long id);

}
