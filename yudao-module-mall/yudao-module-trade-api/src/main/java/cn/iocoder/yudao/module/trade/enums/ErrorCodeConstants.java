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
    ErrorCode ORDER_SKU_NOT_FOUND = new ErrorCode(1011000001, "商品不存在");
    ErrorCode ORDER_SPU_NOT_SALE = new ErrorCode(1011000002, "商品不可售卖");
    ErrorCode ORDER_SKU_NOT_SALE = new ErrorCode(1011000003, "商品Sku不可售卖");
    ErrorCode ORDER_SKU_STOCK_NOT_ENOUGH = new ErrorCode(1011000004, "商品库存不足");

    // ==========  Cart 模块 1-011-001-000 ==========
    ErrorCode CARD_ITEM_NOT_FOUND = new ErrorCode(1001001000, "购物车项不存在");

}
