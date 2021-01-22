package cn.iocoder.dashboard.modules.system.mq.consumer;

import cn.iocoder.dashboard.framework.redis.core.listener.AbstractMessageListener;
import cn.iocoder.dashboard.modules.system.mq.message.permission.SysMenuRefreshMessage;
import org.springframework.data.redis.connection.Message;
import org.springframework.stereotype.Component;

@Component
public class SysMenuRefreshConsumer extends AbstractMessageListener {

    @Override
    public void onMessage(Message message, byte[] bytes) {
        System.out.println(message);
    }

    @Override
    public String getChannel() {
        return SysMenuRefreshMessage.TOPIC;
    }

}
