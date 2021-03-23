package cn.iocoder.dashboard.modules.system.service.sms.impl;

import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsResult;
import cn.iocoder.dashboard.framework.sms.core.SmsResultDetail;
import cn.iocoder.dashboard.framework.sms.core.property.SmsChannelProperty;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.sms.SysSmsQueryLogMapper;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsQueryLogDO;
import cn.iocoder.dashboard.modules.system.enums.sms.SmsSendStatusEnum;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsQueryLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 短信请求日志服务实现类
 *
 * @author zzf
 * @date 13:50 2021/3/2
 */
@Service
public class SysSmsQueryLogServiceImpl implements SysSmsQueryLogService {

    @Resource
    private SysSmsQueryLogMapper logMapper;

    @Override
    public void beforeSendLog(SmsBody smsBody, String targetPhone, AbstractSmsClient client) {
        SysSmsQueryLogDO smsLog = new SysSmsQueryLogDO();
        SmsChannelProperty property = client.getProperty();

        smsLog.setChannelCode(property.getCode())
                .setChannelId(property.getId())
                .setTemplateCode(smsBody.getTemplateCode())
                .setPhone(targetPhone)
                .setContent(smsBody.getParams().toString());

        smsLog.setSendStatus(SmsSendStatusEnum.ASYNC.getStatus());
        logMapper.insert(smsLog);
        smsBody.setSmsLogId(smsLog.getId());
    }

    @Override
    public void afterSendLog(Long logId, SmsResult result) {
        SysSmsQueryLogDO smsLog = new SysSmsQueryLogDO();
        smsLog.setId(logId);
        smsLog.setApiId(result.getApiId());
        smsLog.setSendStatus(SmsSendStatusEnum.QUERY_FAIL.getStatus());
        smsLog.setRemark(result.getCode() + ": " + result.getMessage());
        logMapper.updateById(smsLog);
    }

    @Override
    public void updateSendLogByResultDetail(SmsResultDetail smsResultDetail) {
        SysSmsQueryLogDO queryLogDO = new SysSmsQueryLogDO();
        queryLogDO.setSendStatus(smsResultDetail.getSendStatus());
        queryLogDO.setSendTime(smsResultDetail.getSendTime());
        queryLogDO.setRemark(smsResultDetail.getMessage());
        logMapper.updateByApiId(queryLogDO, smsResultDetail.getApiId());
    }

}
