package cn.iocoder.dashboard.modules.system.redis.stream.sms;

import cn.iocoder.dashboard.framework.redis.core.util.RedisStreamUtils;
import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 短信发送流消息监听器
 *
 * @author zzf
 * @date 2021/3/9 16:35
 */
@Slf4j
@Component
public class SmsSendStreamProducer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送 {@link SmsSendMessage} 消息
     */
    public void sendSmsSendMessage(SmsBody smsBody, String targetPhone) {
        SmsSendMessage message = new SmsSendMessage();
        message.setSmsBody(smsBody);
        message.setTargetPhone(targetPhone);

        RedisStreamUtils.sendChannelMessage(stringRedisTemplate, message);
    }

}