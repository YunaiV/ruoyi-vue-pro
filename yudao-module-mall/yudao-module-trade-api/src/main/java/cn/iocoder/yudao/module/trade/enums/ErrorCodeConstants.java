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
    ErrorCode ORDER_NOT_FOUND = new ErrorCode(1011000010, "交易订单不存在");

    // ==========  After Sale 模块 1-011-000-000 ==========
    ErrorCode AFTER_SALE_NOT_FOUND = new ErrorCode(1011000100, "售后单不存在");
    ErrorCode AFTER_SALE_CREATE_FAIL_REFUND_PRICE_ERROR = new ErrorCode(1011000101, "申请退款金额错误");
    ErrorCode AFTER_SALE_CREATE_FAIL_ORDER_STATUS_CANCELED = new ErrorCode(1011000102, "订单已关闭，无法申请售后");
    ErrorCode AFTER_SALE_CREATE_FAIL_ORDER_STATUS_NO_PAID = new ErrorCode(1011000103, "订单未支付，无法申请售后");
    ErrorCode AFTER_SALE_CREATE_FAIL_ORDER_STATUS_NO_DELIVERED = new ErrorCode(1011000104, "订单未发货，无法申请【退货退款】售后");
    ErrorCode AFTER_SALE_CREATE_FAIL_ORDER_ITEM_APPLIED = new ErrorCode(1011000105, "订单项已申请售后，无法重复申请");

    // ==========  Cart 模块 1-011-001-000 ==========
    ErrorCode CARD_ITEM_NOT_FOUND = new ErrorCode(1011002000, "购物车项不存在");


}
