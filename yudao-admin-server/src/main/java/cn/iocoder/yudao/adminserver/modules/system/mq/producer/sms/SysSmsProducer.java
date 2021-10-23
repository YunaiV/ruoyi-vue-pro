package cn.iocoder.yudao.adminserver.modules.system.mq.producer.sms;

import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.mq.core.util.RedisMessageUtils;
import cn.iocoder.yudao.adminserver.modules.system.mq.message.sms.SysSmsChannelRefreshMessage;
import cn.iocoder.yudao.adminserver.modules.system.mq.message.sms.SysSmsTemplateRefreshMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

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
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送 {@link SysSmsChannelRefreshMessage} 消息
     */
    public void sendSmsChannelRefreshMessage() {
        SysSmsChannelRefreshMessage message = new SysSmsChannelRefreshMessage();
        RedisMessageUtils.sendChannelMessage(stringRedisTemplate, message);
    }

    /**
     * 发送 {@link SysSmsTemplateRefreshMessage} 消息
     */
    public void sendSmsTemplateRefreshMessage() {
        SysSmsTemplateRefreshMessage message = new SysSmsTemplateRefreshMessage();
        RedisMessageUtils.sendChannelMessage(stringRedisTemplate, message);
    }

}
