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

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

/**
 * {@link QiniuSmsClient} 的单元测试
 *
 * @author scholar
 */
public class QiniuSmsClientTest extends BaseMockitoUnitTest {

    private final SmsChannelProperties properties = new SmsChannelProperties()
            .setApiKey(randomString())// 随机一个 apiKey，避免构建报错
            .setApiSecret(randomString()) // 随机一个 apiSecret，避免构建报错
            .setSignature("芋道源码");

    @InjectMocks
    private QiniuSmsClient smsClient = new QiniuSmsClient(properties);

    @Test
    public void testDoSendSms_success() throws Throwable {
        try (MockedStatic<HttpUtils> httpUtilsMockedStatic = mockStatic(HttpUtils.class)) {
            // 准备参数
            Long sendLogId = randomLongId();
            String mobile = randomString();
            String apiTemplateId = randomString() + " " + randomString();
            List<KeyValue<String, Object>> templateParams = Lists.newArrayList(
                    new KeyValue<>("1", 1234), new KeyValue<>("2", "login"));
            // mock 方法
            httpUtilsMockedStatic.when(() -> HttpUtils.post(anyString(), anyMap(), anyString()))
                    .thenReturn("{\"message_id\":\"17245678901\"}");
            // 调用
            SmsSendRespDTO result = smsClient.sendSms(sendLogId, mobile,
                    apiTemplateId, templateParams);
            // 断言
            assertTrue(result.getSuccess());
            assertEquals("17245678901", result.getSerialNo());
        }
    }

    @Test
    public void testDoSendSms_fail() throws Throwable {
        try (MockedStatic<HttpUtils> httpUtilsMockedStatic = mockStatic(HttpUtils.class)) {
            // 准备参数
            Long sendLogId = randomLongId();
            String mobile = randomString();
            String apiTemplateId = randomString() + " " + randomString();
            List<KeyValue<String, Object>> templateParams = Lists.newArrayList(
                    new KeyValue<>("1", 1234), new KeyValue<>("2", "login"));
            // mock 方法
            httpUtilsMockedStatic.when(() -> HttpUtils.post(anyString(), anyMap(), anyString()))
                    .thenReturn("{\"error\":\"BadToken\",\"message\":\"Your authorization token is invalid\",\"request_id\":\"etziWcJFo1C8Ne8X\"}");
            // 调用
            SmsSendRespDTO result = smsClient.sendSms(sendLogId, mobile,
                    apiTemplateId, templateParams);
            // 断言
            assertFalse(result.getSuccess());
            assertEquals("BadToken", result.getApiCode());
            assertEquals("Your authorization token is invalid", result.getApiMsg());
            assertEquals("etziWcJFo1C8Ne8X", result.getApiRequestId());
        }
    }

    @Test
    public void testGetSmsTemplate() throws Throwable {
        try (MockedStatic<HttpUtils> httpUtilsMockedStatic = mockStatic(HttpUtils.class)) {
            // 准备参数
            String apiTemplateId = randomString();
            // mock 方法
            httpUtilsMockedStatic.when(() -> HttpUtils.get(anyString(), anyMap()))
                    .thenReturn("{\"audit_status\":\"passed\",\"created_at\":1724231187,\"description\":\"\",\"disable_broadcast\":false,\"disable_broadcast_reason\":\"\",\"disable_reason\":\"\",\"disabled\":false,\"id\":\"1826184073773596672\",\"is_oversea\":false,\"name\":\"dd\",\"parameters\":[\"code\"],\"reject_reason\":\"\",\"signature_id\":\"1826099896017498112\",\"signature_text\":\"yudao\",\"template\":\"您的验证码为：${code}\",\"type\":\"verification\",\"uid\":1383022432,\"updated_at\":1724288561,\"variable_count\":0}");
            // 调用
            SmsTemplateRespDTO result = smsClient.getSmsTemplate(apiTemplateId);
            // 断言
            assertEquals("1826184073773596672", result.getId());
            assertEquals("您的验证码为：${code}", result.getContent());
            assertEquals(SmsTemplateAuditStatusEnum.SUCCESS.getStatus(), result.getAuditStatus());
            assertEquals("", result.getAuditReason());
        }
    }

    @Test
    public void testParseSmsReceiveStatus() {
        // 准备参数
        String text = "{\"items\":[{\"mobile\":\"18881234567\",\"message_id\":\"10135515063508004167\",\"status\":\"DELIVRD\",\"delivrd_at\":1724591666,\"error\":\"DELIVRD\",\"seq\":\"123\"}]}";
        // 调用
        List<SmsReceiveRespDTO> statuses = smsClient.parseSmsReceiveStatus(text);
        // 断言
        assertEquals(1, statuses.size());
        SmsReceiveRespDTO status = statuses.get(0);
        assertTrue(status.getSuccess());
        assertEquals("DELIVRD", status.getErrorMsg());
        assertEquals(LocalDateTime.of(2024, 8, 25, 21, 14, 26), status.getReceiveTime());
        assertEquals("18881234567", status.getMobile());
        assertEquals("10135515063508004167", status.getSerialNo());
        assertEquals(123, status.getLogId());
    }

    @Test
    public void testConvertSmsTemplateAuditStatus() {
        assertEquals(SmsTemplateAuditStatusEnum.SUCCESS.getStatus(),
                smsClient.convertSmsTemplateAuditStatus("passed"));
        assertEquals(SmsTemplateAuditStatusEnum.CHECKING.getStatus(),
                smsClient.convertSmsTemplateAuditStatus("reviewing"));
        assertEquals(SmsTemplateAuditStatusEnum.FAIL.getStatus(),
                smsClient.convertSmsTemplateAuditStatus("rejected"));
        assertThrows(IllegalArgumentException.class, () -> smsClient.convertSmsTemplateAuditStatus("unknown"),
                "未知审核状态(3)");
    }
}