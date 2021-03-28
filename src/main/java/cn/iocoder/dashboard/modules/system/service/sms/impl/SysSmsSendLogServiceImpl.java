package cn.iocoder.dashboard.modules.system.service.sms.impl;

import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsSendLogDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.sms.SysSmsSendLogMapper;
import cn.iocoder.dashboard.modules.system.enums.sms.SysSmsSendStatusEnum;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsSendLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * 短信发送日志服务实现类
 *
 * @author zzf
 * @date 2021/1/25 9:25
 */
@Slf4j
@Service
public class SysSmsSendLogServiceImpl implements SysSmsSendLogService {

    @Resource
    private SysSmsSendLogMapper smsSendLogMapper;

    @Override
    public Long createSmsSendLog(String mobile, Long userId, Integer userType,
                                 SysSmsTemplateDO template, String templateContent, Map<String, Object> templateParams) {
        SysSmsSendLogDO.SysSmsSendLogDOBuilder logBuilder = SysSmsSendLogDO.builder();
        // 设置手机相关字段
        logBuilder.mobile(mobile).userId(userId).userType(userType);
        // 设置模板相关字段
        logBuilder.templateId(template.getId()).templateCode(template.getCode()).templateType(template.getType());
        logBuilder.templateContent(templateContent).templateParams(templateParams).apiTemplateId(template.getApiTemplateId());
        // 设置渠道相关字段
        logBuilder.channelId(template.getChannelId()).channelCode(template.getChannelCode());

        // 插入数据库
        SysSmsSendLogDO logDO = logBuilder.build();
        smsSendLogMapper.insert(logDO);
        return logDO.getId();
    }

    @Override
    public void updateSmsSendLogResult(Long id, Boolean success, Integer sendFailureType, String sendFailureMsg,
                                       String apiSendFailureType, String apiSendFailureMsg, String apiRequestId, String apiSerialNo) {
        SysSmsSendStatusEnum sendStatus = Objects.equals(success, true) ? SysSmsSendStatusEnum.SUCCESS : SysSmsSendStatusEnum.FAILURE;
        smsSendLogMapper.updateById(new SysSmsSendLogDO().setId(id).setSendStatus(sendStatus.getStatus()).setSendTime(new Date())
                .setSendFailureType(sendFailureType).setSendFailureMsg(sendFailureMsg)
                .setApiSendFailureType(apiSendFailureType).setApiSendFailureMsg(apiSendFailureMsg).setApiRequestId(apiRequestId).setApiSerialNo(apiSerialNo));
    }

}
