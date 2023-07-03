package cn.iocoder.yudao.module.promotion.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Promotion 错误码枚举类
 *
 * promotion 系统，使用 1-013-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 促销活动相关 1013001000 ============
    ErrorCode DISCOUNT_ACTIVITY_NOT_EXISTS = new ErrorCode(1013001000, "限时折扣活动不存在");
    ErrorCode DISCOUNT_ACTIVITY_SPU_CONFLICTS = new ErrorCode(1013001001, "存在商品参加了其它限时折扣活动");
    ErrorCode DISCOUNT_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED = new ErrorCode(1013001002, "限时折扣活动已关闭，不能修改");
    ErrorCode DISCOUNT_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED = new ErrorCode(1013001003, "限时折扣活动未关闭，不能删除");
    ErrorCode DISCOUNT_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED = new ErrorCode(1013001004, "限时折扣活动已关闭，不能重复关闭");
    ErrorCode DISCOUNT_ACTIVITY_CLOSE_FAIL_STATUS_END = new ErrorCode(1013001005, "限时折扣活动已结束，不能关闭");

    // ========== Banner 相关 1013002000 ============
    ErrorCode BANNER_NOT_EXISTS = new ErrorCode(1013002000, "Banner 不存在");

    // ========== Coupon 相关 1013003000 ============
    ErrorCode COUPON_NO_MATCH_SPU = new ErrorCode(1013003000, "优惠劵没有可使用的商品！");
    ErrorCode COUPON_NO_MATCH_MIN_PRICE = new ErrorCode(1013003001, "所结算的商品中未满足使用的金额");

    // ========== 优惠劵模板 1013004000 ==========
    ErrorCode COUPON_TEMPLATE_NOT_EXISTS = new ErrorCode(1013004000, "优惠劵模板不存在");
    ErrorCode COUPON_TEMPLATE_TOTAL_COUNT_TOO_SMALL = new ErrorCode(1013004001, "发放数量不能小于已领取数量({})");

    // ========== 优惠劵模板 1013005000 ==========
    ErrorCode COUPON_NOT_EXISTS = new ErrorCode(1013005000, "优惠券不存在");
    ErrorCode COUPON_DELETE_FAIL_USED = new ErrorCode(1013005001, "回收优惠劵失败，优惠劵已被使用");
    ErrorCode COUPON_STATUS_NOT_UNUSED = new ErrorCode(1013005002, "优惠劵不处于待使用状态");
    ErrorCode COUPON_VALID_TIME_NOT_NOW = new ErrorCode(1013005003, "优惠券不在使用时间范围内");

    // ========== 满减送活动 1013006000 ==========
    ErrorCode REWARD_ACTIVITY_NOT_EXISTS = new ErrorCode(1013006000, "满减送活动不存在");
    ErrorCode REWARD_ACTIVITY_SPU_CONFLICTS = new ErrorCode(1013006001, "存在商品参加了其它满减送活动");
    ErrorCode REWARD_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED = new ErrorCode(1013006002, "满减送活动已关闭，不能修改");
    ErrorCode REWARD_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED = new ErrorCode(1013006003, "满减送活动未关闭，不能删除");
    ErrorCode REWARD_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED = new ErrorCode(1013006004, "满减送活动已关闭，不能重复关闭");
    ErrorCode REWARD_ACTIVITY_CLOSE_FAIL_STATUS_END = new ErrorCode(1013006005, "满减送活动已结束，不能关闭");

    // ========== Price 相关 1013007000 ============
    ErrorCode PRICE_CALCULATE_PAY_PRICE_ILLEGAL = new ErrorCode(1013007000, "支付价格计算异常，原因：价格小于等于 0");

    // ========== 秒杀活动 1013008000 ==========
    ErrorCode SECKILL_ACTIVITY_NOT_EXISTS = new ErrorCode(1013008000, "秒杀活动不存在");
    ErrorCode SECKILL_ACTIVITY_SPU_CONFLICTS = new ErrorCode(1013008002, "存在商品参加了其它秒杀活动");
    ErrorCode SECKILL_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED = new ErrorCode(1013008003, "秒杀活动已关闭，不能修改");
    ErrorCode SECKILL_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END = new ErrorCode(1013008004, "秒杀活动未关闭或未结束，不能删除");
    ErrorCode SECKILL_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED = new ErrorCode(1013008005, "秒杀活动已关闭，不能重复关闭");
    ErrorCode SECKILL_ACTIVITY_CLOSE_FAIL_STATUS_END = new ErrorCode(1013008006, "秒杀活动已结束，不能关闭");

    // ========== 秒杀时段 1013009000 ==========
    ErrorCode SECKILL_TIME_NOT_EXISTS = new ErrorCode(1013009000, "秒杀时段不存在");
    ErrorCode SECKILL_TIME_CONFLICTS = new ErrorCode(1013009001, "秒杀时段冲突");
    ErrorCode SECKILL_TIME_EQUAL = new ErrorCode(1013009002, "秒杀时段开始时间和结束时间不能相等");
    ErrorCode SECKILL_START_TIME_BEFORE_END_TIME = new ErrorCode(1013009003, "秒杀时段开始时间不能在结束时间之后");

    // ========== 拼团活动 1013010000 ==========
    ErrorCode COMBINATION_ACTIVITY_NOT_EXISTS = new ErrorCode(1013010000, "拼团活动不存在");
}
