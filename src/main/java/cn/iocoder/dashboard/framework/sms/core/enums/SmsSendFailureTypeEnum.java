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

    // ========== 模板相关(200 开头) ==========
    SMS_TEMPLATE_NOT_EXISTS(200, "短信模板不存在"),
    SMS_TEMPLATE_DISABLE(201, "短信模板被禁用"), // 例如说，我们在管理后台禁用了
    SMS_TMPLATE_INVALID(202, "短信模板不可用"), // 例如说，短信模板正在审核中
    SMS_TEMPLATE_PARAM_ERROR(203, "模板参数不正确"),

    // ========== 其它相关(900 开头) ==========
    SMS_API_PARAM_ERROR(900, "请求参数缺失"),

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
