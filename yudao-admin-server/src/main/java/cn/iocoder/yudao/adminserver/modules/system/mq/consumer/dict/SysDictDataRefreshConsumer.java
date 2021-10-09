package cn.iocoder.yudao.adminserver.modules.system.mq.consumer.dict;

import cn.iocoder.yudao.coreservice.modules.system.service.dict.SysDictDataCoreService;
import cn.iocoder.yudao.framework.mq.core.pubsub.AbstractChannelMessageListener;
import cn.iocoder.yudao.adminserver.modules.system.mq.message.dict.SysDictDataRefreshMessage;
import cn.iocoder.yudao.adminserver.modules.system.service.dict.SysDictDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 针对 {@link SysDictDataRefreshMessage} 的消费者
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class SysDictDataRefreshConsumer extends AbstractChannelMessageListener<SysDictDataRefreshMessage> {

    @Resource
    private SysDictDataCoreService dictDataCoreService;

    @Override
    public void onMessage(SysDictDataRefreshMessage message) {
        log.info("[onMessage][收到 DictData 刷新消息]");
        dictDataCoreService.initLocalCache();
    }

}
