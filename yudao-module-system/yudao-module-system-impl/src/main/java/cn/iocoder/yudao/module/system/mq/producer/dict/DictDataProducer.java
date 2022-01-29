package cn.iocoder.yudao.module.system.mq.producer.dict;

import cn.iocoder.yudao.module.system.mq.message.dict.DictDataRefreshMessage;
import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DictData 字典数据相关消息的 Producer
 */
@Component
public class DictDataProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link DictDataRefreshMessage} 消息
     */
    public void sendDictDataRefreshMessage() {
        DictDataRefreshMessage message = new DictDataRefreshMessage();
        redisMQTemplate.send(message);
    }

}
