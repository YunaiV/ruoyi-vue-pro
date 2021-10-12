package cn.iocoder.yudao.coreservice.modules.system.service.sms.impl;

import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.sms.SysSmsLogDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.mysql.sms.SysSmsLogCoreMapper;
import cn.iocoder.yudao.coreservice.modules.system.enums.sms.SysSmsReceiveStatusEnum;
import cn.iocoder.yudao.coreservice.modules.system.enums.sms.SysSmsSendStatusEnum;
import cn.iocoder.yudao.coreservice.modules.system.service.sms.SysSmsLogCoreService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * 短信日志 Core Service 实现类
 *
 * @author zzf
 * @date 2021/1/25 9:25
 */
@Slf4j
@Service
public class SysSmsLogCoreServiceImpl implements SysSmsLogCoreService {

    @Resource
    private SysSmsLogCoreMapper smsLogCoreMapper;

    @Override
    public Long createSmsLog(String mobile, Long userId, Integer userType, Boolean isSend,
                             SysSmsTemplateDO template, String templateContent, Map<String, Object> templateParams) {
        SysSmsLogDO.SysSmsLogDOBuilder logBuilder = SysSmsLogDO.builder();
        // 根据是否要发送，设置状态
        logBuilder.sendStatus(Objects.equals(isSend, true) ? SysSmsSendStatusEnum.INIT.getStatus()
                : SysSmsSendStatusEnum.IGNORE.getStatus());
        // 设置手机相关字段
        logBuilder.mobile(mobile).userId(userId).userType(userType);
        // 设置模板相关字段
        logBuilder.templateId(template.getId()).templateCode(template.getCode()).templateType(template.getType());
        logBuilder.templateContent(templateContent).templateParams(templateParams).apiTemplateId(template.getApiTemplateId());
        // 设置渠道相关字段
        logBuilder.channelId(template.getChannelId()).channelCode(template.getChannelCode());
        // 设置接收相关字段
        logBuilder.receiveStatus(SysSmsReceiveStatusEnum.INIT.getStatus());

        // 插入数据库
        SysSmsLogDO logDO = logBuilder.build();
        smsLogCoreMapper.insert(logDO);
        return logDO.getId();
    }

    @Override
    public void updateSmsSendResult(Long id, Integer sendCode, String sendMsg,
                                    String apiSendCode, String apiSendMsg, String apiRequestId, String apiSerialNo) {
        SysSmsSendStatusEnum sendStatus = CommonResult.isSuccess(sendCode) ? SysSmsSendStatusEnum.SUCCESS
                : SysSmsSendStatusEnum.FAILURE;
        smsLogCoreMapper.updateById(SysSmsLogDO.builder().id(id).sendStatus(sendStatus.getStatus()).sendTime(new Date())
                .sendCode(sendCode).sendMsg(sendMsg).apiSendCode(apiSendCode).apiSendMsg(apiSendMsg)
                .apiRequestId(apiRequestId).apiSerialNo(apiSerialNo).build());
    }

    @Override
    public void updateSmsReceiveResult(Long id, Boolean success, Date receiveTime, String apiReceiveCode, String apiReceiveMsg) {
        SysSmsReceiveStatusEnum receiveStatus = Objects.equals(success, true) ? SysSmsReceiveStatusEnum.SUCCESS
                : SysSmsReceiveStatusEnum.FAILURE;
        smsLogCoreMapper.updateById(SysSmsLogDO.builder().id(id).receiveStatus(receiveStatus.getStatus()).receiveTime(receiveTime)
                .apiReceiveCode(apiReceiveCode).apiReceiveMsg(apiReceiveMsg).build());
    }

}
