package cn.iocoder.yudao.framework.sms.core.client.impl.tencent;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.sms.core.enums.SmsFrameworkErrorCodeConstants;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link TencentSmsCodeMapping} 的单元测试
 *
 * @author : shiwp
 */
public class TencentSmsCodeMappingTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TencentSmsCodeMapping codeMapping;

    @Test
    public void testApply() {
        assertEquals(GlobalErrorCodeConstants.SUCCESS, codeMapping.apply(TencentSmsClient.API_SUCCESS_CODE));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_SEND_CONTENT_INVALID, codeMapping.apply("FailedOperation.ContainSensitiveWord"));
        assertEquals(GlobalErrorCodeConstants.BAD_REQUEST, codeMapping.apply("FailedOperation.JsonParseFail"));
        assertEquals(GlobalErrorCodeConstants.BAD_REQUEST, codeMapping.apply("MissingParameter.EmptyPhoneNumberSet"));
        assertEquals(GlobalErrorCodeConstants.BAD_REQUEST, codeMapping.apply("LimitExceeded.PhoneNumberCountLimit"));
        assertEquals(GlobalErrorCodeConstants.BAD_REQUEST, codeMapping.apply("FailedOperation.FailResolvePacket"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_ACCOUNT_MONEY_NOT_ENOUGH, codeMapping.apply("FailedOperation.InsufficientBalanceInSmsPackage"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_SEND_MARKET_LIMIT_CONTROL, codeMapping.apply("FailedOperation.MarketingSendTimeConstraint"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_MOBILE_BLACK, codeMapping.apply("FailedOperation.PhoneNumberInBlacklist"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_SIGN_INVALID, codeMapping.apply("FailedOperation.SignatureIncorrectOrUnapproved"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_TEMPLATE_INVALID, codeMapping.apply("FailedOperation.MissingTemplateToModify"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_TEMPLATE_INVALID, codeMapping.apply("FailedOperation.TemplateIncorrectOrUnapproved"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_MOBILE_INVALID, codeMapping.apply("InvalidParameterValue.IncorrectPhoneNumber"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_APP_ID_INVALID, codeMapping.apply("InvalidParameterValue.SdkAppIdNotExist"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_TEMPLATE_PARAM_ERROR, codeMapping.apply("InvalidParameterValue.TemplateParameterLengthLimit"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_TEMPLATE_PARAM_ERROR, codeMapping.apply("InvalidParameterValue.TemplateParameterFormatError"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_SEND_DAY_LIMIT_CONTROL, codeMapping.apply("LimitExceeded.PhoneNumberDailyLimit"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_SEND_BUSINESS_LIMIT_CONTROL, codeMapping.apply("LimitExceeded.PhoneNumberThirtySecondLimit"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_SEND_BUSINESS_LIMIT_CONTROL, codeMapping.apply("LimitExceeded.PhoneNumberOneHourLimit"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_PERMISSION_DENY, codeMapping.apply("UnauthorizedOperation.RequestPermissionDeny"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_PERMISSION_DENY, codeMapping.apply("FailedOperation.ForbidAddMarketingTemplates"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_PERMISSION_DENY, codeMapping.apply("FailedOperation.NotEnterpriseCertification"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_PERMISSION_DENY, codeMapping.apply("UnauthorizedOperation.IndividualUserMarketingSmsPermissionDeny"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_IP_DENY, codeMapping.apply("UnauthorizedOperation.RequestIpNotInWhitelist"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_ACCOUNT_INVALID, codeMapping.apply("AuthFailure.SecretIdNotFound"));
    }

}