package cn.iocoder.dashboard.modules.system.mq.producer.sms;

import cn.iocoder.dashboard.framework.redis.core.util.RedisMessageUtils;
import cn.iocoder.dashboard.modules.system.mq.message.sms.SysSmsSendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

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
     * 发送短信 Message
     *
     * @param mobile 手机号
     * @param templateCode 短信模板编号
     * @param templateParams 短信模板参数
     * @param userId 用户编号
     * @param userType 用户类型
     */
    public void sendSmsSendMessage(String mobile, String templateCode, Map<String, Object> templateParams,
                                   Integer userId, Integer userType) {
        SysSmsSendMessage message = new SysSmsSendMessage();
        message.setMobile(mobile).setTemplateCode(templateCode).setTemplateParams(templateParams);
        message.setUserId(userId).setUserType(userType);
        RedisMessageUtils.sendStreamMessage(stringRedisTemplate, message);
    }

}
