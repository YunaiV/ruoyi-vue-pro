package cn.iocoder.yudao.module.market.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * market 错误码枚举类
 * <p>
 * market 系统，使用 1-003-000-000 段
 */
public interface ErrorCodeConstants {


    // ========== 促销活动相关  1003001000============
    ErrorCode ACTIVITY_NOT_EXISTS = new ErrorCode(1003001000, "促销活动不存在");

    // ========== banner相关  1003002000============
    ErrorCode BANNER_NOT_EXISTS = new ErrorCode(1003002000, "Banner不存在");
}
