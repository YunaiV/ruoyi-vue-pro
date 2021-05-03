package cn.iocoder.yudao.framework.sms.core.client.impl.yunpian;

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
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import com.google.common.collect.Lists;
import com.yunpian.sdk.YunpianClient;
import com.yunpian.sdk.api.SmsApi;
import com.yunpian.sdk.api.TplApi;
import com.yunpian.sdk.constant.YunpianConstant;
import com.yunpian.sdk.model.Result;
import com.yunpian.sdk.model.SmsSingleSend;
import com.yunpian.sdk.model.Template;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static com.yunpian.sdk.constant.Code.OK;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 对 {@link YunpianSmsClient} 的单元测试
 *
 * @author 芋道源码
 */
public class YunpianSmsClientTest extends BaseMockitoUnitTest {

    private final SmsChannelProperties properties = new SmsChannelProperties()
            .setApiKey(randomString()); // 随机一个 apiKey，避免构建报错

    @InjectMocks
    private final YunpianSmsClient smsClient = new YunpianSmsClient(properties);

    @Mock
    private YunpianClient client;

    @Test
    public void testDoInit() {
        // 准备参数
        // mock 方法

        // 调用
        smsClient.doInit();
        // 断言
        assertNotEquals(client, ReflectUtil.getFieldValue(smsClient, "client"));
        verify(client, times(1)).close();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDoSendSms() throws Throwable {
        // 准备参数
        Long sendLogId = randomLongId();
        String mobile = randomString();
        String apiTemplateId = randomString();
        List<KeyValue<String, Object>> templateParams = Lists.newArrayList(
                new KeyValue<>("code", 1234), new KeyValue<>("op", "login"));
        // mock sms 方法
        SmsApi smsApi = mock(SmsApi.class);
        when(client.sms()).thenReturn(smsApi);
        // mock tpl_single_send 方法
        Map<String, String> request = new HashMap<>();
        request.put(YunpianConstant.MOBILE, mobile);
        request.put(YunpianConstant.TPL_ID, apiTemplateId);
        request.put(YunpianConstant.TPL_VALUE, "#code#=1234&#op#=login");
        request.put(YunpianConstant.UID, String.valueOf(sendLogId));
        request.put(YunpianConstant.CALLBACK_URL, properties.getCallbackUrl());
        Result<SmsSingleSend> responseResult = randomPojo(Result.class, SmsSingleSend.class,
                o -> o.setCode(OK)); // API 发送成功的 code
        when(smsApi.tpl_single_send(eq(request))).thenReturn(responseResult);

        // 调用
        SmsCommonResult<SmsSendRespDTO> result = smsClient.doSendSms(sendLogId, mobile,
                apiTemplateId, templateParams);
        // 断言
        assertEquals(String.valueOf(responseResult.getCode()), result.getApiCode());
        assertEquals(responseResult.getMsg() + " => " + responseResult.getDetail(), result.getApiMsg());
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getCode(), result.getCode());
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getMsg(), result.getMsg());
        assertNull(result.getApiRequestId());
        // 断言结果
        assertEquals(String.valueOf(responseResult.getData().getSid()), result.getData().getSerialNo());
    }

    @Test
    public void testDoParseSmsReceiveStatus() throws Throwable {
        // 准备参数
        String text = "[{\"sid\":9527,\"uid\":1024,\"user_receive_time\":\"2014-03-17 22:55:21\",\"error_msg\":\"\",\"mobile\":\"15205201314\",\"report_status\":\"SUCCESS\"}]";
        // mock 方法

        // 调用

        // 断言
        // 调用
        List<SmsReceiveRespDTO> statuses = smsClient.doParseSmsReceiveStatus(text);
        // 断言
        assertEquals(1, statuses.size());
        assertTrue(statuses.get(0).getSuccess());
        assertEquals("", statuses.get(0).getErrorCode());
        assertNull(statuses.get(0).getErrorMsg());
        assertEquals("15205201314", statuses.get(0).getMobile());
        assertEquals(DateUtils.buildTime(2014, 3, 17, 22, 55, 21), statuses.get(0).getReceiveTime());
        assertEquals("9527", statuses.get(0).getSerialNo());
        assertEquals(1024L, statuses.get(0).getLogId());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDoGetSmsTemplate() throws Throwable {
        // 准备参数
        String apiTemplateId = randomString();
        // mock tpl 方法
        TplApi tplApi = mock(TplApi.class);
        when(client.tpl()).thenReturn(tplApi);
        // mock get 方法
        Map<String, String> request = new HashMap<>();
        request.put(YunpianConstant.APIKEY, properties.getApiKey());
        request.put(YunpianConstant.TPL_ID, apiTemplateId);
        Result<List<Template>> responseResult = randomPojo(Result.class, List.class, o -> {
            o.setCode(OK); // API 发送成功的 code
            o.setData(randomPojoList(Template.class, t -> t.setCheck_status("SUCCESS")));
        });
        when(tplApi.get(eq(request))).thenReturn(responseResult);

        // 调用
        SmsCommonResult<SmsTemplateRespDTO> result = smsClient.doGetSmsTemplate(apiTemplateId);
        // 断言
        assertEquals(String.valueOf(responseResult.getCode()), result.getApiCode());
        assertEquals(responseResult.getMsg() + " => " + responseResult.getDetail(), result.getApiMsg());
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getCode(), result.getCode());
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getMsg(), result.getMsg());
        assertNull(result.getApiRequestId());
        // 断言结果
        Template template = responseResult.getData().get(0);
        assertEquals(template.getTpl_id().toString(), result.getData().getId());
        assertEquals(template.getTpl_content(), result.getData().getContent());
        assertEquals(SmsTemplateAuditStatusEnum.SUCCESS.getStatus(), result.getData().getAuditStatus());
        assertEquals(template.getReason(), result.getData().getAuditReason());
    }

    @Test
    public void testConvertSmsTemplateAuditStatus() {
        assertEquals(SmsTemplateAuditStatusEnum.CHECKING.getStatus(),
                smsClient.convertSmsTemplateAuditStatus("CHECKING"));
        assertEquals(SmsTemplateAuditStatusEnum.SUCCESS.getStatus(),
                smsClient.convertSmsTemplateAuditStatus("SUCCESS"));
        assertEquals(SmsTemplateAuditStatusEnum.FAIL.getStatus(),
                smsClient.convertSmsTemplateAuditStatus("FAIL"));
        assertThrows(IllegalArgumentException.class, () -> smsClient.convertSmsTemplateAuditStatus("test"),
                "未知审核状态(test)");
    }

    @Test
    public void testInvoke_throwable() {
        // 准备参数
        Supplier<Result<Object>> requestConsumer =
                () -> new Result<>().setThrowable(new NullPointerException());
        // mock 方法

        // 调用，并断言异常
        assertThrows(NullPointerException.class,
                () -> smsClient.invoke(requestConsumer, null));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testInvoke_success() throws Throwable {
        // 准备参数
        Result<SmsSingleSend> responseResult = randomPojo(Result.class, SmsSingleSend.class, o -> o.setCode(OK));
        Supplier<Result<SmsSingleSend>> requestConsumer = () -> responseResult;
        Function<SmsSingleSend, SmsSendRespDTO> responseConsumer =
                smsSingleSend -> new SmsSendRespDTO().setSerialNo(String.valueOf(responseResult.getData().getSid()));
        // mock 方法

        // 调用
        SmsCommonResult<SmsSendRespDTO> result = smsClient.invoke(requestConsumer, responseConsumer);
        // 断言
        assertEquals(String.valueOf(responseResult.getCode()), result.getApiCode());
        assertEquals(responseResult.getMsg() + " => " + responseResult.getDetail(), result.getApiMsg());
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getCode(), result.getCode());
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getMsg(), result.getMsg());
        assertNull(result.getApiRequestId());
        assertEquals(String.valueOf(responseResult.getData().getSid()), result.getData().getSerialNo());
    }

}
