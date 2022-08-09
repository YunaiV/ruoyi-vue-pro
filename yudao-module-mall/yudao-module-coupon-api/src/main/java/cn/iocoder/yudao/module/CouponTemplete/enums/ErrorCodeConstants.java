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

    // ========== COUPON分类相关 1010002000 ============
    ErrorCode COUPON_NOT_EXISTS = new ErrorCode(1010001000, "优惠券模板不存在");


}

