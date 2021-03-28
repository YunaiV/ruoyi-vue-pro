package cn.iocoder.dashboard.modules.system.mq.consumer.sms;

import cn.iocoder.dashboard.framework.redis.core.stream.AbstractStreamMessageListener;
import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;
import cn.iocoder.dashboard.modules.system.mq.message.sms.SysSmsSendMessage;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsChannelService;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsQueryLogService;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsService;
import cn.iocoder.dashboard.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 针对 {@link SysSmsSendMessage} 的消费者
 *
 * @author zzf
 * @date 2021/3/9 16:35
 */
@Component
@Slf4j
public class SmsSendConsumer extends AbstractStreamMessageListener<SysSmsSendMessage> {

    @Resource
    private SysSmsChannelService smsChannelService;

    @Resource
    private SysSmsQueryLogService smsQueryLogService;

    @Resource
    private SysSmsService smsService;

    @Override
    public void onMessage(ObjectRecord<String, SmsSendMessage> record) {
        AbstractSmsClient smsClient = smsChannelService.getSmsClient(body.getTemplateCode());
        String templateApiId = smsChannelService.getSmsTemplateApiIdByCode(body.getTemplateCode());

        SmsResult result = smsClient.send(templateApiId, body, message.getTargetPhone());
        smsQueryLogService.afterSendLog(body.getSmsLogId(), result);
    }

    @Override
    public void onMessage(SysSmsSendMessage message) {
        log.info("[onMessage][消息内容({})]", message);
        smsService.doSendSms(message);
    }

}
