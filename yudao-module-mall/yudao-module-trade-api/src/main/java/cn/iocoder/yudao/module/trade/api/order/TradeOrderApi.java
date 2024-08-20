package cn.iocoder.yudao.module.trade.api.order;

import cn.iocoder.yudao.module.trade.api.order.dto.TradeOrderRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 订单 API 接口
 *
 * @author HUIHUI
 */
public interface TradeOrderApi {

    /**
     * 获得订单列表
     *
     * @param ids 订单编号数组
     * @return 订单列表
     */
    List<TradeOrderRespDTO> getOrderList(Collection<Long> ids);

    /**
     * 获得订单
     *
     * @param id 订单编号
     * @return 订单
     */
    TradeOrderRespDTO getOrder(Long id);

    /**
     * 取消支付订单
     *
     * @param userId  用户编号
     * @param orderId 订单编号
     */
    void cancelPaidOrder(Long userId, Long orderId);

}
