package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;

import java.util.List;

/**
 * 订单活动特殊逻辑处理器 handler 接口
 * 提供订单生命周期钩子接口；订单创建前、订单创建后、订单支付后、订单取消
 *
 * @author HUIHUI
 */
public interface TradeOrderHandler {

    /**
     * 订单创建前
     *
     * @param order 订单
     * @param orderItems 订单项
     */
    default void beforeOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {}

    /**
     * 订单创建后
     *
     * @param order 订单
     * @param orderItems 订单项
     */
    default void afterOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {}

    /**
     * 支付订单后
     *
     * @param order 订单
     * @param orderItems 订单项
     */
    default void afterPayOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
    }

    /**
     * 订单取消
     */
    default void cancelOrder() {}

}
