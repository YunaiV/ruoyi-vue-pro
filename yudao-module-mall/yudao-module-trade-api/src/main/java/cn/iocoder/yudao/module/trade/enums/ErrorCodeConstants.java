package cn.iocoder.yudao.module.trade.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Trade 错误码枚举类
 * trade 系统，使用 1-011-000-000 段
 *
 * @author LeeYan9
 * @since 2022-08-26
 */
public interface ErrorCodeConstants {

    // ==========  Order 模块 1011000000 ==========
    ErrorCode ORDER_CREATE_SKU_NOT_FOUND = new ErrorCode(1011000001, "商品 SKU 不存在");
    ErrorCode ORDER_CREATE_SPU_NOT_SALE = new ErrorCode(1011000002, "商品 SPU 不可售卖");
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
    ErrorCode ORDER_COMMENT_FAIL_STATUS_NOT_COMPLETED = new ErrorCode(1011000019, "创建交易订单项的评价失败，订单不是【已完成】状态");
    ErrorCode ORDER_COMMENT_STATUS_NOT_FALSE = new ErrorCode(1011000020, "创建交易订单项的评价失败，订单已评价");
    ErrorCode ORDER_DELIVERY_FAIL_REFUND_STATUS_NOT_NONE = new ErrorCode(1011000021, "交易订单发货失败，订单已退款或部分退款");
    ErrorCode ORDER_DELIVERY_FAIL_COMBINATION_RECORD_STATUS_NOT_SUCCESS = new ErrorCode(1011000022, "交易订单发货失败，拼团未成功");

    // ==========  After Sale 模块 1011000100 ==========
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

    // ==========  Cart 模块 1011002000 ==========
    ErrorCode CARD_ITEM_NOT_FOUND = new ErrorCode(1011002000, "购物车项不存在");

    // ========== Price 相关 1011003000 ============
    ErrorCode PRICE_CALCULATE_PAY_PRICE_ILLEGAL = new ErrorCode(1011003000, "支付价格计算异常，原因：价格小于等于 0");
    ErrorCode PRICE_CALCULATE_DELIVERY_PRICE_USER_ADDR_IS_EMPTY = new ErrorCode(1011003001, "计算快递运费异常，收件人地址编号为空");
    ErrorCode PRICE_CALCULATE_DELIVERY_PRICE_TEMPLATE_NOT_FOUND = new ErrorCode(1011003002, "计算快递运费异常，找不到对应的运费模板");

    // ==========  物流 Express 模块 1011004000 ==========
    ErrorCode EXPRESS_NOT_EXISTS = new ErrorCode(1011004000, "快递公司不存在");
    ErrorCode EXPRESS_CODE_DUPLICATE = new ErrorCode(1011004001, "已经存在该编码的快递公司");
    ErrorCode EXPRESS_CLIENT_NOT_PROVIDE = new ErrorCode(1011004002, "需要接入快递服务商，比如【快递100】");

    ErrorCode EXPRESS_API_QUERY_ERROR = new ErrorCode(1011004101, "快递查询接口异常");
    ErrorCode EXPRESS_API_QUERY_FAILED = new ErrorCode(1011004102, "快递查询返回失败，原因：{}");

    // ==========  物流 Template 模块 1011005000 ==========
    ErrorCode EXPRESS_TEMPLATE_NAME_DUPLICATE = new ErrorCode(1011005000, "已经存在该运费模板名");
    ErrorCode EXPRESS_TEMPLATE_NOT_EXISTS = new ErrorCode(1011005001, "运费模板不存在");

    // ==========  物流 PICK_UP 模块 1011006000 ==========
    ErrorCode PICK_UP_STORE_NOT_EXISTS = new ErrorCode(1011006000, "自提门店不存在");

    // ==========  物流 PICK_UP 模块 1011007000 ==========
    ErrorCode ORDER_DELIVERY_FAILED_ITEMS_NOT_EMPTY = new ErrorCode(1011007000, "订单发货失败，请选择发货商品");
    ErrorCode ORDER_DELIVERY_FAILED_ITEM_NOT_EXISTS = new ErrorCode(1011007001, "订单发货失败，所选发货商品不存在");
    ErrorCode ORDER_DELIVERY_FAILED_ITEM_ALREADY_DELIVERY = new ErrorCode(1011007001, "订单发货失败，所选商品已发货");
}
