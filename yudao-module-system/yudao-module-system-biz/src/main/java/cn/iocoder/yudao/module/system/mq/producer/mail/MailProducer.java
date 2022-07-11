package cn.iocoder.yudao.module.system.mq.producer.mail;

import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.mq.message.mail.MailAccountRefreshMessage;
import cn.iocoder.yudao.module.system.mq.message.mail.MailSendMessage;
import cn.iocoder.yudao.module.system.mq.message.mail.MailTemplateRefreshMessage;
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
     * 发送 {@link MailTemplateRefreshMessage} 消息
     */
    public void sendMailTemplateRefreshMessage() {
        MailTemplateRefreshMessage message = new MailTemplateRefreshMessage();
        redisMQTemplate.send(message);
    }

    /**
     * 发送 {@link MailTemplateRefreshMessage} 消息
     */
    public void sendMailAccountRefreshMessage() {
        MailAccountRefreshMessage message = new MailAccountRefreshMessage();
        redisMQTemplate.send(message);
    }

    /**
     * 发送 {@link MailSendMessage} 消息
     *
     * @param sendLogId 发送日志编码
     * @param mailAccountDO 邮箱账号信息
     * @param mailTemplateDO 邮箱模版信息
     * @param content 内容
     * @param templateParams 邮箱模版参数
     * @param to 收件人
     */
    public void sendMailSendMessage(Long sendLogId,MailAccountDO mailAccountDO, MailTemplateDO mailTemplateDO, String content,List<KeyValue<String, Object>> templateParams,String to) {
        MailSendMessage message = new MailSendMessage();
        message.setContent(content)
        .setFromAddress(mailAccountDO.getFromAddress())
        .setHost(mailAccountDO.getHost())
        .setPort(mailAccountDO.getPort())
        .setPassword(mailAccountDO.getPassword())
        .setUsername(mailAccountDO.getUsername())
        .setSslEnable(mailAccountDO.getSslEnable())
        .setTemplateCode(mailTemplateDO.getCode())
        .setTitle(mailTemplateDO.getTitle())
        .setTo(to)
        .setLogId(sendLogId)
        .setTemplateParams(templateParams);
        redisMQTemplate.send(message);
    }
}
