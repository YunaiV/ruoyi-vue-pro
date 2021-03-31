package cn.iocoder.dashboard.framework.sms.core.client.impl.aliyun;

import cn.iocoder.dashboard.common.exception.ErrorCode;
import cn.iocoder.dashboard.framework.sms.core.client.SmsCodeMapping;

import static cn.iocoder.dashboard.framework.sms.core.enums.SmsFrameworkErrorCodeConstants.*;

/**
 * 阿里云的 SmsCodeMapping 实现类
 *
 * @author 芋道源码
 */
public class AliyunSmsCodeMapping implements SmsCodeMapping {

    @Override
    public ErrorCode apply(String apiCode) {
        switch (apiCode) {
            case "OK": return null;
            case "MissingAccessKeyId": return SMS_CHANNEL_API_KEY_MISSING;
            case "isp.RAM_PERMISSION_DENY": return SMS_CHANNEL_PERMISSION_DENY;
            case "isv.INVALID_PARAMETERS": return SMS_API_PARAM_ERROR;
            case "isv.BUSINESS_LIMIT_CONTROL": return SMS_SEND_LIMIT_CONTROL;
        }
        return SMS_UNKNOWN;
    }

}
