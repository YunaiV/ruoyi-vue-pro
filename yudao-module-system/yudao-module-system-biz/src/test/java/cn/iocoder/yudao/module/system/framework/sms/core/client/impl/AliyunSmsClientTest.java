package cn.iocoder.yudao.module.system.framework.sms.core.client.impl;

import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.iocoder.yudao.module.system.framework.sms.core.property.SmsChannelProperties;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

/**
 * {@link cn.iocoder.yudao.module.system.framework.sms.core.client.impl.AliyunSmsClient} 的单元测试
 *
 * @author 芋道源码
 */
public class AliyunSmsClientTest extends BaseMockitoUnitTest {

    private final SmsChannelProperties properties = new SmsChannelProperties()
            .setApiKey(randomString()) // 随机一个 apiKey，避免构建报错
            .setApiSecret(randomString()) // 随机一个 apiSecret，避免构建报错
            .setSignature("芋道源码");

    @InjectMocks
    private final AliyunSmsClient smsClient = new AliyunSmsClient(properties);

    @Test
    public void tesSendSms_success() throws Throwable {
        try (MockedStatic<HttpUtils> httpUtilsMockedStatic = mockStatic(HttpUtils.class)) {
            // 准备参数
            Long sendLogId = randomLongId();
            String mobile = randomString();
            String apiTemplateId = randomString();
            List<KeyValue<String, Object>> templateParams = Lists.newArrayList(
                    new KeyValue<>("code", 1234), new KeyValue<>("op", "login"));
            // mock 方法
            httpUtilsMockedStatic.when(() -> HttpUtils.post(anyString(), anyMap(), anyString()))
                    .thenReturn("{\"Message\":\"OK\",\"RequestId\":\"30067CE9-3710-5984-8881-909B21D8DB28\",\"Code\":\"OK\",\"BizId\":\"800025323183427988\"}");

            // 调用
            SmsSendRespDTO result = smsClient.sendSms(sendLogId, mobile,
                    apiTemplateId, templateParams);
            // 断言
            assertTrue(result.getSuccess());
            assertEquals("30067CE9-3710-5984-8881-909B21D8DB28", result.getApiRequestId());
            assertEquals("OK", result.getApiCode());
            assertEquals("OK", result.getApiMsg());
            assertEquals("800025323183427988", result.getSerialNo());
        }
    }

    @Test
    public void tesSendSms_fail() throws Throwable {
        try (MockedStatic<HttpUtils> httpUtilsMockedStatic = mockStatic(HttpUtils.class)) {
            // 准备参数
            Long sendLogId = randomLongId();
            String mobile = randomString();
            String apiTemplateId = randomString();
            List<KeyValue<String, Object>> templateParams = Lists.newArrayList(
                    new KeyValue<>("code", 1234), new KeyValue<>("op", "login"));
            // mock 方法
            httpUtilsMockedStatic.when(() -> HttpUtils.post(anyString(), anyMap(), anyString()))
                    .thenReturn("{\"Message\":\"手机号码格式错误\",\"RequestId\":\"B7700B8E-227E-5886-9564-26036172F01F\",\"Code\":\"isv.MOBILE_NUMBER_ILLEGAL\"}");

            // 调用
            SmsSendRespDTO result = smsClient.sendSms(sendLogId, mobile, apiTemplateId, templateParams);
            // 断言
            assertFalse(result.getSuccess());
            assertEquals("B7700B8E-227E-5886-9564-26036172F01F", result.getApiRequestId());
            assertEquals("isv.MOBILE_NUMBER_ILLEGAL", result.getApiCode());
            assertEquals("手机号码格式错误", result.getApiMsg());
            assertNull(result.getSerialNo());
        }
    }

    @Test
    public void testParseSmsReceiveStatus() {
        // 准备参数
        String text = "[\n" +
                "  {\n" +
                "    \"phone_number\" : \"13900000001\",\n" +
                "    \"send_time\" : \"2017-01-01 11:12:13\",\n" +
                "    \"report_time\" : \"2017-02-02 22:23:24\",\n" +
                "    \"success\" : true,\n" +
                "    \"err_code\" : \"DELIVERED\",\n" +
                "    \"err_msg\" : \"用户接收成功\",\n" +
                "    \"sms_size\" : \"1\",\n" +
                "    \"biz_id\" : \"12345\",\n" +
                "    \"out_id\" : \"67890\"\n" +
                "  }\n" +
                "]";
        // mock 方法

        // 调用
        List<SmsReceiveRespDTO> statuses = smsClient.parseSmsReceiveStatus(text);
        // 断言
        assertEquals(1, statuses.size());
        assertTrue(statuses.get(0).getSuccess());
        assertEquals("DELIVERED", statuses.get(0).getErrorCode());
        assertEquals("用户接收成功", statuses.get(0).getErrorMsg());
        assertEquals("13900000001", statuses.get(0).getMobile());
        assertEquals(LocalDateTime.of(2017, 2, 2, 22, 23, 24),
                statuses.get(0).getReceiveTime());
        assertEquals("12345", statuses.get(0).getSerialNo());
        assertEquals(67890L, statuses.get(0).getLogId());
    }

    @Test
    public void testGetSmsTemplate() throws Throwable {
        try (MockedStatic<HttpUtils> httpUtilsMockedStatic = mockStatic(HttpUtils.class)) {
            // 准备参数
            String apiTemplateId = randomString();
            // mock 方法
            httpUtilsMockedStatic.when(() -> HttpUtils.post(anyString(), anyMap(), anyString()))
                    .thenReturn("{\"TemplateCode\":\"SMS_207945135\",\"RequestId\":\"6F4CC077-29C8-5BA5-AB62-5FF95068A5AC\",\"Message\":\"OK\",\"TemplateContent\":\"您的验证码${code}，该验证码5分钟内有效，请勿泄漏于他人！\",\"TemplateName\":\"公告通知\",\"TemplateType\":0,\"Code\":\"OK\",\"CreateDate\":\"2020-12-23 17:34:42\",\"Reason\":\"无审批备注\",\"TemplateStatus\":1}");

            // 调用
            SmsTemplateRespDTO result = smsClient.getSmsTemplate(apiTemplateId);
            // 断言
            assertEquals("SMS_207945135", result.getId());
            assertEquals("您的验证码${code}，该验证码5分钟内有效，请勿泄漏于他人！", result.getContent());
            assertEquals(SmsTemplateAuditStatusEnum.SUCCESS.getStatus(), result.getAuditStatus());
            assertEquals("无审批备注", result.getAuditReason());
        }
    }

    @Test
    public void testConvertSmsTemplateAuditStatus() {
        assertEquals(SmsTemplateAuditStatusEnum.CHECKING.getStatus(),
                smsClient.convertSmsTemplateAuditStatus(0));
        assertEquals(SmsTemplateAuditStatusEnum.SUCCESS.getStatus(),
                smsClient.convertSmsTemplateAuditStatus(1));
        assertEquals(SmsTemplateAuditStatusEnum.FAIL.getStatus(),
                smsClient.convertSmsTemplateAuditStatus(2));
        assertThrows(IllegalArgumentException.class, () -> smsClient.convertSmsTemplateAuditStatus(3),
                "未知审核状态(3)");
    }

}
