package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.iocoder.yudao.module.trade.service.order.bo.TradeAfterOrderCreateReqBO;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeAfterPayOrderReqBO;
import cn.iocoder.yudao.module.trade.service.order.bo.TradeBeforeOrderCreateReqBO;

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
     * @param reqBO 请求
     */
    default void beforeOrderCreate(TradeBeforeOrderCreateReqBO reqBO) {}

    /**
     * 订单创建后
     *
     * @param reqBO 请求
     */
    default void afterOrderCreate(TradeAfterOrderCreateReqBO reqBO) {}

    /**
     * 支付订单后
     *
     * @param reqBO 请求
     */
    default void afterPayOrder(TradeAfterPayOrderReqBO reqBO) {}

    /**
     * 订单取消
     */
    default void cancelOrder() {}

}
