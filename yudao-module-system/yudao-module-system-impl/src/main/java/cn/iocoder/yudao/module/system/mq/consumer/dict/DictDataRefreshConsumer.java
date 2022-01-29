package cn.iocoder.yudao.module.system.mq.consumer.dict;

import cn.iocoder.yudao.coreservice.modules.system.service.dict.SysDictDataCoreService;
import cn.iocoder.yudao.framework.mq.core.pubsub.AbstractChannelMessageListener;
import cn.iocoder.yudao.module.system.mq.message.dict.DictDataRefreshMessage;
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
    private SysDictDataCoreService dictDataCoreService;

    @Override
    public void onMessage(DictDataRefreshMessage message) {
        log.info("[onMessage][收到 DictData 刷新消息]");
        dictDataCoreService.initLocalCache();
    }

}
