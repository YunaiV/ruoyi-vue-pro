package cn.iocoder.dashboard.modules.system.mq.consumer.mail;

import cn.iocoder.dashboard.framework.redis.core.stream.AbstractStreamMessageListener;
import cn.iocoder.dashboard.modules.system.mq.message.mail.SysMailSendMessage;
import org.springframework.stereotype.Component;

@Component
public class SysMailSendConsumer extends AbstractStreamMessageListener<SysMailSendMessage> {

    @Override
    public void onMessage(SysMailSendMessage message) {

    }

}
