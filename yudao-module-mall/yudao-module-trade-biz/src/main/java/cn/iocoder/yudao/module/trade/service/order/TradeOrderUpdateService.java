package cn.iocoder.yudao.module.trade.service.order;

import cn.iocoder.yudao.module.trade.controller.admin.order.vo.TradeOrderDeliveryReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderSettlementReqVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderSettlementRespVO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.item.AppTradeOrderItemCommentCreateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;

/**
 * 交易订单【写】Service 接口
 *
 * @author LeeYan9
 * @since 2022-08-26
 */
public interface TradeOrderUpdateService {

    // =================== Order ===================

    /**
     * 获得订单结算信息
     *
     * @param userId 登录用户
     * @param settlementReqVO 订单结算请求
     * @return 订单结算结果
     */
    AppTradeOrderSettlementRespVO settlementOrder(Long userId, AppTradeOrderSettlementReqVO settlementReqVO);

    /**
     * 【会员】创建交易订单
     *
     * @param userId 登录用户
     * @param userIp 用户 IP 地址
     * @param createReqVO 创建交易订单请求模型
     * @return 交易订单的
     */
    TradeOrderDO createOrder(Long userId, String userIp, AppTradeOrderCreateReqVO createReqVO);

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
     * 【会员】收货交易订单
     *
     * @param userId 用户编号
     * @param id     订单编号
     */
    void receiveOrder(Long userId, Long id);

    // =================== Order Item ===================

    /**
     * 更新交易订单项的售后状态
     *
     * @param id 交易订单项编号
     * @param oldAfterSaleStatus 当前售后状态；如果不符，更新后会抛出异常
     * @param newAfterSaleStatus 目标售后状态
     */
    default void updateOrderItemAfterSaleStatus(Long id, Integer oldAfterSaleStatus, Integer newAfterSaleStatus) {
        updateOrderItemAfterSaleStatus(id, oldAfterSaleStatus, newAfterSaleStatus, null, null);
    }

    /**
     * 更新交易订单项的售后状态
     *
     * @param id 交易订单项编号
     * @param oldAfterSaleStatus 当前售后状态；如果不符，更新后会抛出异常
     * @param newAfterSaleStatus 目标售后状态
     * @param afterSaleId 售后单编号；当订单项发起售后时，必须传递该字段
     * @param refundPrice 退款金额；当订单项退款成功时，必须传递该值
     */
    void updateOrderItemAfterSaleStatus(Long id, Integer oldAfterSaleStatus, Integer newAfterSaleStatus,
                                        Long afterSaleId, Integer refundPrice);

    /**
     * 创建订单项的评论
     *
     * @param userId      用户编号
     * @param createReqVO 创建请求
     * @return 得到评价 id
     */
    Long createOrderItemComment(Long userId, AppTradeOrderItemCommentCreateReqVO createReqVO);

}
