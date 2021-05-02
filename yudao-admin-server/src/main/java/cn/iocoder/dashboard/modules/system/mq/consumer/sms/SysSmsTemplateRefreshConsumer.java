package cn.iocoder.dashboard.modules.system.mq.consumer.sms;

import cn.iocoder.yudao.framework.redis.core.pubsub.AbstractChannelMessageListener;
import cn.iocoder.dashboard.modules.system.mq.message.sms.SysSmsTemplateRefreshMessage;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 针对 {@link SysSmsTemplateRefreshMessage} 的消费者
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class SysSmsTemplateRefreshConsumer extends AbstractChannelMessageListener<SysSmsTemplateRefreshMessage> {

    @Resource
    private SysSmsTemplateService smsTemplateService;

    @Override
    public void onMessage(SysSmsTemplateRefreshMessage message) {
        log.info("[onMessage][收到 SmsTemplate 刷新消息]");
        smsTemplateService.initLocalCache();
    }

}
