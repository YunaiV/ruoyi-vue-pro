package cn.iocoder.yudao.module.system.mq.consumer.mail;

import cn.iocoder.yudao.framework.mq.core.stream.AbstractStreamMessageListener;
import cn.iocoder.yudao.module.system.mq.message.mail.SysMailSendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SysMailSendConsumer extends AbstractStreamMessageListener<SysMailSendMessage> {

    @Override
    public void onMessage(SysMailSendMessage message) {
        log.info("[onMessage][消息内容({})]", message);
    }

}
