package cn.iocoder.yudao.module.CouponTemplete.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * coupon 优惠券错误码枚举类
 *
 * coupon 优惠券系统，使用 1-010-000-000 段
 */
public interface ErrorCodeConstants {
    // ========== COUPON分类相关 1010001000 ============

    ErrorCode COUPON_TEMPLETE_NOT_EXISTS = new ErrorCode(1010001000, "优惠券模板不存在");
    ErrorCode MONEY_NOT_NULL = new ErrorCode(1010001001, "当type为reward时需要添加发放面额不能为空");
    ErrorCode DISCOUNT_NOT_NULL = new ErrorCode(1010001001, "当type为discount时需要添加折扣不能为空");
    ErrorCode DISCOUNT_LIMIT_NOT_NULL = new ErrorCode(1010001001, "当type为discount时可选择性添加最多折扣金额不能为空");
    ErrorCode MIN_MAX_NOT_NULL = new ErrorCode(1010001001, "当type为radom时需要添加最低金额");
    ErrorCode START_END_TIME_NOT_NULL = new ErrorCode(1010001001, "使用开始日期,使用结束日期不能为空");
    ErrorCode FIXED_TERM_NOT_NULL = new ErrorCode(1010001001, "领取之日起或者次日N天内有效不能为空");


    // ========== COUPON分类相关 1010002000 ============
    ErrorCode COUPON_NOT_EXISTS = new ErrorCode(1010001000, "优惠券模板不存在");


}

