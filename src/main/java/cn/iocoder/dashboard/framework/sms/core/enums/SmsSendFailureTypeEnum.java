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

    // ========== 模板相关(100 开头) ==========
    SMS_CHANNEL_CLIENT_NOT_EXISTS(100, "短信渠道的客户端不存在"),

    // ========== 模板相关(200 开头) ==========
    SMS_TEMPLATE_DISABLE(200, "短信模板被禁用"),

    // ========== 其它相关(900 开头) ==========
    SMS_SEND_EXCEPTION(900, "发送异常"),
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
