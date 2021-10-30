package cn.iocoder.yudao.coreservice.modules.system.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * System 错误码枚举类
 *
 * system 系统，使用 1-006-000-000 段
 */
public interface SysErrorCodeConstants {

    // ========== 短信发送 1006000000 ==========
    ErrorCode SMS_SEND_MOBILE_NOT_EXISTS = new ErrorCode(1006000000, "手机号不存在");
    ErrorCode SMS_SEND_MOBILE_TEMPLATE_PARAM_MISS = new ErrorCode(1006000001, "模板参数({})缺失");
    ErrorCode SMS_SEND_TEMPLATE_NOT_EXISTS = new ErrorCode(1006000000, "短信模板不存在");

    // ========= 文件相关 1006001000=================
    ErrorCode FILE_PATH_EXISTS = new ErrorCode(1006001000, "文件路径已存在");
    ErrorCode FILE_NOT_EXISTS = new ErrorCode(1006001002, "文件不存在");

    // ========== 社交模块 1006002000 ==========
    ErrorCode SOCIAL_AUTH_FAILURE = new ErrorCode(1006002000, "社交授权失败，原因是：{}");
    ErrorCode SOCIAL_UNBIND_NOT_SELF = new ErrorCode(1006002001, "社交解绑失败，非当前用户绑定");

}
