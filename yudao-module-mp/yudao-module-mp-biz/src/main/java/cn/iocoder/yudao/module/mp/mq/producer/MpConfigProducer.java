package cn.iocoder.yudao.module.mp.mq.producer;

import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import cn.iocoder.yudao.module.mp.mq.message.MpConfigRefreshMessage;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 微信配置数据相关消息的 Producer
 */
@Component
public class MpConfigProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link MpConfigRefreshMessage} 消息
     */
    public void sendDictDataRefreshMessage() {
        MpConfigRefreshMessage message = new MpConfigRefreshMessage();
        redisMQTemplate.send(message);
    }

}
