package cn.iocoder.dashboard.modules.system.service.sms.impl;

import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsClientFactory;
import cn.iocoder.dashboard.framework.sms.core.SmsResultDetail;
import cn.iocoder.dashboard.modules.system.redis.stream.sms.SmsSendStreamProducer;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsChannelService;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsQueryLogService;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;

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
    private SysSmsQueryLogService logService;

    @Resource
    private SmsSendStreamProducer smsProducer;

    @Resource
    private SmsClientFactory smsClientFactory;

    @Override
    public void send(SmsBody smsBody, String targetPhone) {
        AbstractSmsClient client = channelService.getSmsClient(smsBody.getTemplateCode());
        logService.beforeSendLog(smsBody, targetPhone, client);
        smsProducer.sendSmsSendMessage(smsBody, targetPhone);
    }

    @Override
    public Object smsSendCallbackHandle(ServletRequest request) {
        SmsResultDetail smsResultDetail = smsClientFactory.getSmsResultDetailFromCallbackQuery(request);
        logService.updateSendLogByResultDetail(smsResultDetail);
        return smsResultDetail.getCallbackResponseBody();
    }

}
