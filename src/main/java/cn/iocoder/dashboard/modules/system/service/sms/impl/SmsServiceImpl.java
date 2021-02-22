package cn.iocoder.dashboard.modules.system.service.sms.impl;

import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;
import cn.iocoder.dashboard.modules.system.mq.producer.sms.SmsProducer;
import cn.iocoder.dashboard.modules.system.service.sms.SmsChannelService;
import cn.iocoder.dashboard.modules.system.service.sms.SmsLogService;
import cn.iocoder.dashboard.modules.system.service.sms.SmsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 短信日志Service实现类
 *
 * @author zzf
 * @date 2021/1/25 9:25
 */
@Service
public class SmsServiceImpl implements SmsService {

    @Resource
    private SmsChannelService channelService;

    @Resource
    private SmsLogService smsLogService;

    @Resource
    private SmsProducer smsProducer;

    @Override
    public SmsResult<?> send(SmsBody smsBody, List<String> targetPhones) {
        AbstractSmsClient<?> client = channelService.getClient(smsBody.getTemplateCode());
        Long logId = smsLogService.beforeSendLog(smsBody, targetPhones, client, false);

        SmsResult<?> result = client.send(smsBody, targetPhones);

        smsLogService.afterSendLog(logId, result);

        return result;
    }

    // TODO FROM 芋艿 to ZZF：可能要讨论下，对于短信发送来说，貌似只提供异步发送即可。对于业务来说，一定不能依赖短信的发送结果。
    @Override
    public void sendAsync(SmsBody smsBody, List<String> targetPhones) {
        AbstractSmsClient<?> client = channelService.getClient(smsBody.getTemplateCode());
        smsLogService.beforeSendLog(smsBody, targetPhones, client, true);
        smsProducer.sendSmsSendMessage(smsBody, targetPhones);
    }
}
