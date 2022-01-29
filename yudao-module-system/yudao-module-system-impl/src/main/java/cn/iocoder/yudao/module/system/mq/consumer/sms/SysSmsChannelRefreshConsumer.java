package cn.iocoder.yudao.module.system.mq.consumer.sms;

import cn.iocoder.yudao.framework.mq.core.pubsub.AbstractChannelMessageListener;
import cn.iocoder.yudao.module.system.mq.message.sms.SysSmsChannelRefreshMessage;
import cn.iocoder.yudao.module.system.service.sms.SysSmsChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 针对 {@link SysSmsChannelRefreshMessage} 的消费者
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class SysSmsChannelRefreshConsumer extends AbstractChannelMessageListener<SysSmsChannelRefreshMessage> {

    @Resource
    private SysSmsChannelService smsChannelService;

    @Override
    public void onMessage(SysSmsChannelRefreshMessage message) {
        log.info("[onMessage][收到 SmsChannel 刷新消息]");
        smsChannelService.initSmsClients();
    }

}
