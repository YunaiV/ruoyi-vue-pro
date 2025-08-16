package cn.iocoder.yudao.module.system.mq.producer.mail;

import cn.iocoder.yudao.module.system.mq.message.mail.MailSendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;

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
     * @param sendLogId 发送日志编码
     * @param toMails   接收邮件地址
     * @param ccMails   抄送邮件地址
     * @param bccMails  密送邮件地址
     * @param accountId 邮件账号编号
     * @param nickname  邮件发件人
     * @param title     邮件标题
     * @param content   邮件内容
     */
    public void sendMailSendMessage(Long sendLogId,
                                    Collection<String> toMails, Collection<String> ccMails, Collection<String> bccMails,
                                    Long accountId, String nickname, String title, String content) {
        MailSendMessage message = new MailSendMessage()
                .setLogId(sendLogId)
                .setToMails(toMails).setCcMails(ccMails).setBccMails(bccMails)
                .setAccountId(accountId).setNickname(nickname)
                .setTitle(title).setContent(content);
        applicationContext.publishEvent(message);
    }

}
