package cn.iocoder.yudao.framework.sms.core.client.impl.aliyun;

import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.sms.core.client.SmsCommonResult;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.iocoder.yudao.framework.sms.core.property.SmsChannelProperties;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.sms.core.enums.SmsFrameworkErrorCodeConstants;
import com.aliyuncs.AcsRequest;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySmsTemplateRequest;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySmsTemplateResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    @SuppressWarnings("unchecked")
    public void testDoSendSms() throws ClientException {
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
        SmsCommonResult<SmsSendRespDTO> result = smsClient.doSendSms(sendLogId, mobile,
                apiTemplateId, templateParams);
        // 断言
        assertEquals(response.getCode(), result.getApiCode());
        assertEquals(response.getMessage(), result.getApiMsg());
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getCode(), result.getCode());
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getMsg(), result.getMsg());
        assertEquals(response.getRequestId(), result.getApiRequestId());
        // 断言结果
        assertEquals(response.getBizId(), result.getData().getSerialNo());
    }

    @Test
    public void testDoTParseSmsReceiveStatus() throws Throwable {
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
        List<SmsReceiveRespDTO> statuses = smsClient.doParseSmsReceiveStatus(text);
        // 断言
        assertEquals(1, statuses.size());
        assertTrue(statuses.get(0).getSuccess());
        assertEquals("DELIVERED", statuses.get(0).getErrorCode());
        assertEquals("用户接收成功", statuses.get(0).getErrorMsg());
        assertEquals("13900000001", statuses.get(0).getMobile());
        assertEquals(DateUtils.buildTime(2017, 2, 2, 22, 23, 24), statuses.get(0).getReceiveTime());
        assertEquals("12345", statuses.get(0).getSerialNo());
        assertEquals(67890L, statuses.get(0).getLogId());
    }

    @Test
    public void testDoGetSmsTemplate() throws ClientException {
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
        SmsCommonResult<SmsTemplateRespDTO> result = smsClient.doGetSmsTemplate(apiTemplateId);
        // 断言
        assertEquals(response.getCode(), result.getApiCode());
        assertEquals(response.getMessage(), result.getApiMsg());
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getCode(), result.getCode());
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getMsg(), result.getMsg());
        assertEquals(response.getRequestId(), result.getApiRequestId());
        // 断言结果
        assertEquals(response.getTemplateCode(), result.getData().getId());
        assertEquals(response.getTemplateContent(), result.getData().getContent());
        assertEquals(SmsTemplateAuditStatusEnum.SUCCESS.getStatus(), result.getData().getAuditStatus());
        assertEquals(response.getReason(), result.getData().getAuditReason());
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

    @Test
    @SuppressWarnings("unchecked")
    public void testInvoke_throwable() throws ClientException {
        // 准备参数
        QuerySmsTemplateRequest request = new QuerySmsTemplateRequest();
        // mock 方法
        ClientException ex = new ClientException("isv.INVALID_PARAMETERS", "参数不正确", randomString());
        when(client.getAcsResponse(any(AcsRequest.class))).thenThrow(ex);

        // 调用，并断言异常
        SmsCommonResult<?> result = smsClient.invoke(request,null);
        // 断言
        assertEquals(ex.getErrCode(), result.getApiCode());
        assertEquals(ex.getErrMsg(), result.getApiMsg());
        Assertions.assertEquals(SmsFrameworkErrorCodeConstants.SMS_API_PARAM_ERROR.getCode(), result.getCode());
        Assertions.assertEquals(SmsFrameworkErrorCodeConstants.SMS_API_PARAM_ERROR.getMsg(), result.getMsg());
        assertEquals(ex.getRequestId(), result.getApiRequestId());
    }

    @Test
    public void testInvoke_success() throws ClientException {
        // 准备参数
        QuerySmsTemplateRequest request = new QuerySmsTemplateRequest();
        Function<QuerySmsTemplateResponse, SmsTemplateRespDTO> responseConsumer = response -> {
            SmsTemplateRespDTO data = new SmsTemplateRespDTO();
            data.setId(response.getTemplateCode()).setContent(response.getTemplateContent());
            data.setAuditStatus(SmsTemplateAuditStatusEnum.SUCCESS.getStatus()).setAuditReason(response.getReason());
            return data;
        };
        // mock 方法
        QuerySmsTemplateResponse response = randomPojo(QuerySmsTemplateResponse.class, o -> {
            o.setCode("OK");
            o.setTemplateStatus(1); // 设置模板通过
        });
        when(client.getAcsResponse(any(AcsRequest.class))).thenReturn(response);

        // 调用
        SmsCommonResult<SmsTemplateRespDTO> result = smsClient.invoke(request, responseConsumer);
        // 断言
        assertEquals(response.getCode(), result.getApiCode());
        assertEquals(response.getMessage(), result.getApiMsg());
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getCode(), result.getCode());
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getMsg(), result.getMsg());
        assertEquals(response.getRequestId(), result.getApiRequestId());
        // 断言结果
        assertEquals(response.getTemplateCode(), result.getData().getId());
        assertEquals(response.getTemplateContent(), result.getData().getContent());
        assertEquals(SmsTemplateAuditStatusEnum.SUCCESS.getStatus(), result.getData().getAuditStatus());
        assertEquals(response.getReason(), result.getData().getAuditReason());
    }

}
