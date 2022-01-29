package cn.iocoder.yudao.module.system.mq.consumer.dict;

import cn.iocoder.yudao.framework.mq.core.pubsub.AbstractChannelMessageListener;
import cn.iocoder.yudao.module.system.mq.message.dict.DictDataRefreshMessage;
import cn.iocoder.yudao.module.system.service.dict.DictDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 针对 {@link DictDataRefreshMessage} 的消费者
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class DictDataRefreshConsumer extends AbstractChannelMessageListener<DictDataRefreshMessage> {

    @Resource
    private DictDataService dictDataService;

    @Override
    public void onMessage(DictDataRefreshMessage message) {
        log.info("[onMessage][收到 DictData 刷新消息]");
        dictDataService.initLocalCache();
    }

}
