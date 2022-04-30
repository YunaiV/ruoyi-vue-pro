package cn.iocoder.yudao.module.system.mq.producer.mail;

import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.mq.message.mail.MailSendMessage;
import cn.iocoder.yudao.module.system.mq.message.sms.SmsChannelRefreshMessage;
import cn.iocoder.yudao.module.system.mq.message.sms.SmsSendMessage;
import cn.iocoder.yudao.module.system.mq.message.sms.SmsTemplateRefreshMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Mail 邮件相关消息的 Producer
 *
 * @author wangjingyi
 * @date 2021/4/19 13:33
 */
@Slf4j
@Component
public class MailProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link SmsChannelRefreshMessage} 消息
     */
    public void sendMailChannelRefreshMessage() {
        SmsChannelRefreshMessage message = new SmsChannelRefreshMessage();
        redisMQTemplate.send(message);
    }

    /**
     * 发送 {@link SmsTemplateRefreshMessage} 消息
     */
    public void sendMailTemplateRefreshMessage() {
        SmsTemplateRefreshMessage message = new SmsTemplateRefreshMessage();
        redisMQTemplate.send(message);
    }

    /**
     * 发送 {@link MailSendMessage} 消息
     *
     * @param mailAccountDO 邮箱账号信息
     * @param mailTemplateDO 邮箱模版信息
     * @param content 内容
     * @param tos 收件人
     * @param title 标题
     */
    public void sendMailSendMessage(MailAccountDO mailAccountDO, MailTemplateDO mailTemplateDO, String content, List<String> tos, String title , Long sendLogId) {
        MailSendMessage message = new MailSendMessage();
        message.setContent(content);
        message.setFrom(mailAccountDO.getFrom());
        message.setTemplateCode(mailTemplateDO.getCode());
        message.setTitle(title);
        message.setTos(tos);
        message.setLogId(sendLogId);
        redisMQTemplate.send(message);
    }
}
