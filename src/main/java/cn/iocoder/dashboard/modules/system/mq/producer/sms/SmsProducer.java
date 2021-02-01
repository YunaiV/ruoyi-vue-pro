package cn.iocoder.dashboard.modules.system.mq.producer.sms;

import cn.iocoder.dashboard.framework.redis.core.util.RedisMessageUtils;
import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.modules.system.mq.message.sms.SmsSendMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 短信的 Producer
 */
@Component
public class SmsProducer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送 {@link SmsSendMessage} 消息
     */
    public void sendSmsSendMessage(SmsBody smsBody, List<String> targetPhoneList) {
        SmsSendMessage message = new SmsSendMessage();
        message.setSmsBody(smsBody);
        message.setTargetPhones(targetPhoneList);
        RedisMessageUtils.sendChannelMessage(stringRedisTemplate, message);
    }

}
