package cn.iocoder.yudao.module.iot.service.rule.scene.action;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.alert.IotAlertConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.alert.IotAlertReceiveTypeEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleActionTypeEnum;
import cn.iocoder.yudao.module.iot.service.alert.IotAlertConfigService;
import cn.iocoder.yudao.module.iot.service.alert.IotAlertRecordService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.system.api.mail.MailSendApi;
import cn.iocoder.yudao.module.system.api.mail.dto.MailSendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.sms.SmsSendApi;
import cn.iocoder.yudao.module.system.api.sms.dto.send.SmsSendSingleToUserReqDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link IotAlertTriggerSceneRuleAction} 的单元测试
 *
 * @author 芋道源码
 */
public class IotAlertTriggerSceneRuleActionTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IotAlertTriggerSceneRuleAction action;

    @Mock
    private IotAlertConfigService alertConfigService;
    @Mock
    private IotAlertRecordService alertRecordService;
    @Mock
    private IotDeviceService deviceService;

    @Mock
    private SmsSendApi smsSendApi;
    @Mock
    private MailSendApi mailSendApi;
    @Mock
    private NotifyMessageSendApi notifyMessageSendApi;

    @Test
    public void testGetType() {
        // 调用并断言
        assertEquals(IotSceneRuleActionTypeEnum.ALERT_TRIGGER, action.getType());
    }

    @Test
    public void testExecute_noAlertConfigs() throws Exception {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        IotSceneRuleDO rule = randomPojo(IotSceneRuleDO.class);
        IotSceneRuleDO.Action actionConfig = randomPojo(IotSceneRuleDO.Action.class);

        // mock 行为：返回空列表
        when(alertConfigService.getAlertConfigListBySceneRuleIdAndStatus(rule.getId(), CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(Collections.emptyList());

        // 调用
        action.execute(message, rule, actionConfig);

        // 断言：不查设备、不创建记录、不发消息
        verify(deviceService, never()).getDeviceFromCache(anyLong());
        verify(alertRecordService, never()).createAlertRecord(any(), any(), any(), any());
        verify(smsSendApi, never()).sendSingleSmsToAdmin(any());
        verify(mailSendApi, never()).sendSingleMailToAdmin(any());
        verify(notifyMessageSendApi, never()).sendSingleMessageToAdmin(any());
    }

    @Test
    public void testExecute_deviceTrigger_sendAllChannels() throws Exception {
        // 准备参数
        Long userId = randomLongId();
        IotDeviceMessage message = createDeviceMessage();
        IotSceneRuleDO rule = randomPojo(IotSceneRuleDO.class);
        IotSceneRuleDO.Action actionConfig = randomPojo(IotSceneRuleDO.Action.class);
        IotAlertConfigDO config = randomPojo(IotAlertConfigDO.class, c -> {
            c.setReceiveUserIds(Collections.singletonList(userId));
            c.setReceiveTypes(Arrays.asList(
                    IotAlertReceiveTypeEnum.SMS.getType(),
                    IotAlertReceiveTypeEnum.MAIL.getType(),
                    IotAlertReceiveTypeEnum.NOTIFY.getType()));
            c.setSmsTemplateCode("custom_sms");
            c.setMailTemplateCode("custom_mail");
            c.setNotifyTemplateCode("custom_notify");
        });
        IotDeviceDO device = randomPojo(IotDeviceDO.class);

        // mock 行为
        when(alertConfigService.getAlertConfigListBySceneRuleIdAndStatus(rule.getId(), CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(Collections.singletonList(config));
        when(deviceService.getDeviceFromCache(message.getDeviceId())).thenReturn(device);

        // 调用（mockStatic 需包住整个调用链；buildTemplateParams 内有 DictFrameworkUtils 静态调用）
        try (MockedStatic<DictFrameworkUtils> dictMock = mockStatic(DictFrameworkUtils.class)) {
            dictMock.when(() -> DictFrameworkUtils.parseDictDataLabel(any(), any(Integer.class)))
                    .thenReturn("WARN");
            action.execute(message, rule, actionConfig);
        }

        // 断言：设备只查一次
        verify(deviceService, times(1)).getDeviceFromCache(message.getDeviceId());
        // 断言：告警记录创建一次，参数透传
        verify(alertRecordService, times(1))
                .createAlertRecord(eq(config), eq(rule.getId()), eq(message), eq(device));
        // 断言：三条通道各发一次，模板编号匹配
        ArgumentCaptor<SmsSendSingleToUserReqDTO> smsCaptor = ArgumentCaptor.forClass(SmsSendSingleToUserReqDTO.class);
        verify(smsSendApi, times(1)).sendSingleSmsToAdmin(smsCaptor.capture());
        assertEquals(userId, smsCaptor.getValue().getUserId());
        assertEquals("custom_sms", smsCaptor.getValue().getTemplateCode());
        ArgumentCaptor<MailSendSingleToUserReqDTO> mailCaptor = ArgumentCaptor.forClass(MailSendSingleToUserReqDTO.class);
        verify(mailSendApi, times(1)).sendSingleMailToAdmin(mailCaptor.capture());
        assertEquals("custom_mail", mailCaptor.getValue().getTemplateCode());
        ArgumentCaptor<NotifySendSingleToUserReqDTO> notifyCaptor = ArgumentCaptor.forClass(NotifySendSingleToUserReqDTO.class);
        verify(notifyMessageSendApi, times(1)).sendSingleMessageToAdmin(notifyCaptor.capture());
        assertEquals("custom_notify", notifyCaptor.getValue().getTemplateCode());
    }

    @Test
    public void testExecute_timerTrigger_skipDeviceLookup() throws Exception {
        // 准备参数：定时触发，message 为 null
        Long userId = randomLongId();
        IotSceneRuleDO rule = randomPojo(IotSceneRuleDO.class);
        IotSceneRuleDO.Action actionConfig = randomPojo(IotSceneRuleDO.Action.class);
        IotAlertConfigDO config = randomPojo(IotAlertConfigDO.class, c -> {
            c.setReceiveUserIds(Collections.singletonList(userId));
            c.setReceiveTypes(Collections.singletonList(IotAlertReceiveTypeEnum.NOTIFY.getType()));
        });

        // mock 行为
        when(alertConfigService.getAlertConfigListBySceneRuleIdAndStatus(rule.getId(), CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(Collections.singletonList(config));

        // 调用
        try (MockedStatic<DictFrameworkUtils> dictMock = mockStatic(DictFrameworkUtils.class)) {
            dictMock.when(() -> DictFrameworkUtils.parseDictDataLabel(any(), any(Integer.class)))
                    .thenReturn("INFO");
            action.execute(null, rule, actionConfig);
        }

        // 断言：跳过设备查询；message 与 device 都用 null 创建告警记录
        verify(deviceService, never()).getDeviceFromCache(anyLong());
        verify(alertRecordService, times(1))
                .createAlertRecord(eq(config), eq(rule.getId()), eq(null), eq(null));
        verify(notifyMessageSendApi, times(1)).sendSingleMessageToAdmin(any(NotifySendSingleToUserReqDTO.class));
    }

    @Test
    public void testExecute_emptyReceiveUsers_skipSend() throws Exception {
        // 准备参数：接收用户为空
        IotDeviceMessage message = createDeviceMessage();
        IotSceneRuleDO rule = randomPojo(IotSceneRuleDO.class);
        IotSceneRuleDO.Action actionConfig = randomPojo(IotSceneRuleDO.Action.class);
        IotAlertConfigDO config = randomPojo(IotAlertConfigDO.class, c -> {
            c.setReceiveUserIds(Collections.emptyList());
            c.setReceiveTypes(Collections.singletonList(IotAlertReceiveTypeEnum.SMS.getType()));
        });
        IotDeviceDO device = randomPojo(IotDeviceDO.class);

        // mock 行为
        when(alertConfigService.getAlertConfigListBySceneRuleIdAndStatus(rule.getId(), CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(Collections.singletonList(config));
        when(deviceService.getDeviceFromCache(message.getDeviceId())).thenReturn(device);

        // 调用
        action.execute(message, rule, actionConfig);

        // 断言：告警记录仍然创建，但不发送任何消息
        verify(alertRecordService, times(1))
                .createAlertRecord(eq(config), eq(rule.getId()), eq(message), eq(device));
        verify(smsSendApi, never()).sendSingleSmsToAdmin(any());
    }

    @Test
    public void testExecute_unknownReceiveType_skipSend() throws Exception {
        // 准备参数：接收类型为未知值
        Long userId = randomLongId();
        IotDeviceMessage message = createDeviceMessage();
        IotSceneRuleDO rule = randomPojo(IotSceneRuleDO.class);
        IotSceneRuleDO.Action actionConfig = randomPojo(IotSceneRuleDO.Action.class);
        IotAlertConfigDO config = randomPojo(IotAlertConfigDO.class, c -> {
            c.setReceiveUserIds(Collections.singletonList(userId));
            c.setReceiveTypes(Collections.singletonList(99));
        });
        IotDeviceDO device = randomPojo(IotDeviceDO.class);

        // mock 行为
        when(alertConfigService.getAlertConfigListBySceneRuleIdAndStatus(rule.getId(), CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(Collections.singletonList(config));
        when(deviceService.getDeviceFromCache(message.getDeviceId())).thenReturn(device);

        // 调用
        try (MockedStatic<DictFrameworkUtils> dictMock = mockStatic(DictFrameworkUtils.class)) {
            dictMock.when(() -> DictFrameworkUtils.parseDictDataLabel(any(), any(Integer.class)))
                    .thenReturn("WARN");
            action.execute(message, rule, actionConfig);
        }

        // 断言：未知类型不发送
        verify(smsSendApi, never()).sendSingleSmsToAdmin(any());
        verify(mailSendApi, never()).sendSingleMailToAdmin(any());
        verify(notifyMessageSendApi, never()).sendSingleMessageToAdmin(any());
    }

    @Test
    public void testExecute_smsFailure_doesNotBlockOthers() throws Exception {
        // 准备参数
        Long userId = randomLongId();
        IotDeviceMessage message = createDeviceMessage();
        IotSceneRuleDO rule = randomPojo(IotSceneRuleDO.class);
        IotSceneRuleDO.Action actionConfig = randomPojo(IotSceneRuleDO.Action.class);
        IotAlertConfigDO config = randomPojo(IotAlertConfigDO.class, c -> {
            c.setReceiveUserIds(Collections.singletonList(userId));
            c.setReceiveTypes(Arrays.asList(
                    IotAlertReceiveTypeEnum.SMS.getType(),
                    IotAlertReceiveTypeEnum.MAIL.getType()));
        });
        IotDeviceDO device = randomPojo(IotDeviceDO.class);

        // mock 行为：sms 抛异常
        when(alertConfigService.getAlertConfigListBySceneRuleIdAndStatus(rule.getId(), CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(Collections.singletonList(config));
        when(deviceService.getDeviceFromCache(message.getDeviceId())).thenReturn(device);
        when(smsSendApi.sendSingleSmsToAdmin(any())).thenThrow(new RuntimeException("sms 渠道异常"));

        // 调用
        try (MockedStatic<DictFrameworkUtils> dictMock = mockStatic(DictFrameworkUtils.class)) {
            dictMock.when(() -> DictFrameworkUtils.parseDictDataLabel(any(), any(Integer.class)))
                    .thenReturn("ERROR");
            action.execute(message, rule, actionConfig);
        }

        // 断言：sms 抛错时邮件依旧发送
        verify(smsSendApi, times(1)).sendSingleSmsToAdmin(any());
        verify(mailSendApi, times(1)).sendSingleMailToAdmin(any());
    }

    /**
     * 创建带 reportTime 的设备消息
     */
    private IotDeviceMessage createDeviceMessage() {
        IotDeviceMessage message = new IotDeviceMessage();
        message.setId(randomString());
        message.setDeviceId(randomLongId());
        message.setReportTime(LocalDateTime.now());
        return message;
    }

}
