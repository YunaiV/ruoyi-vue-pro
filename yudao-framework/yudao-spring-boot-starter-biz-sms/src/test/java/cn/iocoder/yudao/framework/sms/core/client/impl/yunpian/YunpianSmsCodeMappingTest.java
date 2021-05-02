package cn.iocoder.yudao.framework.sms.core.client.impl.yunpian;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.sms.core.enums.SmsFrameworkErrorCodeConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static com.yunpian.sdk.constant.Code.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link YunpianSmsCodeMapping} 的单元测试
 *
 * @author 芋道源码
 */
class YunpianSmsCodeMappingTest extends BaseMockitoUnitTest {

    @InjectMocks
    private YunpianSmsCodeMapping codeMapping;

    @Test
    public void testApply() {
        assertEquals(GlobalErrorCodeConstants.SUCCESS, codeMapping.apply(String.valueOf(OK)));
        Assertions.assertEquals(SmsFrameworkErrorCodeConstants.SMS_API_PARAM_ERROR, codeMapping.apply(String.valueOf(ARGUMENT_MISSING)));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_TEMPLATE_PARAM_ERROR, codeMapping.apply(String.valueOf(BAD_ARGUMENT_FORMAT)));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_ACCOUNT_MONEY_NOT_ENOUGH, codeMapping.apply(String.valueOf(MONEY_NOT_ENOUGH)));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_TEMPLATE_INVALID, codeMapping.apply(String.valueOf(TPL_NOT_FOUND)));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_TEMPLATE_INVALID, codeMapping.apply(String.valueOf(TPL_NOT_VALID)));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_SEND_BUSINESS_LIMIT_CONTROL, codeMapping.apply(String.valueOf(DUP_IN_SHORT_TIME)));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_SEND_BUSINESS_LIMIT_CONTROL, codeMapping.apply(String.valueOf(TOO_MANY_TIME_IN_5)));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_SEND_BUSINESS_LIMIT_CONTROL, codeMapping.apply(String.valueOf(DAY_LIMIT_PER_MOBILE)));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_SEND_BUSINESS_LIMIT_CONTROL, codeMapping.apply(String.valueOf(HOUR_LIMIT_PER_MOBILE)));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_MOBILE_BLACK, codeMapping.apply(String.valueOf(BLACK_PHONE_FILTER)));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_SIGN_INVALID, codeMapping.apply(String.valueOf(SIGN_NOT_MATCH)));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_SIGN_INVALID, codeMapping.apply(String.valueOf(SIGN_NOT_VALID)));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_SIGN_INVALID, codeMapping.apply(String.valueOf(BAD_SIGN_FORMAT)));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_ACCOUNT_INVALID, codeMapping.apply(String.valueOf(BAD_API_KEY)));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_PERMISSION_DENY, codeMapping.apply(String.valueOf(API_NOT_ALLOWED)));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_IP_DENY, codeMapping.apply(String.valueOf(IP_NOT_ALLOWED)));
    }

}
