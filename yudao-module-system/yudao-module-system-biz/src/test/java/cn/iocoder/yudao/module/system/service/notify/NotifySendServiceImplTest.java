package cn.iocoder.yudao.module.system.service.notify;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyTemplateDO;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.NOTICE_NOT_FOUND;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.NOTIFY_SEND_TEMPLATE_PARAM_MISS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NotifySendServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private NotifySendServiceImpl notifySendService;

    @Mock
    private NotifyTemplateService notifyTemplateService;
    @Mock
    private NotifyMessageService notifyMessageService;

    @Test
    public void testSendSingleNotifyToAdmin() {
        // 准备参数
        Long userId = randomLongId();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        // mock NotifyTemplateService 的方法
        NotifyTemplateDO template = randomPojo(NotifyTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        when(notifyTemplateService.getNotifyTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String content = randomString();
        when(notifyTemplateService.formatNotifyTemplateContent(eq(template.getContent()), eq(templateParams)))
                .thenReturn(content);
        // mock NotifyMessageService 的方法
        Long messageId = randomLongId();
        when(notifyMessageService.createNotifyMessage(eq(userId), eq(UserTypeEnum.ADMIN.getValue()),
                eq(template), eq(content), eq(templateParams))).thenReturn(messageId);

        // 调用
        Long resultMessageId = notifySendService.sendSingleNotifyToAdmin(userId, templateCode, templateParams);
        // 断言
        assertEquals(messageId, resultMessageId);
    }

    @Test
    public void testSendSingleNotifyToMember() {
        // 准备参数
        Long userId = randomLongId();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        // mock NotifyTemplateService 的方法
        NotifyTemplateDO template = randomPojo(NotifyTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        when(notifyTemplateService.getNotifyTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String content = randomString();
        when(notifyTemplateService.formatNotifyTemplateContent(eq(template.getContent()), eq(templateParams)))
                .thenReturn(content);
        // mock NotifyMessageService 的方法
        Long messageId = randomLongId();
        when(notifyMessageService.createNotifyMessage(eq(userId), eq(UserTypeEnum.MEMBER.getValue()),
                eq(template), eq(content), eq(templateParams))).thenReturn(messageId);

        // 调用
        Long resultMessageId = notifySendService.sendSingleNotifyToMember(userId, templateCode, templateParams);
        // 断言
        assertEquals(messageId, resultMessageId);
    }

    /**
     * 发送成功，当短信模板开启时
     */
    @Test
    public void testSendSingleNotify_successWhenMailTemplateEnable() {
        // 准备参数
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        // mock NotifyTemplateService 的方法
        NotifyTemplateDO template = randomPojo(NotifyTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        when(notifyTemplateService.getNotifyTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String content = randomString();
        when(notifyTemplateService.formatNotifyTemplateContent(eq(template.getContent()), eq(templateParams)))
                .thenReturn(content);
        // mock NotifyMessageService 的方法
        Long messageId = randomLongId();
        when(notifyMessageService.createNotifyMessage(eq(userId), eq(userType),
                eq(template), eq(content), eq(templateParams))).thenReturn(messageId);

        // 调用
        Long resultMessageId = notifySendService.sendSingleNotify(userId, userType, templateCode, templateParams);
        // 断言
        assertEquals(messageId, resultMessageId);
    }

    /**
     * 发送成功，当短信模板关闭时
     */
    @Test
    public void testSendSingleMail_successWhenSmsTemplateDisable() {
        // 准备参数
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        // mock NotifyTemplateService 的方法
        NotifyTemplateDO template = randomPojo(NotifyTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.DISABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        when(notifyTemplateService.getNotifyTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);

        // 调用
        Long resultMessageId = notifySendService.sendSingleNotify(userId, userType, templateCode, templateParams);
        // 断言
        assertNull(resultMessageId);
        verify(notifyTemplateService, never()).formatNotifyTemplateContent(anyString(), anyMap());
        verify(notifyMessageService, never()).createNotifyMessage(anyLong(), anyInt(), any(), anyString(), anyMap());
    }

    @Test
    public void testCheckMailTemplateValid_notExists() {
        // 准备参数
        String templateCode = randomString();
        // mock 方法

        // 调用，并断言异常
        assertServiceException(() -> notifySendService.validateNotifyTemplate(templateCode),
                NOTICE_NOT_FOUND);
    }

    @Test
    public void testCheckTemplateParams_paramMiss() {
        // 准备参数
        NotifyTemplateDO template = randomPojo(NotifyTemplateDO.class,
                o -> o.setParams(Lists.newArrayList("code")));
        Map<String, Object> templateParams = new HashMap<>();
        // mock 方法

        // 调用，并断言异常
        assertServiceException(() -> notifySendService.validateTemplateParams(template, templateParams),
                NOTIFY_SEND_TEMPLATE_PARAM_MISS, "code");
    }


}
