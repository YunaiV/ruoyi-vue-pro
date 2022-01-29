package cn.iocoder.yudao.module.system.mq.producer.sms;

import cn.iocoder.yudao.module.system.mq.message.sms.SmsChannelRefreshMessage;
import cn.iocoder.yudao.module.system.mq.message.sms.SmsTemplateRefreshMessage;
import cn.iocoder.yudao.framework.mq.core.RedisMQTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Sms 短信相关消息的 Producer
 *
 * @author zzf
 * @date 2021/3/9 16:35
 */
@Slf4j
@Component
public class SmsProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link SmsChannelRefreshMessage} 消息
     */
    public void sendSmsChannelRefreshMessage() {
        SmsChannelRefreshMessage message = new SmsChannelRefreshMessage();
        redisMQTemplate.send(message);
    }

    /**
     * 发送 {@link SmsTemplateRefreshMessage} 消息
     */
    public void sendSmsTemplateRefreshMessage() {
        SmsTemplateRefreshMessage message = new SmsTemplateRefreshMessage();
        redisMQTemplate.send(message);
    }

}
