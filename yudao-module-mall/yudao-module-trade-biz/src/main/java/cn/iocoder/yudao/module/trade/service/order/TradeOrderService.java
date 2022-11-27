package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderDeliveryReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;

/**
 * 交易订单 Service 接口
 *
 * @author LeeYan9
 * @since 2022-08-26
 */
public interface TradeOrderService {

    // =================== Order ===================

    /**
     * 【会员】创建交易订单
     *
     * @param userId 登录用户
     * @param userIp 用户 IP 地址
     * @param createReqVO 创建交易订单请求模型
     * @return 交易订单的编号
     */
    Long createOrder(Long userId, String userIp, AppTradeOrderCreateReqVO createReqVO);

    /**
     * 更新交易订单已支付
     *
     * @param id 交易订单编号
     * @param payOrderId 支付订单编号
     */
    void updateOrderPaid(Long id, Long payOrderId);

    /**
     * 【管理员】发货交易订单
     *
     * @param userId 管理员编号
     * @param deliveryReqVO 发货请求
     */
    void deliveryOrder(Long userId, TradeOrderDeliveryReqVO deliveryReqVO);

    /**
     * 获得指定用户，指定的交易订单
     *
     * @param userId 用户编号
     * @param orderId 交易订单编号
     * @return 交易订单
     */
    TradeOrderDO getOrder(Long userId, Long orderId);

    // =================== Order Item ===================

    /**
     * 获得指定用户，指定的交易订单项
     *
     * @param userId 用户编号
     * @param itemId 交易订单项编号
     * @return 交易订单项
     */
    TradeOrderItemDO getOrderItem(Long userId, Long itemId);

    /**
     * 更新交易订单项的售后状态
     *
     * @param id 交易订单项编号
     * @param oldAfterSaleStatus 当前售后状态；如果不符，更新后会抛出异常
     * @param newAfterSaleStatus 目标售后状态
     * @param refundPrice 退款金额；当订单项退款成功时，必须传递该值
     */
    void updateOrderItemAfterSaleStatus(Long id, Integer oldAfterSaleStatus,
                                        Integer newAfterSaleStatus, Integer refundPrice);

}
