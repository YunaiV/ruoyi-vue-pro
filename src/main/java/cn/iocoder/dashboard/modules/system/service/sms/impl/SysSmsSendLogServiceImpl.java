package cn.iocoder.dashboard.modules.system.service.sms.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsSendLogDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsSendLogDOX;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.sms.SysSmsQueryLogMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.sms.SysSmsSendLogMapper;
import cn.iocoder.dashboard.modules.system.enums.sms.SysSmsSendStatusEnum;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsChannelService;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsSendLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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
    private SysSmsQueryLogMapper smsQueryLogMapper;

    @Resource
    private SysSmsSendLogMapper smsSendLogMapper;

    @Resource
    private SysSmsChannelService smsChannelService;

    /**
     * 定时执行 {@link #getSmsSendResultJob()} 的周期
     */
    private static final long SCHEDULER_PERIOD = 5 * 60 * 1000L;


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
    public void getAndSaveSmsSendLog() {

        List<SysSmsSendLogDO> noResultQueryLogList = smsQueryLogMapper.selectNoResultQueryLogList();

        if (CollectionUtil.isEmpty(noResultQueryLogList)) {
            return;
        }
        //用于添加的发送日志对象
        SysSmsSendLogDOX insertSendLog = new SysSmsSendLogDOX();
        //用于修改状态的请求日志对象
        SysSmsSendLogDO updateQueryLog = new SysSmsSendLogDO();

        noResultQueryLogList.forEach(queryLog -> {
            AbstractSmsClient smsClient = smsChannelService.getSmsClient(queryLog.getTemplateCode());

            updateQueryLog.setId(queryLog.getId());

            // 只处理实现了获取发送结果方法的短信客户端，理论上这里都是满足条件的，以防万一加个判断。
            /*if (smsClient instanceof NeedQuerySendResultSmsClient) {
                //初始化点字段值
                queryLog2SendLong(insertSendLog, queryLog);

                NeedQuerySendResultSmsClient querySendResultSmsClient = (NeedQuerySendResultSmsClient) smsClient;
                try {
                    List<SmsResultDetail> smsSendResult = querySendResultSmsClient.getSmsSendResult(queryLog.getRemark());
                    smsSendResult.forEach(resultDetail -> {
                        insertSendLog.setPhone(resultDetail.getPhone());
                        insertSendLog.setSendStatus(resultDetail.getSendStatus());
                        insertSendLog.setSendTime(resultDetail.getSendTime());
                        insertSendLog.setRemark(resultDetail.getMessage());
                        smsSendLogMapper.insert(insertSendLog);
                    });
                } catch (Exception e) {
                    //exception handle
                    log.error("query send result fail, exception: " + e.getMessage());

                    updateQueryLog.setSendStatus(SmsSendStatusEnum.QUERY_SEND_FAIL.getStatus());
                    updateQueryLog.setRemark(e.getMessage());
                    smsQueryLogMapper.updateById(updateQueryLog);
                    return;
                }
            } else {
                //理论上这里都是满足条件的，以防万一加个判断。
                updateQueryLog.setSendStatus(SmsSendStatusEnum.QUERY_SEND_FAIL.getStatus());
                smsQueryLogMapper.updateById(updateQueryLog);
            }*/
            updateQueryLog.setSendStatus(SysSmsSendStatusEnum.SEND_SUCCESS.getStatus());
            updateQueryLog.setRemark(String.format("日志(id = %s)对应的客户端没有继承NeedQuerySendResultSmsClient, 不能获取短信结果。", queryLog.getId()));
            smsQueryLogMapper.updateById(updateQueryLog);
        });
    }

    private void queryLog2SendLong(SysSmsSendLogDOX insertSendLog, SysSmsSendLogDO queryLog) {
        insertSendLog.setChannelCode(queryLog.getChannelCode());
        insertSendLog.setChannelId(queryLog.getChannelId());
        insertSendLog.setTemplateCode(queryLog.getTemplateCode());
    }

    @Scheduled(fixedDelay = SCHEDULER_PERIOD, initialDelay = SCHEDULER_PERIOD)
    public void getSmsSendResultJob() {
        getAndSaveSmsSendLog();
    }
}
