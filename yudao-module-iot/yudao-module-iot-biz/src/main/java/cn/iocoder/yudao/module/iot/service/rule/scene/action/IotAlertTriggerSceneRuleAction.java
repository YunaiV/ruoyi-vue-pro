package cn.iocoder.yudao.module.iot.service.rule.scene.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.alert.IotAlertConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.DictTypeConstants;
import cn.iocoder.yudao.module.iot.enums.alert.IotAlertReceiveTypeEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleActionTypeEnum;
import cn.iocoder.yudao.module.iot.service.alert.IotAlertConfigService;
import cn.iocoder.yudao.module.iot.service.alert.IotAlertRecordService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.system.api.mail.MailSendApi;
import cn.iocoder.yudao.module.system.api.mail.dto.MailSendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.sms.SmsSendApi;
import cn.iocoder.yudao.module.system.api.sms.dto.send.SmsSendSingleToUserReqDTO;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IoT 告警触发的 {@link IotSceneRuleAction} 实现类
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class IotAlertTriggerSceneRuleAction implements IotSceneRuleAction {

    @Resource
    private IotAlertConfigService alertConfigService;
    @Resource
    private IotAlertRecordService alertRecordService;
    @Resource
    private IotDeviceService deviceService;

    @Resource
    private SmsSendApi smsSendApi;
    @Resource
    private MailSendApi mailSendApi;
    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Override
    public void execute(@Nullable IotDeviceMessage message,
                        IotSceneRuleDO rule, IotSceneRuleDO.Action actionConfig) throws Exception {
        List<IotAlertConfigDO> alertConfigs = alertConfigService.getAlertConfigListBySceneRuleIdAndStatus(
                rule.getId(), CommonStatusEnum.ENABLE.getStatus());
        if (CollUtil.isEmpty(alertConfigs)) {
            return;
        }
        // 获得设备信息
        IotDeviceDO device = message != null ? deviceService.getDeviceFromCache(message.getDeviceId()) : null;
        alertConfigs.forEach(alertConfig -> {
            // 创建告警记录
            alertRecordService.createAlertRecord(alertConfig, rule.getId(), message, device);
            // 发送告警消息
            sendAlertMessage(alertConfig, message, device);
        });
    }

    private void sendAlertMessage(IotAlertConfigDO config,
                                  @Nullable IotDeviceMessage deviceMessage,
                                  @Nullable IotDeviceDO device) {
        if (CollUtil.isEmpty(config.getReceiveUserIds()) || CollUtil.isEmpty(config.getReceiveTypes())) {
            return;
        }
        Map<String, Object> templateParams = buildTemplateParams(config, deviceMessage, device);
        config.getReceiveUserIds().forEach(userId ->
                config.getReceiveTypes().forEach(receiveType ->
                        sendAlertMessageToUser(userId, receiveType, config, templateParams)));
    }

    /**
     * 按指定接收方式，给单个用户发送告警消息
     */
    private void sendAlertMessageToUser(Long userId, Integer receiveType, IotAlertConfigDO config,
                                        Map<String, Object> templateParams) {
        IotAlertReceiveTypeEnum typeEnum = IotAlertReceiveTypeEnum.of(receiveType);
        if (typeEnum == null) {
            return;
        }
        String templateCode = resolveTemplateCode(config, typeEnum);
        if (StrUtil.isBlank(templateCode)) {//为了兼容老的结构
            templateCode=typeEnum.getTemplateCode();
            log.warn("[sendAlertMessageToUser][配置({}) 用户({}) 接收方式({}) 未配置模板，使用默认模板（{}）]",
                    config.getId(), userId, typeEnum,templateCode);
        }
        try {
            switch (typeEnum) {
                case SMS:
                    smsSendApi.sendSingleSmsToAdmin(new SmsSendSingleToUserReqDTO().setUserId(userId)
                            .setTemplateCode(templateCode).setTemplateParams(templateParams));
                    break;
                case MAIL:
                    mailSendApi.sendSingleMailToAdmin(new MailSendSingleToUserReqDTO().setUserId(userId)
                            .setTemplateCode(templateCode).setTemplateParams(templateParams));
                    break;
                case NOTIFY:
                    notifyMessageSendApi.sendSingleMessageToAdmin(new NotifySendSingleToUserReqDTO().setUserId(userId)
                            .setTemplateCode(templateCode).setTemplateParams(templateParams));
                    break;
            }
        } catch (Exception ex) {
            log.error("[sendAlertMessageToUser][用户({}) 模板参数({}) 发送 {} 告警失败]",
                    userId, templateParams, typeEnum, ex);
        }
    }

    private String resolveTemplateCode(IotAlertConfigDO config, IotAlertReceiveTypeEnum typeEnum) {
        String templateCode = switch (typeEnum) {
            case SMS -> config.getSmsTemplateCode();
            case MAIL -> config.getMailTemplateCode();
            case NOTIFY -> config.getNotifyTemplateCode();
        };
        return StrUtil.blankToDefault(templateCode, typeEnum.getTemplateCode());
    }

    private Map<String, Object> buildTemplateParams(IotAlertConfigDO config,
                                                    @Nullable IotDeviceMessage deviceMessage,
                                                    @Nullable IotDeviceDO device) {
        Map<String, Object> params = new HashMap<>();
        params.put("configName", config.getName());
        params.put("configDescription", config.getDescription());
        params.put("configLevel", DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.ALERT_LEVEL, config.getLevel()));
        params.put("deviceName", device != null ? device.getDeviceName() : null);
        params.put("reportTime", deviceMessage != null
                ? LocalDateTimeUtil.format(deviceMessage.getReportTime(), DatePattern.NORM_DATETIME_PATTERN) : null);
        return params;
    }

    @Override
    public IotSceneRuleActionTypeEnum getType() {
        return IotSceneRuleActionTypeEnum.ALERT_TRIGGER;
    }

}
