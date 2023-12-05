package cn.iocoder.yudao.module.system.service.sms;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.sms.core.client.SmsClient;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsReceiveRespDTO;
import cn.iocoder.yudao.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.system.dal.dataobject.sms.SmsChannelDO;
import cn.iocoder.yudao.module.system.dal.dataobject.sms.SmsTemplateDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.mq.message.sms.SmsSendMessage;
import cn.iocoder.yudao.module.system.mq.producer.sms.SmsProducer;
import cn.iocoder.yudao.module.system.service.member.MemberService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class SmsSendServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SmsSendServiceImpl smsSendService;

    @Mock
    private AdminUserService adminUserService;
    @Mock
    private MemberService memberService;
    @Mock
    private SmsChannelService smsChannelService;
    @Mock
    private SmsTemplateService smsTemplateService;
    @Mock
    private SmsLogService smsLogService;
    @Mock
    private SmsProducer smsProducer;

    @Test
    public void testSendSingleSmsToAdmin() {
        // 准备参数
        Long userId = randomLongId();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        // mock adminUserService 的方法
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setMobile("15601691300"));
        when(adminUserService.getUser(eq(userId))).thenReturn(user);

        // mock SmsTemplateService 的方法
        SmsTemplateDO template = randomPojo(SmsTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        when(smsTemplateService.getSmsTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String content = randomString();
        when(smsTemplateService.formatSmsTemplateContent(eq(template.getContent()), eq(templateParams)))
                .thenReturn(content);
        // mock SmsChannelService 的方法
        SmsChannelDO smsChannel = randomPojo(SmsChannelDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(smsChannelService.getSmsChannel(eq(template.getChannelId()))).thenReturn(smsChannel);
        // mock SmsLogService 的方法
        Long smsLogId = randomLongId();
        when(smsLogService.createSmsLog(eq(user.getMobile()), eq(userId), eq(UserTypeEnum.ADMIN.getValue()), eq(Boolean.TRUE), eq(template),
                eq(content), eq(templateParams))).thenReturn(smsLogId);

        // 调用
        Long resultSmsLogId = smsSendService.sendSingleSmsToAdmin(null, userId, templateCode, templateParams);
        // 断言
        assertEquals(smsLogId, resultSmsLogId);
        // 断言调用
        verify(smsProducer).sendSmsSendMessage(eq(smsLogId), eq(user.getMobile()),
                eq(template.getChannelId()), eq(template.getApiTemplateId()),
                eq(Lists.newArrayList(new KeyValue<>("code", "1234"), new KeyValue<>("op", "login"))));
    }

    @Test
    public void testSendSingleSmsToUser() {
        // 准备参数
        Long userId = randomLongId();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        // mock memberService 的方法
        String mobile = "15601691300";
        when(memberService.getMemberUserMobile(eq(userId))).thenReturn(mobile);

        // mock SmsTemplateService 的方法
        SmsTemplateDO template = randomPojo(SmsTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        when(smsTemplateService.getSmsTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String content = randomString();
        when(smsTemplateService.formatSmsTemplateContent(eq(template.getContent()), eq(templateParams)))
                .thenReturn(content);
        // mock SmsChannelService 的方法
        SmsChannelDO smsChannel = randomPojo(SmsChannelDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(smsChannelService.getSmsChannel(eq(template.getChannelId()))).thenReturn(smsChannel);
        // mock SmsLogService 的方法
        Long smsLogId = randomLongId();
        when(smsLogService.createSmsLog(eq(mobile), eq(userId), eq(UserTypeEnum.MEMBER.getValue()), eq(Boolean.TRUE), eq(template),
                eq(content), eq(templateParams))).thenReturn(smsLogId);

        // 调用
        Long resultSmsLogId = smsSendService.sendSingleSmsToMember(null, userId, templateCode, templateParams);
        // 断言
        assertEquals(smsLogId, resultSmsLogId);
        // 断言调用
        verify(smsProducer).sendSmsSendMessage(eq(smsLogId), eq(mobile),
                eq(template.getChannelId()), eq(template.getApiTemplateId()),
                eq(Lists.newArrayList(new KeyValue<>("code", "1234"), new KeyValue<>("op", "login"))));
    }

    /**
     * 发送成功，当短信模板开启时
     */
    @Test
    public void testSendSingleSms_successWhenSmsTemplateEnable() {
        // 准备参数
        String mobile = randomString();
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        // mock SmsTemplateService 的方法
        SmsTemplateDO template = randomPojo(SmsTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        when(smsTemplateService.getSmsTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String content = randomString();
        when(smsTemplateService.formatSmsTemplateContent(eq(template.getContent()), eq(templateParams)))
                .thenReturn(content);
        // mock SmsChannelService 的方法
        SmsChannelDO smsChannel = randomPojo(SmsChannelDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(smsChannelService.getSmsChannel(eq(template.getChannelId()))).thenReturn(smsChannel);
        // mock SmsLogService 的方法
        Long smsLogId = randomLongId();
        when(smsLogService.createSmsLog(eq(mobile), eq(userId), eq(userType), eq(Boolean.TRUE), eq(template),
                eq(content), eq(templateParams))).thenReturn(smsLogId);

        // 调用
        Long resultSmsLogId = smsSendService.sendSingleSms(mobile, userId, userType, templateCode, templateParams);
        // 断言
        assertEquals(smsLogId, resultSmsLogId);
        // 断言调用
        verify(smsProducer).sendSmsSendMessage(eq(smsLogId), eq(mobile),
                eq(template.getChannelId()), eq(template.getApiTemplateId()),
                eq(Lists.newArrayList(new KeyValue<>("code", "1234"), new KeyValue<>("op", "login"))));
    }

    /**
     * 发送成功，当短信模板关闭时
     */
    @Test
    public void testSendSingleSms_successWhenSmsTemplateDisable() {
        // 准备参数
        String mobile = randomString();
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        // mock SmsTemplateService 的方法
        SmsTemplateDO template = randomPojo(SmsTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.DISABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        when(smsTemplateService.getSmsTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String content = randomString();
        when(smsTemplateService.formatSmsTemplateContent(eq(template.getContent()), eq(templateParams)))
                .thenReturn(content);
        // mock SmsChannelService 的方法
        SmsChannelDO smsChannel = randomPojo(SmsChannelDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(smsChannelService.getSmsChannel(eq(template.getChannelId()))).thenReturn(smsChannel);
        // mock SmsLogService 的方法
        Long smsLogId = randomLongId();
        when(smsLogService.createSmsLog(eq(mobile), eq(userId), eq(userType), eq(Boolean.FALSE), eq(template),
                eq(content), eq(templateParams))).thenReturn(smsLogId);

        // 调用
        Long resultSmsLogId = smsSendService.sendSingleSms(mobile, userId, userType, templateCode, templateParams);
        // 断言
        assertEquals(smsLogId, resultSmsLogId);
        // 断言调用
        verify(smsProducer, times(0)).sendSmsSendMessage(anyLong(), anyString(),
                anyLong(), any(), anyList());
    }

    @Test
    public void testCheckSmsTemplateValid_notExists() {
        // 准备参数
        String templateCode = randomString();
        // mock 方法

        // 调用，并断言异常
        assertServiceException(() -> smsSendService.validateSmsTemplate(templateCode),
                SMS_SEND_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testBuildTemplateParams_paramMiss() {
        // 准备参数
        SmsTemplateDO template = randomPojo(SmsTemplateDO.class,
                o -> o.setParams(Lists.newArrayList("code")));
        Map<String, Object> templateParams = new HashMap<>();
        // mock 方法

        // 调用，并断言异常
        assertServiceException(() -> smsSendService.buildTemplateParams(template, templateParams),
                SMS_SEND_MOBILE_TEMPLATE_PARAM_MISS, "code");
    }

    @Test
    public void testCheckMobile_notExists() {
        // 准备参数
        // mock 方法

        // 调用，并断言异常
        assertServiceException(() -> smsSendService.validateMobile(null),
                SMS_SEND_MOBILE_NOT_EXISTS);
    }

    @Test
    public void testSendBatchNotify() {
        // 准备参数
        // mock 方法

        // 调用
        UnsupportedOperationException exception = Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> smsSendService.sendBatchSms(null, null, null, null, null)
        );
        // 断言
        assertEquals("暂时不支持该操作，感兴趣可以实现该功能哟！", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDoSendSms() throws Throwable {
        // 准备参数
        SmsSendMessage message = randomPojo(SmsSendMessage.class);
        // mock SmsClientFactory 的方法
        SmsClient smsClient = spy(SmsClient.class);
        when(smsChannelService.getSmsClient(eq(message.getChannelId()))).thenReturn(smsClient);
        // mock SmsClient 的方法
        SmsSendRespDTO sendResult = randomPojo(SmsSendRespDTO.class);
        when(smsClient.sendSms(eq(message.getLogId()), eq(message.getMobile()), eq(message.getApiTemplateId()),
                eq(message.getTemplateParams()))).thenReturn(sendResult);

        // 调用
        smsSendService.doSendSms(message);
        // 断言
        verify(smsLogService).updateSmsSendResult(eq(message.getLogId()),
                eq(sendResult.getSuccess()), eq(sendResult.getApiCode()),
                eq(sendResult.getApiMsg()), eq(sendResult.getApiRequestId()), eq(sendResult.getSerialNo()));
    }

    @Test
    public void testReceiveSmsStatus() throws Throwable {
        // 准备参数
        String channelCode = randomString();
        String text = randomString();
        // mock SmsClientFactory 的方法
        SmsClient smsClient = spy(SmsClient.class);
        when(smsChannelService.getSmsClient(eq(channelCode))).thenReturn(smsClient);
        // mock SmsClient 的方法
        List<SmsReceiveRespDTO> receiveResults = randomPojoList(SmsReceiveRespDTO.class);

        // 调用
        smsSendService.receiveSmsStatus(channelCode, text);
        // 断言
        receiveResults.forEach(result -> smsLogService.updateSmsReceiveResult(eq(result.getLogId()), eq(result.getSuccess()),
                eq(result.getReceiveTime()), eq(result.getErrorCode()), eq(result.getErrorCode())));
    }

}
