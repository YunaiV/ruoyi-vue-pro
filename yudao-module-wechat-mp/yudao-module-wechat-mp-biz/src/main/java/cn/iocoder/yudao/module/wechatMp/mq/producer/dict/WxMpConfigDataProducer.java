package cn.iocoder.yudao.module.wechatMp.mq.producer.dict;

import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import cn.iocoder.yudao.module.wechatMp.mq.message.dict.WxConfigDataRefreshMessage;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 微信配置数据相关消息的 Producer
 */
@Component
public class WxMpConfigDataProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link WxConfigDataRefreshMessage} 消息
     */
    public void sendDictDataRefreshMessage() {
        WxConfigDataRefreshMessage message = new WxConfigDataRefreshMessage();
        redisMQTemplate.send(message);
    }

}
