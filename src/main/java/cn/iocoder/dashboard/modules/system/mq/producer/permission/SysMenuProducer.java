package cn.iocoder.dashboard.modules.system.mq.producer.permission;

import cn.iocoder.dashboard.modules.system.mq.message.permission.SysMenuRefreshMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Menu 菜单相关消息的 Producer
 */
@Component
public class SysMenuProducer {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 发送 {@link SysMenuRefreshMessage} 消息
     */
    public void sendMenuRefreshMessage() {
        SysMenuRefreshMessage message = new SysMenuRefreshMessage();
        redisTemplate.convertAndSend(SysMenuRefreshMessage.TOPIC, message);
    }

}
