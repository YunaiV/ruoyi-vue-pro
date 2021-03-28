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
    SMS_TEMPLATE_DISABLE(100), // 短信模板被禁用

    // ========== 其它相关 ==========
    ;

    /**
     * 失败类型
     */
    private final int type;

}
