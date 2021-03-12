package cn.iocoder.dashboard.modules.system.redis.stream.sms;

import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsChannelService;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsQueryLogService;
import cn.iocoder.dashboard.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
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
public class SmsSendStreamConsumer implements StreamListener<String, ObjectRecord<String, SmsSendMessage>> {

    @Resource
    private SysSmsChannelService smsChannelService;

    @Resource
    private SysSmsQueryLogService smsQueryLogService;

    @Override
    public void onMessage(ObjectRecord<String, SmsSendMessage> record) {
        SmsSendMessage message = record.getValue();
        SmsBody body = message.getSmsBody();
        log.info("[onMessage][收到 发送短信 消息], content: " + JsonUtils.toJsonString(body));
        AbstractSmsClient smsClient = smsChannelService.getSmsClient(body.getTemplateCode());
        String templateApiId = smsChannelService.getSmsTemplateApiIdByCode(body.getTemplateCode());

        SmsResult result = smsClient.send(templateApiId, body, message.getTargetPhone());
        smsQueryLogService.afterSendLog(body.getSmsLogId(), result);
    }
}
