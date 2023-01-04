package cn.iocoder.yudao.module.trade.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * 交易 错误码枚举类
 * 交易系统，使用 1-011-000-000 段
 *
 * @author LeeYan9
 * @since 2022-08-26
 */
public interface ErrorCodeConstants {

    // ==========  Order 模块 1-011-000-000 ==========
    ErrorCode ORDER_CREATE_SKU_NOT_FOUND = new ErrorCode(1011000001, "商品 SKU 不存在");
    ErrorCode ORDER_CREATE_SPU_NOT_SALE = new ErrorCode(1011000002, "商品 SPU 不可售卖");
    ErrorCode ORDER_CREATE_SKU_NOT_SALE = new ErrorCode(1011000003, "商品 SKU 不可售卖");
    ErrorCode ORDER_CREATE_SKU_STOCK_NOT_ENOUGH = new ErrorCode(1011000004, "商品 SKU 库存不足");
    ErrorCode ORDER_CREATE_SPU_NOT_FOUND = new ErrorCode(1011000005, "商品 SPU 不可售卖");
    ErrorCode ORDER_CREATE_ADDRESS_NOT_FOUND = new ErrorCode(1011000006, "收货地址不存在");

    ErrorCode ORDER_ITEM_NOT_FOUND = new ErrorCode(1011000010, "交易订单项不存在");
    ErrorCode ORDER_NOT_FOUND = new ErrorCode(1011000011, "交易订单不存在");
    ErrorCode ORDER_ITEM_UPDATE_AFTER_SALE_STATUS_FAIL = new ErrorCode(1011000012, "交易订单项更新售后状态失败，请重试");
    ErrorCode ORDER_UPDATE_PAID_STATUS_NOT_UNPAID = new ErrorCode(1011000013, "交易订单更新支付状态失败，订单不是【未支付】状态");
    ErrorCode ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR = new ErrorCode(1011000014, "交易订单更新支付状态失败，支付单编号不匹配");
    ErrorCode ORDER_UPDATE_PAID_FAIL_PAY_ORDER_STATUS_NOT_SUCCESS = new ErrorCode(1011000015, "交易订单更新支付状态失败，支付单状态不是【支付成功】状态");
    ErrorCode ORDER_UPDATE_PAID_FAIL_PAY_PRICE_NOT_MATCH = new ErrorCode(1011000016, "交易订单更新支付状态失败，支付单金额不匹配");
    ErrorCode ORDER_DELIVERY_FAIL_STATUS_NOT_UNDELIVERED = new ErrorCode(1011000017, "交易订单发货失败，订单不是【待发货】状态");
    ErrorCode ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED = new ErrorCode(1011000018, "交易订单收货失败，订单不是【待收货】状态");

    // ==========  After Sale 模块 1-011-000-000 ==========
    ErrorCode AFTER_SALE_NOT_FOUND = new ErrorCode(1011000100, "售后单不存在");
    ErrorCode AFTER_SALE_CREATE_FAIL_REFUND_PRICE_ERROR = new ErrorCode(1011000101, "申请退款金额错误");
    ErrorCode AFTER_SALE_CREATE_FAIL_ORDER_STATUS_CANCELED = new ErrorCode(1011000102, "订单已关闭，无法申请售后");
    ErrorCode AFTER_SALE_CREATE_FAIL_ORDER_STATUS_NO_PAID = new ErrorCode(1011000103, "订单未支付，无法申请售后");
    ErrorCode AFTER_SALE_CREATE_FAIL_ORDER_STATUS_NO_DELIVERED = new ErrorCode(1011000104, "订单未发货，无法申请【退货退款】售后");
    ErrorCode AFTER_SALE_CREATE_FAIL_ORDER_ITEM_APPLIED = new ErrorCode(1011000105, "订单项已申请售后，无法重复申请");
    ErrorCode AFTER_SALE_AUDIT_FAIL_STATUS_NOT_APPLY = new ErrorCode(1011000106, "审批失败，售后状态不处于审批中");
    ErrorCode AFTER_SALE_UPDATE_STATUS_FAIL = new ErrorCode(1011000107, "操作售后单失败，请刷新后重试");
    ErrorCode AFTER_SALE_DELIVERY_FAIL_STATUS_NOT_SELLER_AGREE = new ErrorCode(1011000108, "退货失败，售后单状态不处于【待买家退货】");
    ErrorCode AFTER_SALE_CONFIRM_FAIL_STATUS_NOT_BUYER_DELIVERY = new ErrorCode(1011000109, "确认收货失败，售后单状态不处于【待确认收货】");
    ErrorCode AFTER_SALE_REFUND_FAIL_STATUS_NOT_WAIT_REFUND = new ErrorCode(1011000110, "退款失败，售后单状态不是【待退款】");
    ErrorCode AFTER_SALE_CANCEL_FAIL_STATUS_NOT_APPLY_OR_AGREE = new ErrorCode(1011000111, "取消售后单失败，售后单状态不是【待审核】或【卖家同意】");

    // ==========  Cart 模块 1-011-001-000 ==========
    ErrorCode CARD_ITEM_NOT_FOUND = new ErrorCode(1011002000, "购物车项不存在");

}
