package cn.iocoder.yudao.module.promotion.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Promotion 错误码枚举类
 * <p>
 * promotion 系统，使用 1-013-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 促销活动相关 1-013-001-000 ============
    ErrorCode DISCOUNT_ACTIVITY_NOT_EXISTS = new ErrorCode(1_013_001_000, "限时折扣活动不存在");
    ErrorCode DISCOUNT_ACTIVITY_SPU_CONFLICTS = new ErrorCode(1_013_001_001, "存在商品参加了其它限时折扣活动");
    ErrorCode DISCOUNT_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED = new ErrorCode(1_013_001_002, "限时折扣活动已关闭，不能修改");
    ErrorCode DISCOUNT_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED = new ErrorCode(1_013_001_003, "限时折扣活动未关闭，不能删除");
    ErrorCode DISCOUNT_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED = new ErrorCode(1_013_001_004, "限时折扣活动已关闭，不能重复关闭");

    // ========== Banner 相关 1-013-002-000 ============
    ErrorCode BANNER_NOT_EXISTS = new ErrorCode(1_013_002_000, "Banner 不存在");

    // ========== Coupon 相关 1-013-003-000 ============
    ErrorCode COUPON_NO_MATCH_SPU = new ErrorCode(1_013_003_000, "优惠劵没有可使用的商品！");
    ErrorCode COUPON_NO_MATCH_MIN_PRICE = new ErrorCode(1_013_003_001, "所结算的商品中未满足使用的金额");

    // ========== 优惠劵模板 1-013-004-000 ==========
    ErrorCode COUPON_TEMPLATE_NOT_EXISTS = new ErrorCode(1_013_004_000, "优惠劵模板不存在");
    ErrorCode COUPON_TEMPLATE_TOTAL_COUNT_TOO_SMALL = new ErrorCode(1_013_004_001, "发放数量不能小于已领取数量({})");
    ErrorCode COUPON_TEMPLATE_NOT_ENOUGH = new ErrorCode(1_013_004_002, "当前剩余数量不够领取");
    ErrorCode COUPON_TEMPLATE_USER_ALREADY_TAKE = new ErrorCode(1_013_004_003, "用户已领取过此优惠券");
    ErrorCode COUPON_TEMPLATE_EXPIRED = new ErrorCode(1_013_004_004, "优惠券已过期");
    ErrorCode COUPON_TEMPLATE_CANNOT_TAKE = new ErrorCode(1_013_004_005, "领取方式不正确");

    // ========== 优惠劵 1-013-005-000 ==========
    ErrorCode COUPON_NOT_EXISTS = new ErrorCode(1_013_005_000, "优惠券不存在");
    ErrorCode COUPON_DELETE_FAIL_USED = new ErrorCode(1_013_005_001, "回收优惠劵失败，优惠劵已被使用");
    ErrorCode COUPON_STATUS_NOT_UNUSED = new ErrorCode(1_013_005_002, "优惠劵不处于待使用状态");
    ErrorCode COUPON_VALID_TIME_NOT_NOW = new ErrorCode(1_013_005_003, "优惠券不在使用时间范围内");
    ErrorCode COUPON_STATUS_NOT_USED = new ErrorCode(1_013_005_004, "优惠劵不是已使用状态");

    // ========== 满减送活动 1-013-006-000 ==========
    ErrorCode REWARD_ACTIVITY_NOT_EXISTS = new ErrorCode(1_013_006_000, "满减送活动不存在");
    ErrorCode REWARD_ACTIVITY_SPU_CONFLICTS = new ErrorCode(1_013_006_001, "该时间段存在商品参加了其它满减送活动");
    ErrorCode REWARD_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED = new ErrorCode(1_013_006_002, "满减送活动已关闭，不能修改");
    ErrorCode REWARD_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED = new ErrorCode(1_013_006_003, "满减送活动未关闭，不能删除");
    ErrorCode REWARD_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED = new ErrorCode(1_013_006_004, "满减送活动已关闭，不能重复关闭");
    ErrorCode REWARD_ACTIVITY_SCOPE_EXISTS = new ErrorCode(1_013_006_005, "与该时间段已存在的满减送活动商品范围冲突。注意商品范围 全部 > 分类 > 商品");

    // ========== TODO 空着 1-013-007-000 ============

    // ========== 秒杀活动 1-013-008-000 ==========
    ErrorCode SECKILL_ACTIVITY_NOT_EXISTS = new ErrorCode(1_013_008_000, "秒杀活动不存在");
    ErrorCode SECKILL_ACTIVITY_SPU_CONFLICTS = new ErrorCode(1_013_008_002, "存在商品参加了其它秒杀活动，秒杀时段冲突");
    ErrorCode SECKILL_ACTIVITY_UPDATE_FAIL_STATUS_CLOSED = new ErrorCode(1_013_008_003, "秒杀活动已关闭，不能修改");
    ErrorCode SECKILL_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END = new ErrorCode(1_013_008_004, "秒杀活动未关闭或未结束，不能删除");
    ErrorCode SECKILL_ACTIVITY_CLOSE_FAIL_STATUS_CLOSED = new ErrorCode(1_013_008_005, "秒杀活动已关闭，不能重复关闭");
    ErrorCode SECKILL_ACTIVITY_UPDATE_STOCK_FAIL = new ErrorCode(1_013_008_006, "秒杀失败，原因：秒杀库存不足");
    ErrorCode SECKILL_JOIN_ACTIVITY_TIME_ERROR = new ErrorCode(1_013_008_007, "秒杀失败，原因：不在活动时间范围内");
    ErrorCode SECKILL_JOIN_ACTIVITY_STATUS_CLOSED = new ErrorCode(1_013_008_008, "秒杀失败，原因：秒杀活动已关闭");
    ErrorCode SECKILL_JOIN_ACTIVITY_SINGLE_LIMIT_COUNT_EXCEED = new ErrorCode(1_013_008_009, "秒杀失败，原因：单次限购超出");
    ErrorCode SECKILL_JOIN_ACTIVITY_PRODUCT_NOT_EXISTS = new ErrorCode(1_013_008_010, "秒杀失败，原因：商品不存在");

    // ========== 秒杀时段 1-013-009-000 ==========
    ErrorCode SECKILL_CONFIG_NOT_EXISTS = new ErrorCode(1_013_009_000, "秒杀时段不存在");
    ErrorCode SECKILL_CONFIG_TIME_CONFLICTS = new ErrorCode(1_013_009_001, "秒杀时段冲突");
    ErrorCode SECKILL_CONFIG_DISABLE = new ErrorCode(1_013_009_004, "秒杀时段已关闭");

    // ========== 拼团活动 1-013-010-000 ==========
    ErrorCode COMBINATION_ACTIVITY_NOT_EXISTS = new ErrorCode(1_013_010_000, "拼团活动不存在");
    ErrorCode COMBINATION_ACTIVITY_SPU_CONFLICTS = new ErrorCode(1_013_010_001, "存在商品参加了其它拼团活动");
    ErrorCode COMBINATION_ACTIVITY_STATUS_DISABLE_NOT_UPDATE = new ErrorCode(1_013_010_002, "拼团活动已关闭不能修改");
    ErrorCode COMBINATION_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END = new ErrorCode(1_013_010_003, "拼团活动未关闭或未结束，不能删除");
    ErrorCode COMBINATION_ACTIVITY_STATUS_DISABLE = new ErrorCode(1_013_010_004, "拼团失败，原因：拼团活动已关闭");
    ErrorCode COMBINATION_JOIN_ACTIVITY_PRODUCT_NOT_EXISTS = new ErrorCode(1_013_010_005, "拼团失败，原因：拼团活动商品不存在");
    ErrorCode COMBINATION_ACTIVITY_UPDATE_STOCK_FAIL = new ErrorCode(1_013_010_006, "拼团失败，原因：拼团活动商品库存不足");

    // ========== 拼团记录 1-013-011-000 ==========
    ErrorCode COMBINATION_RECORD_NOT_EXISTS = new ErrorCode(1_013_011_000, "拼团不存在");
    ErrorCode COMBINATION_RECORD_EXISTS = new ErrorCode(1_013_011_001, "拼团失败，已参与过该拼团");
    ErrorCode COMBINATION_RECORD_HEAD_NOT_EXISTS = new ErrorCode(1_013_011_002, "拼团失败，父拼团不存在");
    ErrorCode COMBINATION_RECORD_USER_FULL = new ErrorCode(1_013_011_003, "拼团失败，拼团人数已满");
    ErrorCode COMBINATION_RECORD_FAILED_HAVE_JOINED = new ErrorCode(1_013_011_004, "拼团失败，原因：存在该活动正在进行的拼团记录");
    ErrorCode COMBINATION_RECORD_FAILED_TIME_NOT_START = new ErrorCode(1_013_011_005, "拼团失败，活动未开始");
    ErrorCode COMBINATION_RECORD_FAILED_TIME_END = new ErrorCode(1_013_011_006, "拼团失败，活动已经结束");
    ErrorCode COMBINATION_RECORD_FAILED_SINGLE_LIMIT_COUNT_EXCEED = new ErrorCode(1_013_011_007, "拼团失败，原因：单次限购超出");
    ErrorCode COMBINATION_RECORD_FAILED_TOTAL_LIMIT_COUNT_EXCEED = new ErrorCode(1_013_011_008, "拼团失败，原因：超出总购买次数");
    ErrorCode COMBINATION_RECORD_FAILED_ORDER_STATUS_UNPAID = new ErrorCode(1_013_011_009, "拼团失败，原因：存在未支付订单，请先支付");

    // ========== 砍价活动 1-013-012-000 ==========
    ErrorCode BARGAIN_ACTIVITY_NOT_EXISTS = new ErrorCode(1_013_012_000, "砍价活动不存在");
    ErrorCode BARGAIN_ACTIVITY_SPU_CONFLICTS = new ErrorCode(1_013_012_001, "存在商品参加了其它砍价活动");
    ErrorCode BARGAIN_ACTIVITY_STATUS_DISABLE = new ErrorCode(1_013_012_002, "砍价活动已关闭，不能修改");
    ErrorCode BARGAIN_ACTIVITY_DELETE_FAIL_STATUS_NOT_CLOSED_OR_END = new ErrorCode(1_013_012_003, "砍价活动未关闭或未结束，不能删除");
    ErrorCode BARGAIN_ACTIVITY_STOCK_NOT_ENOUGH = new ErrorCode(1_013_012_004, "砍价活动库存不足");
    ErrorCode BARGAIN_ACTIVITY_STATUS_CLOSED = new ErrorCode(1_013_012_005, "砍价活动已关闭");
    ErrorCode BARGAIN_ACTIVITY_TIME_END = new ErrorCode(1_013_012_006, "砍价活动已经结束");

    // ========== 砍价记录 1-013-013-000 ==========
    ErrorCode BARGAIN_RECORD_NOT_EXISTS = new ErrorCode(1_013_013_000, "砍价记录不存在");
    ErrorCode BARGAIN_RECORD_CREATE_FAIL_EXISTS = new ErrorCode(1_013_013_001, "参与失败，您已经参与当前砍价活动");
    ErrorCode BARGAIN_RECORD_CREATE_FAIL_LIMIT = new ErrorCode(1_013_013_002, "参与失败，您已达到当前砍价活动的参与上限");
    ErrorCode BARGAIN_JOIN_RECORD_NOT_SUCCESS = new ErrorCode(1_013_013_004, "下单失败，砍价未成功");
    ErrorCode BARGAIN_JOIN_RECORD_ALREADY_ORDER = new ErrorCode(1_013_013_005, "下单失败，该砍价已经下单");

    // ========== 砍价助力 1-013-014-000 ==========
    ErrorCode BARGAIN_HELP_CREATE_FAIL_RECORD_NOT_IN_PROCESS = new ErrorCode(1_013_014_000, "助力失败，砍价记录不处于进行中");
    ErrorCode BARGAIN_HELP_CREATE_FAIL_RECORD_SELF = new ErrorCode(1_013_014_001, "助力失败，不能助力自己");
    ErrorCode BARGAIN_HELP_CREATE_FAIL_LIMIT = new ErrorCode(1_013_014_002, "助力失败，您已达到当前砍价活动的助力上限");
    ErrorCode BARGAIN_HELP_CREATE_FAIL_CONFLICT = new ErrorCode(1_013_014_003, "助力失败，请重试");
    ErrorCode BARGAIN_HELP_CREATE_FAIL_HELP_EXISTS = new ErrorCode(1_013_014_004, "助力失败，您已经助力过了");

    // ========== 文章分类 1-013-015-000 ==========
    ErrorCode ARTICLE_CATEGORY_NOT_EXISTS = new ErrorCode(1_013_015_000, "文章分类不存在");
    ErrorCode ARTICLE_CATEGORY_DELETE_FAIL_HAVE_ARTICLES = new ErrorCode(1_013_015_001, "文章分类删除失败，存在关联文章");

    // ========== 文章管理 1-013-016-000 ==========
    ErrorCode ARTICLE_NOT_EXISTS = new ErrorCode(1_013_016_000, "文章不存在");

    // ========== 装修模板 1-013-017-000 ==========
    ErrorCode DIY_TEMPLATE_NOT_EXISTS = new ErrorCode(1_013_017_000, "装修模板不存在");
    ErrorCode DIY_TEMPLATE_NAME_USED = new ErrorCode(1_013_017_001, "装修模板名称({})已经被使用");
    ErrorCode DIY_TEMPLATE_USED_CANNOT_DELETE = new ErrorCode(1_013_017_002, "不能删除正在使用的装修模板");

    // ========== 装修页面 1-013-018-000 ==========
    ErrorCode DIY_PAGE_NOT_EXISTS = new ErrorCode(1_013_018_000, "装修页面不存在");
    ErrorCode DIY_PAGE_NAME_USED = new ErrorCode(1_013_018_001, "装修页面名称({})已经被使用");

    // ========== 客服会话 1-013-019-000 ==========
    ErrorCode KEFU_CONVERSATION_NOT_EXISTS = new ErrorCode(1_013_019_000, "客服会话不存在");

    // ========== 客服消息 1-013-020-000 ==========
    ErrorCode KEFU_MESSAGE_NOT_EXISTS = new ErrorCode(1_013_020_000, "客服消息不存在");

}
