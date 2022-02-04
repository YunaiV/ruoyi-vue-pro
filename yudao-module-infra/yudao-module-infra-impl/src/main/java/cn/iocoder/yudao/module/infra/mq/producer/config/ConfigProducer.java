package cn.iocoder.yudao.module.infra.mq.producer.config;

import cn.iocoder.yudao.module.infra.mq.message.config.ConfigRefreshMessage;
import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Config 配置相关消息的 Producer
 */
@Component
public class ConfigProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link ConfigRefreshMessage} 消息
     */
    public void sendConfigRefreshMessage() {
        ConfigRefreshMessage message = new ConfigRefreshMessage();
        redisMQTemplate.send(message);
    }

}
