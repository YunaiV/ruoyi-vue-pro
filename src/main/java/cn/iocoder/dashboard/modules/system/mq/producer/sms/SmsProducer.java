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
        // TODO FROM 芋艿 TO ZZF：这块等未来改哈。这个方法目前是广播消费，会导致每个节点都发送一次。等后续封装出 redis stream 消息
        RedisMessageUtils.sendChannelMessage(stringRedisTemplate, message);
    }

}
