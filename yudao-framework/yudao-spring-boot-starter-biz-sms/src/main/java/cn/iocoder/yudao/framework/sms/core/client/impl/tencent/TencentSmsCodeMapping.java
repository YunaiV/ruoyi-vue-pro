package cn.iocoder.yudao.framework.sms.core.client.impl.tencent;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.sms.core.client.SmsCodeMapping;
import cn.iocoder.yudao.framework.sms.core.enums.SmsFrameworkErrorCodeConstants;

import static cn.iocoder.yudao.framework.sms.core.enums.SmsFrameworkErrorCodeConstants.*;

/**
 * 腾讯云的 SmsCodeMapping 实现类
 *
 * 参见 https://cloud.tencent.com/document/api/382/52075#.E5.85.AC.E5.85.B1.E9.94.99.E8.AF.AF.E7.A0.81
 *
 * @author : shiwp
 */
public class TencentSmsCodeMapping implements SmsCodeMapping {

    @Override
    public ErrorCode apply(String apiCode) {
        switch (apiCode) {
            case TencentSmsClient.API_SUCCESS_CODE: return GlobalErrorCodeConstants.SUCCESS;
            case "FailedOperation.ContainSensitiveWord": return SMS_SEND_CONTENT_INVALID;
            case "FailedOperation.JsonParseFail":
            case "MissingParameter.EmptyPhoneNumberSet":
            case "LimitExceeded.PhoneNumberCountLimit":
            case "FailedOperation.FailResolvePacket": return GlobalErrorCodeConstants.BAD_REQUEST;
            case "FailedOperation.InsufficientBalanceInSmsPackage": return SMS_ACCOUNT_MONEY_NOT_ENOUGH;
            case "FailedOperation.MarketingSendTimeConstraint": return SMS_SEND_MARKET_LIMIT_CONTROL;
            case "FailedOperation.PhoneNumberInBlacklist": return SMS_MOBILE_BLACK;
            case "FailedOperation.SignatureIncorrectOrUnapproved": return SMS_SIGN_INVALID;
            case "FailedOperation.MissingTemplateToModify":
            case "FailedOperation.TemplateIncorrectOrUnapproved": return SMS_TEMPLATE_INVALID;
            case "InvalidParameterValue.IncorrectPhoneNumber": return SMS_MOBILE_INVALID;
            case "InvalidParameterValue.SdkAppIdNotExist": return SMS_APP_ID_INVALID;
            case "InvalidParameterValue.TemplateParameterLengthLimit":
            case "InvalidParameterValue.TemplateParameterFormatError": return SMS_TEMPLATE_PARAM_ERROR;
            case "LimitExceeded.PhoneNumberDailyLimit": return SMS_SEND_DAY_LIMIT_CONTROL;
            case "LimitExceeded.PhoneNumberThirtySecondLimit":
            case "LimitExceeded.PhoneNumberOneHourLimit": return SMS_SEND_BUSINESS_LIMIT_CONTROL;
            case "UnauthorizedOperation.RequestPermissionDeny":
            case "FailedOperation.ForbidAddMarketingTemplates":
            case "FailedOperation.NotEnterpriseCertification":
            case "UnauthorizedOperation.IndividualUserMarketingSmsPermissionDeny": return SMS_PERMISSION_DENY;
            case "UnauthorizedOperation.RequestIpNotInWhitelist": return SMS_IP_DENY;
            case "AuthFailure.SecretIdNotFound": return SMS_ACCOUNT_INVALID;
        }
        return SmsFrameworkErrorCodeConstants.SMS_UNKNOWN;
    }
}