package cn.iocoder.yudao.module.mp.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Mp 错误码枚举类
 *
 * mp 系统，使用 1-006-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 公众号账号 1006000000============
    ErrorCode ACCOUNT_NOT_EXISTS = new ErrorCode(1006000000, "公众号账号不存在");
    ErrorCode ACCOUNT_GENERATE_QR_CODE_FAIL = new ErrorCode(1006000001, "生成公众号二维码失败，原因：{}");
    ErrorCode ACCOUNT_CLEAR_QUOTA_FAIL = new ErrorCode(1006000001, "清空公众号的 API 配额失败，原因：{}");

    // ========== 公众号账号 1006001000============
    ErrorCode STATISTICS_GET_USER_SUMMARY_FAIL = new ErrorCode(1006001000, "获取用户增减数据失败，原因：{}");

    // TODO 要处理下
    ErrorCode COMMON_NOT_EXISTS = new ErrorCode(1006001002, "用户不存在");

}
