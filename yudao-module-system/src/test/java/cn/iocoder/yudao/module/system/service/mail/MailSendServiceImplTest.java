package cn.iocoder.yudao.module.system.service.mail;

import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.mail.*;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.mq.message.mail.MailSendMessage;
import cn.iocoder.yudao.module.system.mq.producer.mail.MailProducer;
import cn.iocoder.yudao.module.system.service.member.MemberService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.HashMap;
import java.util.Map;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class MailSendServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private MailSendServiceImpl mailSendService;

    @Mock
    private AdminUserService adminUserService;
    @Mock
    private MemberService memberService;
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

    @Test
    public void testSendSingleMailToAdmin() {
        // 准备参数
        Long userId = randomLongId();
        String templateCode = RandomUtils.randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        // mock adminUserService 的方法
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setMobile("15601691300"));
        when(adminUserService.getUser(eq(userId))).thenReturn(user);

        // mock MailTemplateService 的方法
        MailTemplateDO template = randomPojo(MailTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        when(mailTemplateService.getMailTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String title = RandomUtils.randomString();
        when(mailTemplateService.formatMailTemplateContent(eq(template.getTitle()), eq(templateParams)))
                .thenReturn(title);
        String content = RandomUtils.randomString();
        when(mailTemplateService.formatMailTemplateContent(eq(template.getContent()), eq(templateParams)))
                .thenReturn(content);
        // mock MailAccountService 的方法
        MailAccountDO account = randomPojo(MailAccountDO.class);
        when(mailAccountService.getMailAccountFromCache(eq(template.getAccountId()))).thenReturn(account);
        // mock MailLogService 的方法
        Long mailLogId = randomLongId();
        when(mailLogService.createMailLog(eq(userId), eq(UserTypeEnum.ADMIN.getValue()), eq(user.getEmail()),
                eq(account), eq(template), eq(content), eq(templateParams), eq(true))).thenReturn(mailLogId);

        // 调用
        Long resultMailLogId = mailSendService.sendSingleMailToAdmin(null, userId, templateCode, templateParams);
        // 断言
        assertEquals(mailLogId, resultMailLogId);
        // 断言调用
        verify(mailProducer).sendMailSendMessage(eq(mailLogId), eq(user.getEmail()),
                eq(account.getId()), eq(template.getNickname()), eq(title), eq(content));
    }

    @Test
    public void testSendSingleMailToMember() {
        // 准备参数
        Long userId = randomLongId();
        String templateCode = RandomUtils.randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        // mock memberService 的方法
        String mail = randomEmail();
        when(memberService.getMemberUserEmail(eq(userId))).thenReturn(mail);

        // mock MailTemplateService 的方法
        MailTemplateDO template = randomPojo(MailTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        when(mailTemplateService.getMailTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String title = RandomUtils.randomString();
        when(mailTemplateService.formatMailTemplateContent(eq(template.getTitle()), eq(templateParams)))
                .thenReturn(title);
        String content = RandomUtils.randomString();
        when(mailTemplateService.formatMailTemplateContent(eq(template.getContent()), eq(templateParams)))
                .thenReturn(content);
        // mock MailAccountService 的方法
        MailAccountDO account = randomPojo(MailAccountDO.class);
        when(mailAccountService.getMailAccountFromCache(eq(template.getAccountId()))).thenReturn(account);
        // mock MailLogService 的方法
        Long mailLogId = randomLongId();
        when(mailLogService.createMailLog(eq(userId), eq(UserTypeEnum.MEMBER.getValue()), eq(mail),
                eq(account), eq(template), eq(content), eq(templateParams), eq(true))).thenReturn(mailLogId);

        // 调用
        Long resultMailLogId = mailSendService.sendSingleMailToMember(null, userId, templateCode, templateParams);
        // 断言
        assertEquals(mailLogId, resultMailLogId);
        // 断言调用
        verify(mailProducer).sendMailSendMessage(eq(mailLogId), eq(mail),
                eq(account.getId()), eq(template.getNickname()), eq(title), eq(content));
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
        String templateCode = RandomUtils.randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        // mock MailTemplateService 的方法
        MailTemplateDO template = randomPojo(MailTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        when(mailTemplateService.getMailTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String title = RandomUtils.randomString();
        when(mailTemplateService.formatMailTemplateContent(eq(template.getTitle()), eq(templateParams)))
                .thenReturn(title);
        String content = RandomUtils.randomString();
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
                eq(account.getId()), eq(template.getNickname()), eq(title), eq(content));
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
        String templateCode = RandomUtils.randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        // mock MailTemplateService 的方法
        MailTemplateDO template = randomPojo(MailTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.DISABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        when(mailTemplateService.getMailTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String title = RandomUtils.randomString();
        when(mailTemplateService.formatMailTemplateContent(eq(template.getTitle()), eq(templateParams)))
                .thenReturn(title);
        String content = RandomUtils.randomString();
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
    public void testValidateMailTemplateValid_notExists() {
        // 准备参数
        String templateCode = RandomUtils.randomString();
        // mock 方法

        // 调用，并断言异常
        assertServiceException(() -> mailSendService.validateMailTemplate(templateCode),
                MAIL_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testValidateTemplateParams_paramMiss() {
        // 准备参数
        MailTemplateDO template = randomPojo(MailTemplateDO.class,
                o -> o.setParams(Lists.newArrayList("code")));
        Map<String, Object> templateParams = new HashMap<>();
        // mock 方法

        // 调用，并断言异常
        assertServiceException(() -> mailSendService.validateTemplateParams(template, templateParams),
                MAIL_SEND_TEMPLATE_PARAM_MISS, "code");
    }

    @Test
    public void testValidateMail_notExists() {
        // 准备参数
        // mock 方法

        // 调用，并断言异常
        assertServiceException(() -> mailSendService.validateMail(null),
                MAIL_SEND_MAIL_NOT_EXISTS);
    }

    @Test
    public void testDoSendMail_success() {
        try (final MockedStatic<MailUtil> mailUtilMock = mockStatic(MailUtil.class)) {
            // 准备参数
            MailSendMessage message = randomPojo(MailSendMessage.class, o -> o.setNickname("芋艿"));
            // mock 方法（获得邮箱账号）
            MailAccountDO account = randomPojo(MailAccountDO.class, o -> o.setMail("7685@qq.com"));
            when(mailAccountService.getMailAccountFromCache(eq(message.getAccountId())))
                    .thenReturn(account);

            // mock 方法（发送邮件）
            String messageId = randomString();
            mailUtilMock.when(() -> MailUtil.send(
                    argThat(mailAccount -> {
                        assertEquals("芋艿 <7685@qq.com>", mailAccount.getFrom());
                        assertTrue(mailAccount.isAuth());
                        assertEquals(account.getUsername(), mailAccount.getUser());
                        assertEquals(account.getPassword(), mailAccount.getPass());
                        assertEquals(account.getHost(), mailAccount.getHost());
                        assertEquals(account.getPort(), mailAccount.getPort());
                        assertEquals(account.getSslEnable(), mailAccount.isSslEnable());
                        return true;
                    }), eq(message.getMail()), eq(message.getTitle()), eq(message.getContent()), eq(true)))
                    .thenReturn(messageId);

            // 调用
            mailSendService.doSendMail(message);
            // 断言
            verify(mailLogService).updateMailSendResult(eq(message.getLogId()), eq(messageId), isNull());
        }
    }

    @Test
    public void testDoSendMail_exception() {
        try (MockedStatic<MailUtil> mailUtilMock = mockStatic(MailUtil.class)) {
            // 准备参数
            MailSendMessage message = randomPojo(MailSendMessage.class, o -> o.setNickname("芋艿"));
            // mock 方法（获得邮箱账号）
            MailAccountDO account = randomPojo(MailAccountDO.class, o -> o.setMail("7685@qq.com"));
            when(mailAccountService.getMailAccountFromCache(eq(message.getAccountId())))
                    .thenReturn(account);

            // mock 方法（发送邮件）
            Exception e = new NullPointerException("啦啦啦");
            mailUtilMock.when(() -> MailUtil.send(argThat(mailAccount -> {
                assertEquals("芋艿 <7685@qq.com>", mailAccount.getFrom());
                assertTrue(mailAccount.isAuth());
                assertEquals(account.getUsername(), mailAccount.getUser());
                assertEquals(account.getPassword(), mailAccount.getPass());
                assertEquals(account.getHost(), mailAccount.getHost());
                assertEquals(account.getPort(), mailAccount.getPort());
                assertEquals(account.getSslEnable(), mailAccount.isSslEnable());
                return true;
            }), eq(message.getMail()), eq(message.getTitle()), eq(message.getContent()), eq(true))).thenThrow(e);

            // 调用
            mailSendService.doSendMail(message);
            // 断言
            verify(mailLogService).updateMailSendResult(eq(message.getLogId()), isNull(), same(e));
        }
    }

}
