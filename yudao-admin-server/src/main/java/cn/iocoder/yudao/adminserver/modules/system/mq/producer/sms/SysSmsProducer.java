package cn.iocoder.yudao.adminserver.modules.system.mq.producer.sms;

import cn.iocoder.yudao.adminserver.modules.system.mq.message.sms.SysSmsChannelRefreshMessage;
import cn.iocoder.yudao.adminserver.modules.system.mq.message.sms.SysSmsTemplateRefreshMessage;
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
public class SysSmsProducer {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送 {@link SysSmsChannelRefreshMessage} 消息
     */
    public void sendSmsChannelRefreshMessage() {
        SysSmsChannelRefreshMessage message = new SysSmsChannelRefreshMessage();
        redisMQTemplate.send(message);
    }

    /**
     * 发送 {@link SysSmsTemplateRefreshMessage} 消息
     */
    public void sendSmsTemplateRefreshMessage() {
        SysSmsTemplateRefreshMessage message = new SysSmsTemplateRefreshMessage();
        redisMQTemplate.send(message);
    }

}
