package cn.iocoder.yudao.module.system.service.mail;

import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.mq.producer.mail.MailProducer;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MailSendServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private MailSendServiceImpl mailSendService;

    @Mock
    private MailAccountService mailAccountService;
    @Mock
    private MailTemplateService mailTemplateService;
    @Mock
    private MailLogService mailLogService;
    @Mock
    private MailProducer mailProducer;

    /**
     * 用于快速测试你的邮箱账号是否正常
     */
    @Test
    @Disabled
    public void testDemo() {
        MailAccount mailAccount = new MailAccount()
//                .setFrom("奥特曼 <ydym_test@163.com>")
                .setFrom("ydym_test@163.com") // 邮箱地址
                .setHost("smtp.163.com").setPort(465).setSslEnable(true) // SMTP 服务器
                .setAuth(true).setUser("ydym_test@163.com").setPass("WBZTEINMIFVRYSOE"); // 登录账号密码
        String messageId = MailUtil.send(mailAccount, "7685413@qq.com", "主题", "内容", false);
        System.out.println("发送结果：" + messageId);
    }

    /**
     * 发送成功，当短信模板开启时
     */
    @Test
    public void testSendSingleMail_successWhenMailTemplateEnable() {
        // 准备参数
        String mail = randomEmail();
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        // mock MailTemplateService 的方法
        MailTemplateDO template = randomPojo(MailTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        when(mailTemplateService.getMailTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String content = randomString();
        when(mailTemplateService.formatMailTemplateContent(eq(template.getContent()), eq(templateParams)))
                .thenReturn(content);
        // mock MailAccountService 的方法
        MailAccountDO account = randomPojo(MailAccountDO.class);
        when(mailAccountService.getMailAccountFromCache(eq(template.getAccountId()))).thenReturn(account);
        // mock MailLogService 的方法
        Long mailLogId = randomLongId();
        when(mailLogService.createMailLog(eq(userId), eq(userType), eq(mail),
                eq(account), eq(template), eq(content), eq(templateParams), eq(true))).thenReturn(mailLogId);

        // 调用
        Long resultMailLogId = mailSendService.sendSingleMail(mail, userId, userType, templateCode, templateParams);
        // 断言
        assertEquals(mailLogId, resultMailLogId);
        // 断言调用
        verify(mailProducer).sendMailSendMessage(eq(mailLogId), eq(mail),
                eq(account.getId()), eq(template.getNickname()), eq(template.getTitle()), eq(content));
    }

    /**
     * 发送成功，当短信模板关闭时
     */
    @Test
    public void testSendSingleMail_successWhenSmsTemplateDisable() {
        // 准备参数
        String mail = randomEmail();
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        // mock MailTemplateService 的方法
        MailTemplateDO template = randomPojo(MailTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.DISABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        when(mailTemplateService.getMailTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String content = randomString();
        when(mailTemplateService.formatMailTemplateContent(eq(template.getContent()), eq(templateParams)))
                .thenReturn(content);
        // mock MailAccountService 的方法
        MailAccountDO account = randomPojo(MailAccountDO.class);
        when(mailAccountService.getMailAccountFromCache(eq(template.getAccountId()))).thenReturn(account);
        // mock MailLogService 的方法
        Long mailLogId = randomLongId();
        when(mailLogService.createMailLog(eq(userId), eq(userType), eq(mail),
                eq(account), eq(template), eq(content), eq(templateParams), eq(false))).thenReturn(mailLogId);

        // 调用
        Long resultMailLogId = mailSendService.sendSingleMail(mail, userId, userType, templateCode, templateParams);
        // 断言
        assertEquals(mailLogId, resultMailLogId);
        // 断言调用
        verify(mailProducer, times(0)).sendMailSendMessage(anyLong(), anyString(),
                anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    public void testCheckMailTemplateValid_notExists() {
        // 准备参数
        String templateCode = randomString();
        // mock 方法

        // 调用，并断言异常
        assertServiceException(() -> mailSendService.checkMailTemplateValid(templateCode),
                MAIL_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testCheckTemplateParams_paramMiss() {
        // 准备参数
        MailTemplateDO template = randomPojo(MailTemplateDO.class,
                o -> o.setParams(Lists.newArrayList("code")));
        Map<String, Object> templateParams = new HashMap<>();
        // mock 方法

        // 调用，并断言异常
        assertServiceException(() -> mailSendService.checkTemplateParams(template, templateParams),
                MAIL_SEND_TEMPLATE_PARAM_MISS, "code");
    }

    @Test
    public void testCheckMail_notExists() {
        // 准备参数
        // mock 方法

        // 调用，并断言异常
        assertServiceException(() -> mailSendService.checkMail(null),
                MAIL_SEND_MAIL_NOT_EXISTS);
    }

}
