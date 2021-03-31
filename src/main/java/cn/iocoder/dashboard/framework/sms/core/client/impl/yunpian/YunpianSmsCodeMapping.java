package cn.iocoder.dashboard.framework.sms.core.client.impl.yunpian;

import cn.iocoder.dashboard.common.exception.ErrorCode;
import cn.iocoder.dashboard.framework.sms.core.client.SmsCodeMapping;

import static cn.iocoder.dashboard.common.exception.enums.GlobalErrorCodeConstants.SUCCESS;
import static cn.iocoder.dashboard.framework.sms.core.enums.SmsFrameworkErrorCodeConstants.*;
import static com.yunpian.sdk.constant.Code.*;

/**
 * 云片的 SmsCodeMapping 实现类
 *
 * @author 芋道源码
 */
public class YunpianSmsCodeMapping implements SmsCodeMapping {

    @Override
    public ErrorCode apply(String apiCode) {
        int code = Integer.parseInt(apiCode);
        switch (code) {
            case OK: return SUCCESS;
            case ARGUMENT_MISSING: return SMS_API_PARAM_ERROR;
            case BAD_ARGUMENT_FORMAT: return SMS_TEMPLATE_PARAM_ERROR;
            case TPL_NOT_FOUND: return SMS_TEMPLATE_NOT_EXISTS;
            case TPL_NOT_VALID: return SMS_TEMPLATE_INVALID;
        }
        return SMS_UNKNOWN;
    }

}
