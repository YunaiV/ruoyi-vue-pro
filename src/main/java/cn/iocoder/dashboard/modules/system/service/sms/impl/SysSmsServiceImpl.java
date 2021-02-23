package cn.iocoder.dashboard.modules.system.service.sms.impl;

import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;
import cn.iocoder.dashboard.modules.system.mq.producer.sms.SmsProducer;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsChannelService;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsLogService;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsService;
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
public class SysSmsServiceImpl implements SysSmsService {

    @Resource
    private SysSmsChannelService channelService;

    @Resource
    private SysSmsLogService logService;

    @Resource
    private SmsProducer smsProducer;

    @Override
    public SmsResult send(SmsBody smsBody, List<String> targetPhones) {
        AbstractSmsClient client = channelService.getSmsClient(smsBody.getTemplateCode());
        String templateApiId = channelService.getSmsTemplateApiIdByCode(smsBody.getTemplateCode());
        Long logId = logService.beforeSendLog(smsBody, targetPhones, client, false);

        SmsResult result = client.send(templateApiId, smsBody, targetPhones);

        logService.afterSendLog(logId, result);

        return result;
    }

    // TODO FROM 芋艿 to ZZF：可能要讨论下，对于短信发送来说，貌似只提供异步发送即可。对于业务来说，一定不能依赖短信的发送结果。
    //  我的想法是1、很多短信，比如验证码，总还是需要知道是否发送成功的。2、别人可以不用，我们不能没有。3、实现挺简单的，个人觉得无需纠结。
    @Override
    public void sendAsync(SmsBody smsBody, List<String> targetPhones) {
        AbstractSmsClient client = channelService.getSmsClient(smsBody.getTemplateCode());
        logService.beforeSendLog(smsBody, targetPhones, client, true);
        smsProducer.sendSmsSendMessage(smsBody, targetPhones);
    }
}
