package cn.iocoder.dashboard.framework.sms.core.client.impl.aliyun;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.dashboard.framework.sms.core.client.SmsCodeMapping;

import static cn.iocoder.dashboard.framework.sms.core.enums.SmsFrameworkErrorCodeConstants.*;

/**
 * 阿里云的 SmsCodeMapping 实现类
 *
 * 参见 https://help.aliyun.com/document_detail/101346.htm 文档
 *
 * @author 芋道源码
 */
public class AliyunSmsCodeMapping implements SmsCodeMapping {

    @Override
    public ErrorCode apply(String apiCode) {
        switch (apiCode) {
            case "OK": return GlobalErrorCodeConstants.SUCCESS;
            case "isv.ACCOUNT_NOT_EXISTS":
            case "isv.ACCOUNT_ABNORMAL":
            case "MissingAccessKeyId": return SMS_ACCOUNT_INVALID;
            case "isp.RAM_PERMISSION_DENY": return SMS_PERMISSION_DENY;
            case "isv.INVALID_JSON_PARAM":
            case "isv.INVALID_PARAMETERS": return SMS_API_PARAM_ERROR;
            case "isv.BUSINESS_LIMIT_CONTROL": return SMS_SEND_BUSINESS_LIMIT_CONTROL;
            case "isv.DAY_LIMIT_CONTROL": return SMS_SEND_DAY_LIMIT_CONTROL;
            case "isv.SMS_CONTENT_ILLEGAL": return SMS_SEND_CONTENT_INVALID;
            case "isv.SMS_TEMPLATE_ILLEGAL": return SMS_TEMPLATE_INVALID;
            case "isv.SMS_SIGNATURE_ILLEGAL":
            case "isv.SIGN_NAME_ILLEGAL":
            case "isv.SMS_SIGN_ILLEGAL": return SMS_SIGN_INVALID;
            case "isv.AMOUNT_NOT_ENOUGH":
            case "isv.OUT_OF_SERVICE": return SMS_ACCOUNT_MONEY_NOT_ENOUGH;
            case "isv.MOBILE_NUMBER_ILLEGAL": return SMS_MOBILE_INVALID;
            case "isv.TEMPLATE_MISSING_PARAMETERS": return SMS_TEMPLATE_PARAM_ERROR;
        }
        return SMS_UNKNOWN;
    }

}
