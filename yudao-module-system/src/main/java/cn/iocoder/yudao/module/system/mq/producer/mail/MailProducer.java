package cn.iocoder.yudao.module.system.mq.producer.mail;

import cn.iocoder.yudao.module.system.mq.message.mail.MailSendMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collection;

/**
 * Mail 邮件相关消息的 Producer
 *
 * @author wangjingyi
 * @since 2021/4/19 13:33
 */
@Slf4j
@Component
public class MailProducer {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 发送 {@link MailSendMessage} 消息
     *
     * @param sendLogId   发送日志编码
     * @param toMails     接收邮件地址
     * @param ccMails     抄送邮件地址
     * @param bccMails    密送邮件地址
     * @param accountId   邮件账号编号
     * @param nickname    邮件发件人
     * @param title       邮件标题
     * @param content     邮件内容
     * @param attachments 附件
     */
    public void sendMailSendMessage(Long sendLogId,
                                    Collection<String> toMails, Collection<String> ccMails, Collection<String> bccMails,
                                    Long accountId, String nickname, String title, String content,
                                    File[] attachments) {
        MailSendMessage message = new MailSendMessage()
                .setLogId(sendLogId)
                .setToMails(toMails).setCcMails(ccMails).setBccMails(bccMails)
                .setAccountId(accountId).setNickname(nickname)
                .setTitle(title).setContent(content).setAttachments(attachments);
        applicationContext.publishEvent(message);
    }

}
