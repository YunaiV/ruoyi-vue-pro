package cn.iocoder.yudao.framework.sms.core.client.impl.tencent;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.sms.core.client.SmsCommonResult;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsTemplateRespDTO;
import cn.iocoder.yudao.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import cn.iocoder.yudao.framework.sms.core.property.SmsChannelProperties;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import com.google.common.collect.Lists;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.DescribeSmsTemplateListResponse;
import com.tencentcloudapi.sms.v20210111.models.DescribeTemplateListStatus;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

/**
 * {@link TencentSmsClient} 的单元测试
 *
 * @author shiwp
 */
public class TencentSmsClientTest extends BaseMockitoUnitTest {

    private final SmsChannelProperties properties = new SmsChannelProperties()
            .setApiKey(randomString() + " " + randomString()) // 随机一个 apiKey，避免构建报错
            .setApiSecret(randomString()) // 随机一个 apiSecret，避免构建报错
            .setSignature("芋道源码");

    @InjectMocks
    private TencentSmsClient smsClient = new TencentSmsClient(properties);

    @Mock
    private SmsClient client;

    @Test
    public void testDoInit() {
        // 准备参数
        // mock 方法

        // 调用
        smsClient.doInit();
        // 断言
        assertNotSame(client, ReflectUtil.getFieldValue(smsClient, "client"));
    }

    @Test
    public void testRefresh() {
        // 准备参数
        SmsChannelProperties p = new SmsChannelProperties()
                .setApiKey(randomString() + " " + randomString()) // 随机一个 apiKey，避免构建报错
                .setApiSecret(randomString()) // 随机一个 apiSecret，避免构建报错
                .setSignature("芋道源码");
        // 调用
        smsClient.refresh(p);
        // 断言
        assertNotSame(client, ReflectUtil.getFieldValue(smsClient, "client"));
    }

    @Test
    public void testDoSendSms() throws Throwable {
        // 准备参数
        Long sendLogId = randomLongId();
        String mobile = randomString();
        String apiTemplateId = randomString();
        List<KeyValue<String, Object>> templateParams = Lists.newArrayList(
                new KeyValue<>("1", 1234), new KeyValue<>("2", "login"));
        String requestId = randomString();
        String serialNo = randomString();
        // mock 方法
        SendSmsResponse response = randomPojo(SendSmsResponse.class, o -> {
            o.setRequestId(requestId);
            SendStatus[] sendStatuses = new SendStatus[1];
            o.setSendStatusSet(sendStatuses);
            SendStatus sendStatus = new SendStatus();
            sendStatuses[0] = sendStatus;
            sendStatus.setCode(TencentSmsClient.API_SUCCESS_CODE);
            sendStatus.setMessage("send success");
            sendStatus.setSerialNo(serialNo);
        });
        when(client.SendSms(argThat(request -> {
            assertEquals(mobile, request.getPhoneNumberSet()[0]);
            assertEquals(properties.getSignature(), request.getSignName());
            assertEquals(apiTemplateId, request.getTemplateId());
            assertEquals(toJsonString(ArrayUtils.toArray(new ArrayList<>(MapUtils.convertMap(templateParams).values()), String::valueOf)),
                    toJsonString(request.getTemplateParamSet()));
            assertEquals(sendLogId, ReflectUtil.getFieldValue(JsonUtils.parseObject(request.getSessionContext(), TencentSmsClient.SessionContext.class), "logId"));
            return true;
        }))).thenReturn(response);

        // 调用
        SmsCommonResult<SmsSendRespDTO> result = smsClient.doSendSms(sendLogId, mobile,
                apiTemplateId, templateParams);
        // 断言
        assertEquals(response.getSendStatusSet()[0].getCode(), result.getApiCode());
        assertEquals(response.getSendStatusSet()[0].getMessage(), result.getApiMsg());
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getCode(), result.getCode());
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getMsg(), result.getMsg());
        assertEquals(response.getRequestId(), result.getApiRequestId());
        // 断言结果
        assertEquals(response.getSendStatusSet()[0].getSerialNo(), result.getData().getSerialNo());
    }

    @Test
    public void testDoTParseSmsReceiveStatus() throws Throwable {
        // 准备参数
        String text = "[\n" +
                "    {\n" +
                "        \"user_receive_time\": \"2015-10-17 08:03:04\",\n" +
                "        \"nationcode\": \"86\",\n" +
                "        \"mobile\": \"13900000001\",\n" +
                "        \"report_status\": \"SUCCESS\",\n" +
                "        \"errmsg\": \"DELIVRD\",\n" +
                "        \"description\": \"用户短信送达成功\",\n" +
                "        \"sid\": \"12345\",\n" +
                "        \"ext\": {\"logId\":\"67890\"}\n" +
                "    }\n" +
                "]";
        // mock 方法

        // 调用
        List<SmsReceiveRespDTO> statuses = smsClient.doParseSmsReceiveStatus(text);
        // 断言
        assertEquals(1, statuses.size());
        assertTrue(statuses.get(0).getSuccess());
        assertEquals("DELIVRD", statuses.get(0).getErrorCode());
        assertEquals("用户短信送达成功", statuses.get(0).getErrorMsg());
        assertEquals("13900000001", statuses.get(0).getMobile());
        assertEquals(DateUtils.buildTime(2015, 10, 17, 8, 3, 4), statuses.get(0).getReceiveTime());
        assertEquals("12345", statuses.get(0).getSerialNo());
        assertEquals(67890L, statuses.get(0).getLogId());
    }

    @Test
    public void testDoGetSmsTemplate() throws Throwable {
        // 准备参数
        Long apiTemplateId = randomLongId();
        String requestId = randomString();

        // mock 方法
        DescribeSmsTemplateListResponse response = randomPojo(DescribeSmsTemplateListResponse.class, o -> {
            DescribeTemplateListStatus[] describeTemplateListStatuses = new DescribeTemplateListStatus[1];
            DescribeTemplateListStatus templateStatus = new DescribeTemplateListStatus();
            templateStatus.setTemplateId(apiTemplateId);
            templateStatus.setStatusCode(0L);// 设置模板通过
            describeTemplateListStatuses[0] = templateStatus;
            o.setDescribeTemplateStatusSet(describeTemplateListStatuses);
            o.setRequestId(requestId);
        });
        when(client.DescribeSmsTemplateList(argThat(request -> {
            assertEquals(apiTemplateId, request.getTemplateIdSet()[0]);
            return true;
        }))).thenReturn(response);

        // 调用
        SmsCommonResult<SmsTemplateRespDTO> result = smsClient.doGetSmsTemplate(apiTemplateId.toString());
        // 断言
        assertEquals(TencentSmsClient.API_SUCCESS_CODE, result.getApiCode());
        assertNull(result.getApiMsg());
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getCode(), result.getCode());
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getMsg(), result.getMsg());
        assertEquals(response.getRequestId(), result.getApiRequestId());
        // 断言结果
        assertEquals(response.getDescribeTemplateStatusSet()[0].getTemplateId().toString(), result.getData().getId());
        assertEquals(response.getDescribeTemplateStatusSet()[0].getTemplateContent(), result.getData().getContent());
        assertEquals(SmsTemplateAuditStatusEnum.SUCCESS.getStatus(), result.getData().getAuditStatus());
        assertEquals(response.getDescribeTemplateStatusSet()[0].getReviewReply(), result.getData().getAuditReason());
    }

    @Test
    public void testConvertSuccessTemplateStatus() {
        testTemplateStatus(SmsTemplateAuditStatusEnum.SUCCESS, 0L);
    }

    @Test
    public void testConvertCheckingTemplateStatus() {
        testTemplateStatus(SmsTemplateAuditStatusEnum.CHECKING, 1L);
    }

    @Test
    public void testConvertFailTemplateStatus() {
        testTemplateStatus(SmsTemplateAuditStatusEnum.FAIL, -1L);
    }

    @Test
    public void testConvertUnknownTemplateStatus() {
        DescribeTemplateListStatus templateStatus = new DescribeTemplateListStatus();
        templateStatus.setStatusCode(3L);
        Long templateId = randomLongId();
        // 调用，并断言结果
        assertThrows(IllegalStateException.class, () -> smsClient.convertTemplateStatusDTO(templateStatus),
                StrUtil.format("不能解析短信模版审核状态[3]，模版id[{}]", templateId));
    }

    private void testTemplateStatus(SmsTemplateAuditStatusEnum expected, Long value) {
        DescribeTemplateListStatus templateStatus = new DescribeTemplateListStatus();
        templateStatus.setStatusCode(value);
        SmsTemplateRespDTO result = smsClient.convertTemplateStatusDTO(templateStatus);
        assertEquals(expected.getStatus(), result.getAuditStatus());
    }

}
