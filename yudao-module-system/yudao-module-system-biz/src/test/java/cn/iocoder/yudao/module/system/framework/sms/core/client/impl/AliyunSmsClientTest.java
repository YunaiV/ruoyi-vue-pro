package cn.iocoder.yudao.module.system.framework.sms.core.client.impl;

import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.iocoder.yudao.module.system.framework.sms.core.property.SmsChannelProperties;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySmsTemplateRequest;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySmsTemplateResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

/**
 * {@link AliyunSmsClient} 的单元测试
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

    @Mock
    private IAcsClient client;

    @Test
    public void testDoInit() {
        // 准备参数
        // mock 方法

        // 调用
        smsClient.doInit();
        // 断言
        assertNotSame(client, ReflectUtil.getFieldValue(smsClient, "acsClient"));
    }

    @Test
    public void tesSendSms_success() throws Throwable {
        // 准备参数
        Long sendLogId = randomLongId();
        String mobile = randomString();
        String apiTemplateId = randomString();
        List<KeyValue<String, Object>> templateParams = Lists.newArrayList(
                new KeyValue<>("code", 1234), new KeyValue<>("op", "login"));
        // mock 方法
        SendSmsResponse response = randomPojo(SendSmsResponse.class, o -> o.setCode("OK"));
        when(client.getAcsResponse(argThat((ArgumentMatcher<SendSmsRequest>) acsRequest -> {
            assertEquals(mobile, acsRequest.getPhoneNumbers());
            assertEquals(properties.getSignature(), acsRequest.getSignName());
            assertEquals(apiTemplateId, acsRequest.getTemplateCode());
            assertEquals(toJsonString(MapUtils.convertMap(templateParams)), acsRequest.getTemplateParam());
            assertEquals(sendLogId.toString(), acsRequest.getOutId());
            return true;
        }))).thenReturn(response);

        // 调用
        SmsSendRespDTO result = smsClient.sendSms(sendLogId, mobile,
                apiTemplateId, templateParams);
        // 断言
        assertTrue(result.getSuccess());
        assertEquals(response.getRequestId(), result.getApiRequestId());
        assertEquals(response.getCode(), result.getApiCode());
        assertEquals(response.getMessage(), result.getApiMsg());
        assertEquals(response.getBizId(), result.getSerialNo());
    }

    @Test
    public void tesSendSms_fail() throws Throwable {
        // 准备参数
        Long sendLogId = randomLongId();
        String mobile = randomString();
        String apiTemplateId = randomString();
        List<KeyValue<String, Object>> templateParams = Lists.newArrayList(
                new KeyValue<>("code", 1234), new KeyValue<>("op", "login"));
        // mock 方法
        SendSmsResponse response = randomPojo(SendSmsResponse.class, o -> o.setCode("ERROR"));
        when(client.getAcsResponse(argThat((ArgumentMatcher<SendSmsRequest>) acsRequest -> {
            assertEquals(mobile, acsRequest.getPhoneNumbers());
            assertEquals(properties.getSignature(), acsRequest.getSignName());
            assertEquals(apiTemplateId, acsRequest.getTemplateCode());
            assertEquals(toJsonString(MapUtils.convertMap(templateParams)), acsRequest.getTemplateParam());
            assertEquals(sendLogId.toString(), acsRequest.getOutId());
            return true;
        }))).thenReturn(response);

        // 调用
        SmsSendRespDTO result = smsClient.sendSms(sendLogId, mobile, apiTemplateId, templateParams);
        // 断言
        assertFalse(result.getSuccess());
        assertEquals(response.getRequestId(), result.getApiRequestId());
        assertEquals(response.getCode(), result.getApiCode());
        assertEquals(response.getMessage(), result.getApiMsg());
        assertEquals(response.getBizId(), result.getSerialNo());
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
        // 准备参数
        String apiTemplateId = randomString();
        // mock 方法
        QuerySmsTemplateResponse response = randomPojo(QuerySmsTemplateResponse.class, o -> {
            o.setCode("OK");
            o.setTemplateStatus(1); // 设置模板通过
        });
        when(client.getAcsResponse(argThat((ArgumentMatcher<QuerySmsTemplateRequest>) acsRequest -> {
            assertEquals(apiTemplateId, acsRequest.getTemplateCode());
            return true;
        }))).thenReturn(response);

        // 调用
        SmsTemplateRespDTO result = smsClient.getSmsTemplate(apiTemplateId);
        // 断言
        assertEquals(response.getTemplateCode(), result.getId());
        assertEquals(response.getTemplateContent(), result.getContent());
        assertEquals(SmsTemplateAuditStatusEnum.SUCCESS.getStatus(), result.getAuditStatus());
        assertEquals(response.getReason(), result.getAuditReason());
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
