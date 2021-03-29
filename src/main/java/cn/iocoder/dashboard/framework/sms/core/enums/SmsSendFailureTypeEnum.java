package cn.iocoder.dashboard.framework.sms.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 短信的发送失败类型的枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum SmsSendFailureTypeEnum {

    // ========== 渠道相关(100 开头) ==========
    SMS_CHANNEL_CLIENT_NOT_EXISTS(100, "短信渠道的客户端不存在"),
    SMS_CHANNEL_API_KEY_MISSING(101, "API Key 不存在"),
    SMS_CHANNEL_PERMISSION_DENY(102, "没有发送短信的权限"),

    // ========== 模板相关(200 开头) ==========
    SMS_TEMPLATE_NOT_EXISTS(200, "短信模板不存在"),
    SMS_TEMPLATE_DISABLE(201, "短信模板被禁用"), // 例如说，我们在管理后台禁用了
    SMS_TEMPLATE_INVALID(202, "短信模板不可用"), // 例如说，短信模板正在审核中
    SMS_TEMPLATE_PARAM_ERROR(203, "模板参数不正确"),

    // ========== 其它相关(900 开头) ==========
    SMS_API_PARAM_ERROR(900, "请求参数缺失"),

    SMS_SEND_LIMIT_CONTROL(997, "业务限流"), // 将短信发送频率限制在正常的业务限流范围内。默认短信验证码：使用同一签名，对同一个手机号验证码，支持 1 条 / 分钟，5 条 / 小时，累计 10 条 / 天。
    SMS_SEND_EXCEPTION(998, "发送异常"),
    SMS_UNKNOWN(999, "未知错误，需要解析")
    ;

    /**
     * 失败类型
     */
    private final int type;
    /**
     * 失败提示
     */
    private final String msg;

}
