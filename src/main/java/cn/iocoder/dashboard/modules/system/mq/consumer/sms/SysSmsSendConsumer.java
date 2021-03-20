package cn.iocoder.dashboard.modules.system.mq.consumer.sms;

import cn.iocoder.dashboard.framework.redis.core.stream.AbstractStreamMessageListener;
import cn.iocoder.dashboard.modules.system.mq.message.sms.SysSmsSendMessage;
import org.springframework.stereotype.Component;

@Component
public class SysSmsSendConsumer extends AbstractStreamMessageListener<SysSmsSendMessage> {

    @Override
    public void onMessage(SysSmsSendMessage message) {
        System.out.println(message);
    }

}
