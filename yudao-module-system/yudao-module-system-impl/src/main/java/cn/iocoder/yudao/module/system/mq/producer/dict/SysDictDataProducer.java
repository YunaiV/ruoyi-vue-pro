package cn.iocoder.yudao.module.system.mq.producer.dict;

import cn.iocoder.yudao.module.system.mq.message.dict.SysDictDataRefreshMessage;
import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DictData 字典数据相关消息的 Producer
 */
@Component
public class SysDictDataProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link SysDictDataRefreshMessage} 消息
     */
    public void sendDictDataRefreshMessage() {
        SysDictDataRefreshMessage message = new SysDictDataRefreshMessage();
        redisMQTemplate.send(message);
    }

}
