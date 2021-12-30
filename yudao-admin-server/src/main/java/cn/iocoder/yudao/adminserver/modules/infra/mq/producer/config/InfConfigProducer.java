package cn.iocoder.yudao.adminserver.modules.infra.mq.producer.config;

import cn.iocoder.yudao.adminserver.modules.infra.mq.message.config.InfConfigRefreshMessage;
import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Config 配置相关消息的 Producer
 */
@Component
public class InfConfigProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link InfConfigRefreshMessage} 消息
     */
    public void sendConfigRefreshMessage() {
        InfConfigRefreshMessage message = new InfConfigRefreshMessage();
        redisMQTemplate.send(message);
    }

}
