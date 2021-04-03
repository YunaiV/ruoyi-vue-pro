package cn.iocoder.dashboard.modules.system.service.sms.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.dashboard.common.core.KeyValue;
import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.common.enums.UserTypeEnum;
import cn.iocoder.dashboard.framework.sms.core.client.SmsClient;
import cn.iocoder.dashboard.framework.sms.core.client.SmsClientFactory;
import cn.iocoder.dashboard.framework.sms.core.client.SmsCommonResult;
import cn.iocoder.dashboard.framework.sms.core.client.dto.SmsSendRespDTO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.modules.system.mq.message.sms.SysSmsSendMessage;
import cn.iocoder.dashboard.modules.system.mq.producer.sms.SysSmsProducer;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsChannelService;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsLogService;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsService;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsTemplateService;
import cn.iocoder.dashboard.modules.system.service.user.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.*;

/**
 * 短信日志Service实现类
 *
 * @author zzf
 * @date 2021/1/25 9:25
 */
@Service
@Slf4j
public class SysSmsServiceImpl implements SysSmsService {

    @Resource
    private SysSmsChannelService smsChannelService;
    @Resource
    private SysSmsTemplateService smsTemplateService;
    @Resource
    private SysSmsLogService smsLogService;
    @Resource
    private SysSmsProducer smsProducer;
    @Resource
    private SmsClientFactory smsClientFactory;

    @Resource
    private SysUserService userService;

    @Override
    public void sendSingleSms(String mobile, Long userId, Integer userType,
                              String templateCode, Map<String, Object> templateParams) {
        // 校验短信模板是否合法
        SysSmsTemplateDO template = this.checkSmsTemplateValid(templateCode);
        // 校验手机号码是否存在
        mobile = this.checkMobile(mobile, userId, userType);

        // 创建发送日志
        Boolean isSend = CommonStatusEnum.ENABLE.getStatus().equals(template.getStatus()); // 如果模板被禁用，则不发送短信，只记录日志
        String content = smsTemplateService.formatSmsTemplateContent(template.getContent(), templateParams);
        Long sendLogId = smsLogService.createSmsLog(mobile, userId, userType, isSend, template, content, templateParams);

        // 发送 MQ 消息，异步执行发送短信
        if (!isSend) {
            return;
        }
        List<KeyValue<String, Object>> newTemplateParams = this.buildTemplateParams(template, templateParams);
        smsProducer.sendSmsSendMessage(sendLogId, mobile, template.getChannelId(), template.getApiTemplateId(), newTemplateParams);
    }

    @Override
    public void sendBatchSms(List<String> mobiles, List<Long> userIds, Integer userType,
                             String templateCode, Map<String, Object> templateParams) {
        // 校验短信模板是否存在
        SysSmsTemplateDO template = this.checkSmsTemplateValid(templateCode);
    }

    private SysSmsTemplateDO checkSmsTemplateValid(String templateCode) {
        // 短信模板不存在
        SysSmsTemplateDO template = smsTemplateService.getSmsTemplateByCode(templateCode);
        if (template == null) {
            throw exception(SMS_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    /**
     * 将参数模板，处理成有序的 KeyValue 数组
     *
     * 原因是，部分短信平台并不是使用 key 作为参数，而是数组下标，例如说腾讯云 https://cloud.tencent.com/document/product/382/39023
     *
     * @param template 短信模板
     * @param templateParams 原始参数
     * @return 处理后的参数
     */
    private List<KeyValue<String, Object>> buildTemplateParams(SysSmsTemplateDO template, Map<String, Object> templateParams) {
        return template.getParams().stream().map(key -> {
            Object value = templateParams.get(key);
            if (value == null) {
                throw exception(SMS_SEND_MOBILE_TEMPLATE_PARAM_MISS, key);
            }
            return new KeyValue<>(key, value);
        }).collect(Collectors.toList());
    }

    private String checkMobile(String mobile, Long userId, Integer userType) {
        mobile = getMobile(mobile, userId, userType);
        if (StrUtil.isEmpty(mobile)) {
            throw exception(SMS_SEND_MOBILE_NOT_EXISTS);
        }
        return mobile;
    }

    private String getMobile(String mobile, Long userId, Integer userType) {
        // 如果已经有手机号，则直接返回
        if (StrUtil.isNotEmpty(mobile)) {
            return mobile;
        }
        // 没有手机号，则基于 userId 检索
        if (userId == null || userType == null) {
            return null;
        }
        if (Objects.equals(userType, UserTypeEnum.ADMIN.getValue())) {
            SysUserDO user = userService.getUser(userId);
            return user != null ? user.getMobile() : null;
        }
        // TODO 芋艿：支持 C 端用户
        return null;
    }

    @Override
    public void doSendSms(SysSmsSendMessage message) {
        // 获得渠道对应的 SmsClient 客户端
        SmsClient smsClient = smsClientFactory.getSmsClient(message.getChannelId());
        Assert.notNull(smsClient, String.format("短信客户端(%d) 不存在", message.getChannelId()));
        // 发送短信
        SmsCommonResult<SmsSendRespDTO> sendResult = smsClient.send(message.getLogId(), message.getMobile(),
                message.getApiTemplateId(), message.getTemplateParams());
        smsLogService.updateSmsSendResult(message.getLogId(), sendResult.getCode(), sendResult.getMsg(),
                sendResult.getApiCode(), sendResult.getApiMsg(), sendResult.getApiRequestId(), sendResult.getData().getSerialNo());
    }

    @Override
    public Object smsSendCallbackHandle(ServletRequest request) {
//        SmsResultDetail smsResultDetail = smsClientFactory.getSmsResultDetailFromCallbackQuery(request);
//        logService.updateSendLogByResultDetail(smsResultDetail);
//        return smsResultDetail.getCallbackResponseBody();
        return null;
    }

}
