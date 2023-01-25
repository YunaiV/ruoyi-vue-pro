package cn.iocoder.yudao.module.system.service.mail;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.system.convert.mail.MailAccountConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.dal.dataobject.sms.SmsChannelDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailAccountMapper;
import cn.iocoder.yudao.module.system.mq.message.mail.MailSendMessage;
import cn.iocoder.yudao.module.system.mq.producer.mail.MailProducer;
import cn.iocoder.yudao.module.system.service.mail.MailLogService;
import cn.iocoder.yudao.module.system.service.mail.MailSendService;
import cn.iocoder.yudao.module.system.service.mail.MailTemplateService;
import cn.iocoder.yudao.module.system.service.member.MemberService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;

/**
 * 邮箱模版 服务实现类
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Service
@Validated
@Slf4j
public class MailSendServiceImpl implements MailSendService {

    @Resource
    private AdminUserService adminUserService;
    @Resource
    private MemberService memberService;

    @Resource
    private MailAccountService mailAccountService;
    @Resource
    private MailTemplateService mailTemplateService;

    @Resource
    private MailLogService mailLogService;
    @Resource
    private MailProducer mailProducer;

    @Override
    public Long sendSingleMailToAdmin(String mail, Long userId,
                                      String templateCode, Map<String, Object> templateParams) {
        // 如果 mail 为空，则加载用户编号对应的邮箱
        if (StrUtil.isEmpty(mail)) {
            AdminUserDO user = adminUserService.getUser(userId);
            if (user != null) {
                mail = user.getEmail();
            }
        }
        // 执行发送
        return sendSingleMail(mail, userId, UserTypeEnum.ADMIN.getValue(), templateCode, templateParams);
    }

    @Override
    public Long sendSingleMailToMember(String mail, Long userId,
                                       String templateCode, Map<String, Object> templateParams) {
        // 如果 mail 为空，则加载用户编号对应的邮箱
        if (StrUtil.isEmpty(mail)) {
            mail = memberService.getMemberUserEmail(userId);
        }
        // 执行发送
        return sendSingleMail(mail, userId, UserTypeEnum.MEMBER.getValue(), templateCode, templateParams);
    }

    @Override
    public Long sendSingleMail(String mail, Long userId, Integer userType,
                               String templateCode, Map<String, Object> templateParams) {
        // 校验邮箱模版是否合法
        MailTemplateDO template = checkMailTemplateValid(templateCode);
        // 校验邮箱账号是否合法
        MailAccountDO account = checkMailAccountValid(template.getAccountId());

        // 校验邮箱是否存在
        mail = checkMail(mail);
        // 构建有序的模板参数。为什么放在这个位置，是提前保证模板参数的正确性，而不是到了插入发送日志
        List<KeyValue<String, Object>> newTemplateParams = buildTemplateParams(template, templateParams);

        // 创建发送日志。如果模板被禁用，则不发送短信，只记录日志
        Boolean isSend = CommonStatusEnum.ENABLE.getStatus().equals(template.getStatus());
        String content = mailTemplateService.formatMailTemplateContent(template.getContent(), templateParams);
        Long sendLogId = mailLogService.createMailLog(userId, userType, mail,
                account, template, content, templateParams, isSend); // TODO 芋艿：待测试
        // 发送 MQ 消息，异步执行发送短信
        if (isSend) {
            mailProducer.sendMailSendMessage(sendLogId, account, template, content, newTemplateParams, mail); // TODO 芋艿：待测试
        }
        return sendLogId;
    }

    @Override
    public void doSendMail(MailSendMessage message) {
        // TODO @wangjingyi：直接使用 hutool 发送，不要封装 mail client 哈，因为短信的客户端都是比较统一的 DONE
        //装载账号信息
        MailAccount account  = MailAccountConvert.INSTANCE.convertAccount(message);
        //发送邮件
        try {
            String messageId = MailUtil.send(account,message.getTo(),message.getTitle(),message.getContent(),false,null);
            mailLogService.updateMailSendResult(message.getLogId() , messageId);
        } catch (Exception e){
            mailLogService.updateFailMailSendResult(message.getLogId() , e.getMessage());
        }
    }

    @VisibleForTesting
    public MailTemplateDO checkMailTemplateValid(String templateCode) {
        // 获得邮件模板。考虑到效率，从缓存中获取
        MailTemplateDO template = mailTemplateService.getMailTemplateByCodeFromCache(templateCode);
        // 邮件模板不存在
        if (template == null) {
            throw exception(MAIL_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    @VisibleForTesting
    public MailAccountDO checkMailAccountValid(Long accountId) {
        // 获得邮箱账号。考虑到效率，从缓存中获取
        MailAccountDO account = mailAccountService.getMailAccountFromCache(accountId);
        // 邮箱账号不存在
        if (account == null) {
            throw exception(MAIL_ACCOUNT_NOT_EXISTS);
        }
        return account;
    }

    @VisibleForTesting
    public String checkMail(String mail) {
        if (StrUtil.isEmpty(mail)) {
            throw exception(MAIL_SEND_MAIL_NOT_EXISTS);
        }
        return mail;
    }

    /**
     * 将参数模板，处理成有序的 KeyValue 数组
     *
     * @param template 邮箱模板
     * @param templateParams 原始参数
     * @return 处理后的参数
     */
    @VisibleForTesting
    public List<KeyValue<String, Object>> buildTemplateParams(MailTemplateDO template, Map<String, Object> templateParams) {
        return template.getParams().stream().map(key -> {
            Object value = templateParams.get(key);
            if (value == null) {
                throw exception(MAIL_SEND_TEMPLATE_PARAM_MISS, key);
            }
            return new KeyValue<>(key, value);
        }).collect(Collectors.toList());
    }
}
