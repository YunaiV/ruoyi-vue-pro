package cn.iocoder.dashboard.modules.system.mq.consumer.sms;

import cn.iocoder.dashboard.framework.redis.core.pubsub.AbstractChannelMessageListener;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;
import cn.iocoder.dashboard.modules.system.mq.message.dept.SysDeptRefreshMessage;
import cn.iocoder.dashboard.modules.system.mq.message.sms.SmsSendMessage;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 针对 {@link SysDeptRefreshMessage} 的消费者
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class SmsSendConsumer extends AbstractChannelMessageListener<SmsSendMessage> {

    @Resource
    private SysSmsService sysSmsService;

    @Override
    public void onMessage(SmsSendMessage message) {
        log.info("[onMessage][收到 发送短信 消息], content: " +  message.toString());
        SmsResult send = sysSmsService.send(message.getSmsBody(), message.getTargetPhones());
    }

}
