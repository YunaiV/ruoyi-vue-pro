package cn.iocoder.yudao.module.promotion.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * market 错误码枚举类
 * <p>
 * market 系统，使用 1-003-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 促销活动相关 1003001000 ============ TODO 芋艿：看看是不是要删除掉
    ErrorCode ACTIVITY_NOT_EXISTS = new ErrorCode(1003001000, "促销活动不存在");

    // ========== Banner 相关 1003002000 ============
    ErrorCode BANNER_NOT_EXISTS = new ErrorCode(1003002000, "Banner 不存在");

    // ========== Coupon 相关 1003003000 ============
    ErrorCode COUPON_NO_MATCH_SPU = new ErrorCode(1003003000, "优惠劵没有可使用的商品！");
    ErrorCode COUPON_NO_MATCH_MIN_PRICE = new ErrorCode(1003003000, "不满足优惠劵使用的最低金额");

    // ========== 优惠劵模板 1003004000 ==========
    ErrorCode COUPON_TEMPLATE_NOT_EXISTS = new ErrorCode(1003004000, "优惠劵模板不存在");
    ErrorCode COUPON_TEMPLATE_TOTAL_COUNT_TOO_SMALL = new ErrorCode(1003004001, "发放数量不能小于已领取数量({})");

}
