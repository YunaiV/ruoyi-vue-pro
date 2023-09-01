package cn.iocoder.yudao.module.promotion.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Promotion 错误码枚举类
 * <p>
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
    ErrorCode COUPON_TEMPLATE_NOT_ENOUGH = new ErrorCode(1013004002, "当前剩余数量不够领取");
    ErrorCode COUPON_TEMPLATE_USER_ALREADY_TAKE = new ErrorCode(1013004003, "用户已领取过此优惠券");
    ErrorCode COUPON_TEMPLATE_EXPIRED = new ErrorCode(1013004004, "优惠券已过期");
    ErrorCode COUPON_TEMPLATE_CANNOT_TAKE = new ErrorCode(1013004005, "领取方式不正确");

    // ========== 优惠劵 1013005000 ==========
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

    // ========== TODO 空着 1013007000 ============

    // ========== 秒杀活动 1013008000 ==========
    ErrorCode SECKILL_ACTIVITY_NOT_EXISTS = new ErrorCode(1013008000, "秒杀活动不存在");
    ErrorCode SECKILL_ACTIVITY_SPU_CONFLICTS = new ErrorCode(1013008002, "存在商品参加了其它秒杀活动");
    ErrorCode SECKILL_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED = new ErrorCode(1013008003, "秒杀活动已关闭，不能修改");
    ErrorCode SECKILL_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END = new ErrorCode(1013008004, "秒杀活动未关闭或未结束，不能删除");
    ErrorCode SECKILL_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED = new ErrorCode(1013008005, "秒杀活动已关闭，不能重复关闭");

    // ========== 秒杀时段 1013009000 ==========
    ErrorCode SECKILL_CONFIG_NOT_EXISTS = new ErrorCode(1013009000, "秒杀时段不存在");
    ErrorCode SECKILL_CONFIG_TIME_CONFLICTS = new ErrorCode(1013009001, "秒杀时段冲突");
    ErrorCode SECKILL_CONFIG_DISABLE = new ErrorCode(1013009004, "秒杀时段已关闭");

    // ========== 拼团活动 1013010000 ==========
    ErrorCode COMBINATION_ACTIVITY_NOT_EXISTS = new ErrorCode(1013010000, "拼团活动不存在");
    ErrorCode COMBINATION_ACTIVITY_SPU_CONFLICTS = new ErrorCode(1013010001, "存在商品参加了其它拼团活动");
    ErrorCode COMBINATION_ACTIVITY_STATUS_DISABLE = new ErrorCode(1013010002, "拼团活动已关闭不能修改");
    ErrorCode COMBINATION_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END = new ErrorCode(1013010003, "拼团活动未关闭或未结束，不能删除");
    ErrorCode COMBINATION_RECORD_NOT_EXISTS = new ErrorCode(1013010004, "拼团不存在");

    // ========== 拼团记录 1013011000 ==========
    ErrorCode COMBINATION_RECORD_EXISTS = new ErrorCode(1013011000, "拼团失败，已参与过该拼团");
    ErrorCode COMBINATION_RECORD_HEAD_NOT_EXISTS = new ErrorCode(1013011001, "拼团失败，父拼团不存在");
    ErrorCode COMBINATION_RECORD_USER_FULL = new ErrorCode(1013011002, "拼团失败，拼团人数已满");
    ErrorCode COMBINATION_RECORD_FAILED_HAVE_JOINED = new ErrorCode(1013011003, "拼团失败，已参与其它拼团");
    ErrorCode COMBINATION_RECORD_FAILED_TIME_END = new ErrorCode(1013011004, "拼团失败，活动已经结束");
    ErrorCode COMBINATION_RECORD_FAILED_SINGLE_LIMIT_COUNT_EXCEED = new ErrorCode(1013011005, "拼团失败，单次限购超出");
    ErrorCode COMBINATION_RECORD_FAILED_TOTAL_LIMIT_COUNT_EXCEED = new ErrorCode(1013011006, "拼团失败，单次限购超出");

    // ========== 砍价活动 1013012000 ==========
    ErrorCode BARGAIN_ACTIVITY_NOT_EXISTS = new ErrorCode(1013012000, "砍价活动不存在");
    ErrorCode BARGAIN_ACTIVITY_SPU_CONFLICTS = new ErrorCode(1013012001, "存在商品参加了其它砍价活动");
    ErrorCode BARGAIN_ACTIVITY_STATUS_DISABLE = new ErrorCode(1013012002, "砍价活动已关闭不能修改");
    ErrorCode BARGAIN_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END = new ErrorCode(1013012003, "砍价活动未关闭或未结束，不能删除");

    // ========== 砍价记录 1013013000 ==========
    ErrorCode BARGAIN_RECORD_NOT_EXISTS = new ErrorCode(1013013000, "砍价记录不存在");
    ErrorCode BARGAIN_RECORD_EXISTS = new ErrorCode(1013013001, "砍价失败，已参与过该砍价");
    ErrorCode BARGAIN_RECORD_HEAD_NOT_EXISTS = new ErrorCode(1013013002, "砍价失败，父砍价不存在");
    ErrorCode BARGAIN_RECORD_USER_FULL = new ErrorCode(1013013003, "砍价失败，砍价人数已满");

}
