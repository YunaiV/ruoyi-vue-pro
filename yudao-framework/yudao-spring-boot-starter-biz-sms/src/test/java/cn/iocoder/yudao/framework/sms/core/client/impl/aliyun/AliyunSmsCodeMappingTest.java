package cn.iocoder.yudao.framework.sms.core.client.impl.aliyun;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.sms.core.enums.SmsFrameworkErrorCodeConstants;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link AliyunSmsCodeMapping} 的单元测试
 *
 * @author 芋道源码
 */
public class AliyunSmsCodeMappingTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AliyunSmsCodeMapping codeMapping;

    @Test
    public void testApply() {
        assertEquals(GlobalErrorCodeConstants.SUCCESS, codeMapping.apply("OK"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_ACCOUNT_INVALID, codeMapping.apply("MissingAccessKeyId"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_ACCOUNT_INVALID, codeMapping.apply("isv.ACCOUNT_NOT_EXISTS"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_ACCOUNT_INVALID, codeMapping.apply("isv.ACCOUNT_ABNORMAL"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_SEND_DAY_LIMIT_CONTROL, codeMapping.apply("isv.DAY_LIMIT_CONTROL"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_SEND_CONTENT_INVALID, codeMapping.apply("isv.SMS_CONTENT_ILLEGAL"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_SIGN_INVALID, codeMapping.apply("isv.SMS_SIGN_ILLEGAL"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_SIGN_INVALID, codeMapping.apply("isv.SIGN_NAME_ILLEGAL"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_PERMISSION_DENY, codeMapping.apply("isp.RAM_PERMISSION_DENY"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_ACCOUNT_MONEY_NOT_ENOUGH, codeMapping.apply("isv.OUT_OF_SERVICE"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_ACCOUNT_MONEY_NOT_ENOUGH, codeMapping.apply("isv.AMOUNT_NOT_ENOUGH"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_TEMPLATE_INVALID, codeMapping.apply("isv.SMS_TEMPLATE_ILLEGAL"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_SIGN_INVALID, codeMapping.apply("isv.SMS_SIGNATURE_ILLEGAL"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_API_PARAM_ERROR, codeMapping.apply("isv.INVALID_PARAMETERS"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_API_PARAM_ERROR, codeMapping.apply("isv.INVALID_JSON_PARAM"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_MOBILE_INVALID, codeMapping.apply("isv.MOBILE_NUMBER_ILLEGAL"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_TEMPLATE_PARAM_ERROR, codeMapping.apply("isv.TEMPLATE_MISSING_PARAMETERS"));
        assertEquals(SmsFrameworkErrorCodeConstants.SMS_SEND_BUSINESS_LIMIT_CONTROL, codeMapping.apply("isv.BUSINESS_LIMIT_CONTROL"));
    }

}
