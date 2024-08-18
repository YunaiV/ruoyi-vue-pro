package cn.iocoder.yudao.module.system.framework.sms.core.client.impl;

import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
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
 * {@link HuaweiSmsClient} 的单元测试
 *
 * @author scholar
 */
public class HuaweiSmsClientTest extends BaseMockitoUnitTest {

    private final SmsChannelProperties properties = new SmsChannelProperties()
            .setApiKey(randomString())// 随机一个 apiKey，避免构建报错
            .setApiSecret(randomString()) // 随机一个 apiSecret，避免构建报错
            .setSignature("芋道源码");

    @InjectMocks
    private HuaweiSmsClient smsClient = new HuaweiSmsClient(properties);

    @Test
    public void testDoInit() {
        // 调用
        smsClient.doInit();
    }

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
                    .thenReturn(
                            "{\"result\":[{\"originTo\":\"+86155****5678\",\"createTime\":\"2018-05-25T16:34:34Z\",\"from\":\"1069********0012\",\"smsMsgId\":\"d6e3cdd0-522b-4692-8304-a07553cdf591_8539659\",\"status\":\"000000\",\"countryId\":\"CN\",\"total\":2}],\"code\":\"000000\",\"description\":\"Success\"}\n"
                    );

            // 调用
            SmsSendRespDTO result = smsClient.sendSms(sendLogId, mobile,
                    apiTemplateId, templateParams);
            // 断言
            assertTrue(result.getSuccess());
            assertEquals("d6e3cdd0-522b-4692-8304-a07553cdf591_8539659", result.getSerialNo());
            assertEquals("000000", result.getApiCode());

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
                    .thenReturn(
                            "{\"result\":[{\"originTo\":\"+86155****5678\",\"createTime\":\"2018-05-25T16:34:34Z\",\"from\":\"1069********0012\",\"smsMsgId\":\"d6e3cdd0-522b-4692-8304-a07553cdf591_8539659\",\"status\":\"E200015\",\"countryId\":\"CN\",\"total\":2}],\"code\":\"E000000\",\"description\":\"Success\"}\n"
                    );

            // 调用
            SmsSendRespDTO result = smsClient.sendSms(sendLogId, mobile,
                    apiTemplateId, templateParams);
            // 断言
            assertFalse(result.getSuccess());
            assertEquals("d6e3cdd0-522b-4692-8304-a07553cdf591_8539659", result.getSerialNo());
            assertEquals("E200015", result.getApiCode());
        }
    }

    @Test
    public void testParseSmsReceiveStatus() {
        // 准备参数
        String text = "sequence=1&total=1&statusDesc=%E7%94%A8%E6%88%B7%E5%B7%B2%E6%88%90%E5%8A%9F%E6%94%B6%E5%88%B0%E7%9F%AD%E4%BF%A1&updateTime=2024-08-15T03%3A00%3A34Z&source=2&smsMsgId=70207ed7-1d02-41b0-8537-bb25fd1c2364_143684459&status=DELIVRD&extend=176";

        // 调用
        List<SmsReceiveRespDTO> statuses = smsClient.parseSmsReceiveStatus(text);
        // 断言
        assertEquals(1, statuses.size());
        assertTrue(statuses.getFirst().getSuccess());
        assertEquals("DELIVRD", statuses.getFirst().getErrorCode());
        assertEquals(LocalDateTime.of(2024, 8, 15, 3, 0, 34), statuses.getFirst().getReceiveTime());
        assertEquals("70207ed7-1d02-41b0-8537-bb25fd1c2364_143684459", statuses.getFirst().getSerialNo());
    }

}
