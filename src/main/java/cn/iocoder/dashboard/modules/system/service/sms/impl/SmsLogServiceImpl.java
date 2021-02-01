package cn.iocoder.dashboard.modules.system.service.sms.impl;

import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.SmsChannelPropertyVO;
import cn.iocoder.dashboard.modules.system.controller.sms.vo.SmsTemplateVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.sms.SmsLogMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.sms.SmsLogDO;
import cn.iocoder.dashboard.modules.system.enums.sms.SmsSendStatusEnum;
import cn.iocoder.dashboard.modules.system.service.sms.SmsLogService;
import cn.iocoder.dashboard.util.json.JsonUtils;
import cn.iocoder.dashboard.util.string.StrUtils;
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
public class SmsLogServiceImpl implements SmsLogService {

    @Resource
    private SmsLogMapper smsLogMapper;

    @Override
    public Long beforeSendLog(SmsBody smsBody, List<String> targetPhones, AbstractSmsClient<?> client, Boolean isAsync) {
        SmsLogDO smsLog = new SmsLogDO();
        if (smsBody.getSmsLogId() != null) {
            smsLog.setId(smsBody.getSmsLogId());
            smsLog.setSendStatus(SmsSendStatusEnum.SENDING.getStatus());
            smsLogMapper.updateById(smsLog);
            return smsBody.getSmsLogId();
        } else {
            SmsChannelPropertyVO property = client.getProperty();
            SmsTemplateVO smsTemplate = property.getTemplateByTemplateCode(smsBody.getTemplateCode());

            smsLog.setChannelCode(property.getCode())
                    .setChannelId(property.getId())
                    .setTemplateCode(smsTemplate.getCode())
                    .setPhones(JsonUtils.toJsonString(targetPhones))
                    .setContent(StrUtils.replace(smsTemplate.getContent(), smsBody.getParams()));

            if (isAsync) {
                smsLog.setSendStatus(SmsSendStatusEnum.ASYNC.getStatus());
            } else {
                smsLog.setSendStatus(SmsSendStatusEnum.SENDING.getStatus());
            }
            smsLogMapper.insert(smsLog);
            return smsLog.getId();
        }
    }

    @Override
    public void afterSendLog(Long logId, SmsResult<?> result) {
        SmsLogDO smsLog = new SmsLogDO();
        smsLog.setId(logId);
        if (result.getSuccess()) {
            smsLog.setSendStatus(SmsSendStatusEnum.SUCCESS.getStatus());
        } else {
            smsLog.setSendStatus(SmsSendStatusEnum.FAIL.getStatus());
            smsLog.setRemark(result.getMessage() + JsonUtils.toJsonString(result.getResult()));
        }
        smsLogMapper.updateById(smsLog);
    }

}
