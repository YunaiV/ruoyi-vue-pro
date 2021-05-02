package cn.iocoder.yudao.adminserver.modules.system.mq.producer.sms;

import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.redis.core.util.RedisMessageUtils;
import cn.iocoder.yudao.adminserver.modules.system.mq.message.sms.SysSmsChannelRefreshMessage;
import cn.iocoder.yudao.adminserver.modules.system.mq.message.sms.SysSmsSendMessage;
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
     * 发送 {@link SysSmsSendMessage} 消息
     *
     * @param logId 短信日志编号
     * @param mobile 手机号
     * @param channelId 渠道编号
     * @param apiTemplateId 短信模板编号
     * @param templateParams 短信模板参数
     */
    public void sendSmsSendMessage(Long logId, String mobile,
                                   Long channelId, String apiTemplateId, List<KeyValue<String, Object>> templateParams) {
        SysSmsSendMessage message = new SysSmsSendMessage().setLogId(logId).setMobile(mobile);
        message.setChannelId(channelId).setApiTemplateId(apiTemplateId).setTemplateParams(templateParams);
        RedisMessageUtils.sendStreamMessage(stringRedisTemplate, message);
    }

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
