package cn.iocoder.dashboard.modules.system.service.sms.impl;

import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperty;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.sms.SysSmsLogMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.sms.SysSmsLogDO;
import cn.iocoder.dashboard.modules.system.enums.sms.SmsSendStatusEnum;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsLogService;
import cn.iocoder.dashboard.util.json.JsonUtils;
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
public class SysSmsLogServiceImpl implements SysSmsLogService {

    @Resource
    private SysSmsLogMapper logMapper;

    @Override
    public Long beforeSendLog(SmsBody smsBody, List<String> targetPhones, AbstractSmsClient client, Boolean isAsync) {
        SysSmsLogDO smsLog = new SysSmsLogDO();
        if (smsBody.getSmsLogId() != null) {
            smsLog.setId(smsBody.getSmsLogId());
            smsLog.setSendStatus(SmsSendStatusEnum.SENDING.getStatus());
            logMapper.updateById(smsLog);
            return smsBody.getSmsLogId();
        } else {
            SmsChannelProperty property = client.getProperty();

            smsLog.setChannelCode(property.getCode())
                    .setChannelId(property.getId())
                    .setTemplateCode(smsBody.getTemplateCode())
                    .setPhones(JsonUtils.toJsonString(targetPhones))
                    .setContent(smsBody.getParams().toString());

            if (isAsync) {
                smsLog.setSendStatus(SmsSendStatusEnum.ASYNC.getStatus());
            } else {
                smsLog.setSendStatus(SmsSendStatusEnum.SENDING.getStatus());
            }
            logMapper.insert(smsLog);
            return smsLog.getId();
        }
    }

    @Override
    public void afterSendLog(Long logId, SmsResult result) {
        SysSmsLogDO smsLog = new SysSmsLogDO();
        smsLog.setId(logId);
        if (result.getSuccess()) {
            smsLog.setSendStatus(SmsSendStatusEnum.SUCCESS.getStatus());
            SysSmsLogDO smsLogDO = logMapper.selectById(logId);
            result.getResult().forEach(s -> {
                smsLogDO.setPhones(s.getPhone());
                smsLogDO.setSendStatus(s.getStatus());
                smsLogDO.setRemark(s.getMessage());
                smsLogDO.setCreateTime(s.getCreateTime());
                logMapper.insert(smsLogDO);
            });
        } else {
            smsLog.setSendStatus(SmsSendStatusEnum.FAIL.getStatus());
            smsLog.setRemark(result.getMessage() + JsonUtils.toJsonString(result.getResult()));
        }
        logMapper.updateById(smsLog);
    }

}
