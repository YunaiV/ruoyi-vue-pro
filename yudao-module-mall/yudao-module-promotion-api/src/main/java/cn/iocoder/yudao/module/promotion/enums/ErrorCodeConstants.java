package cn.iocoder.yudao.module.promotion.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * promotion 错误码枚举类
 *
 * market 系统，使用 1-003-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 促销活动相关 1003001000 ============
    ErrorCode DISCOUNT_ACTIVITY_NOT_EXISTS = new ErrorCode(1003001000, "限时折扣活动不存在");
    ErrorCode DISCOUNT_ACTIVITY_SPU_CONFLICTS = new ErrorCode(1003006001, "存在商品参加了其它限时折扣活动");
    ErrorCode DISCOUNT_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED = new ErrorCode(1003006002, "限时折扣活动已关闭，不能修改");
    ErrorCode DISCOUNT_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED = new ErrorCode(1003006003, "限时折扣活动未关闭，不能删除");
    ErrorCode DISCOUNT_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED = new ErrorCode(1003006004, "限时折扣活动已关闭，不能重复关闭");
    ErrorCode DISCOUNT_ACTIVITY_CLOSE_FAIL_STATUS_END = new ErrorCode(1003006004, "限时折扣活动已结束，不能关闭");

    // ========== Banner 相关 1003002000 ============
    ErrorCode BANNER_NOT_EXISTS = new ErrorCode(1003002000, "Banner 不存在");

    // ========== Coupon 相关 1003003000 ============
    ErrorCode COUPON_NO_MATCH_SPU = new ErrorCode(1003003000, "优惠劵没有可使用的商品！");
    ErrorCode COUPON_NO_MATCH_MIN_PRICE = new ErrorCode(1003003000, "所结算的商品中未满足使用的金额");

    // ========== 优惠劵模板 1003004000 ==========
    ErrorCode COUPON_TEMPLATE_NOT_EXISTS = new ErrorCode(1003004000, "优惠劵模板不存在");
    ErrorCode COUPON_TEMPLATE_TOTAL_COUNT_TOO_SMALL = new ErrorCode(1003004001, "发放数量不能小于已领取数量({})");

    // ========== 优惠劵模板 1003005000 ==========
    ErrorCode COUPON_NOT_EXISTS = new ErrorCode(1003005000, "优惠券不存在");
    ErrorCode COUPON_DELETE_FAIL_USED = new ErrorCode(1003005001, "回收优惠劵失败，优惠劵已被使用");
    ErrorCode COUPON_STATUS_NOT_UNUSED = new ErrorCode(1006003003, "优惠劵不处于待使用状态");
    ErrorCode COUPON_VALID_TIME_NOT_NOW = new ErrorCode(1006003004, "优惠券不在使用时间范围内");

    // ========== 满减送活动 1003006000 ==========
    ErrorCode REWARD_ACTIVITY_NOT_EXISTS = new ErrorCode(1003006000, "满减送活动不存在");
    ErrorCode REWARD_ACTIVITY_SPU_CONFLICTS = new ErrorCode(1003006001, "存在商品参加了其它满减送活动");
    ErrorCode REWARD_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED = new ErrorCode(1003006002, "满减送活动已关闭，不能修改");
    ErrorCode REWARD_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED = new ErrorCode(1003006003, "满减送活动未关闭，不能删除");
    ErrorCode REWARD_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED = new ErrorCode(1003006004, "满减送活动已关闭，不能重复关闭");
    ErrorCode REWARD_ACTIVITY_CLOSE_FAIL_STATUS_END = new ErrorCode(1003006004, "满减送活动已结束，不能关闭");

    // ========== Price 相关 1003007000 ============
    ErrorCode PRICE_CALCULATE_PAY_PRICE_ILLEGAL = new ErrorCode(1003007000, "支付价格计算异常，原因：价格小于等于 0");

}
