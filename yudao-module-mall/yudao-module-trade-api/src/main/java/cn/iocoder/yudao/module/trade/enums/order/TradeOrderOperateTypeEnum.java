package cn.iocoder.yudao.module.trade.enums.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 订单操作类型的枚举
 *
 * @author 陈賝
 * @since 2023/7/6 15:31
 */
@RequiredArgsConstructor
@Getter
public enum TradeOrderOperateTypeEnum {

    MEMBER_CREATE(1, "用户下单"),
    ADMIN_UPDATE_PRICE(2, "订单价格 {oldPayPrice} 修改，实际支付金额为 {newPayPrice} 元"),
    MEMBER_PAY(10, "用户付款成功"),
    ADMIN_UPDATE_ADDRESS(11, "收货地址修改"),
    ADMIN_DELIVERY(20, "已发货，快递公司：{deliveryName}，快递单号：{logisticsNo}"),
    MEMBER_RECEIVE(30, "用户已收货"),
    SYSTEM_RECEIVE(31, "到期未收货，系统自动确认收货"),
    ADMIN_PICK_UP_RECEIVE(32, "管理员自提收货"),
    MEMBER_COMMENT(33, "用户评价"),
    SYSTEM_COMMENT(34, "到期未评价，系统自动评价"),
    MEMBER_CANCEL(40, "取消订单"),
    SYSTEM_CANCEL(41, "到期未支付，系统自动取消订单"),
    // 42 预留：管理员取消订单
    ADMIN_CANCEL_AFTER_SALE(43, "订单全部售后，管理员自动取消订单"),
    MEMBER_DELETE(49, "删除订单"),
    ;

    /**
     * 操作类型
     */
    private final Integer type;
    /**
     * 操作描述
     */
    private final String content;

}
