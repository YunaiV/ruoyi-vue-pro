package cn.iocoder.yudao.module.system.mq.consumer.mail;

import cn.iocoder.yudao.framework.mq.core.stream.AbstractStreamMessageListener;
import cn.iocoder.yudao.module.system.mq.message.mail.MailSendMessage;
import cn.iocoder.yudao.module.system.service.mail.MailSendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
@Slf4j
public class MailSendConsumer extends AbstractStreamMessageListener<MailSendMessage> {

    @Resource
    private MailSendService mailSendService;
    @Override
    public void onMessage(MailSendMessage message) {
        log.info("[onMessage][消息内容({})]", message);
        mailSendService.doSendMail(message);
    }

}
