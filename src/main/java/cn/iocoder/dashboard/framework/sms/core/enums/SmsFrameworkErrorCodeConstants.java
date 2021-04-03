package cn.iocoder.dashboard.framework.sms.core.enums;

import cn.iocoder.dashboard.common.exception.ErrorCode;

/**
 * 短信框架的错误码枚举
 *
 * 短信框架，使用 2-001-000-000 段
 *
 * @author 芋道源码
 */
public interface SmsFrameworkErrorCodeConstants {

    // ========== 渠道相关 2001000100 ==========
    ErrorCode SMS_CHANNEL_API_KEY_MISSING = new ErrorCode(2001000101, "API Key 不存在");
    ErrorCode SMS_CHANNEL_PERMISSION_DENY = new ErrorCode(2001000102, "没有发送短信的权限");

    // ========== 模板相关(200 开头) ==========
    ErrorCode SMS_TEMPLATE_NOT_EXISTS = new ErrorCode(200, "短信模板不存在");
    ErrorCode SMS_TEMPLATE_DISABLE = new ErrorCode(201, "短信模板被禁用"); // 例如说，我们在管理后台禁用了
    ErrorCode SMS_TEMPLATE_INVALID = new ErrorCode(202, "短信模板不可用"); // 例如说，短信模板正在审核中
    ErrorCode SMS_TEMPLATE_PARAM_ERROR = new ErrorCode(203, "模板参数不正确");

    // ========== 其它相关(900 开头) ==========
    ErrorCode SMS_API_PARAM_ERROR = new ErrorCode(900, "请求参数缺失");

    ErrorCode SMS_SEND_LIMIT_CONTROL = new ErrorCode(997, "业务限流"); // 将短信发送频率限制在正常的业务限流范围内。默认短信验证码：使用同一签名，对同一个手机号验证码，支持 1 条 / 分钟，5 条 / 小时，累计 10 条 / 天。
    ErrorCode EXCEPTION = new ErrorCode(998, "调用异常");
    ErrorCode SMS_UNKNOWN = new ErrorCode(999, "未知错误，需要解析");

}
