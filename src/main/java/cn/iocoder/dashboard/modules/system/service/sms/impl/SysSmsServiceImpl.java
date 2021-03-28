package cn.iocoder.dashboard.modules.system.service.sms.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.common.enums.UserTypeEnum;
import cn.iocoder.dashboard.framework.sms.core.SmsClientFactory;
import cn.iocoder.dashboard.framework.sms.core.SmsResultDetail;
import cn.iocoder.dashboard.framework.sms.core.enums.SmsSendFailureTypeEnum;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.modules.system.mq.message.sms.SysSmsSendMessage;
import cn.iocoder.dashboard.modules.system.mq.producer.sms.SysSmsProducer;
import cn.iocoder.dashboard.modules.system.service.sms.*;
import cn.iocoder.dashboard.modules.system.service.user.SysUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.*;

/**
 * 短信日志Service实现类
 *
 * @author zzf
 * @date 2021/1/25 9:25
 */
@Service
public class SysSmsServiceImpl implements SysSmsService {

    @Resource
    private SysSmsTemplateService smsTemplateService;
    @Resource
    private SysSmsSendLogService smsSendLogService;

    @Resource
    private SysUserService userService;

    @Resource
    private SysSmsChannelService channelService;


    @Resource
    private SysSmsQueryLogService logService;

    @Resource
    private SysSmsProducer smsProducer;

    @Resource
    private SmsClientFactory smsClientFactory;

    @Override
    public void sendSingleSms(String mobile, Long userId, Integer userType,
                              String templateCode, Map<String, Object> templateParams) {
        // 校验短信模板是否合法
        SysSmsTemplateDO template = this.checkSmsTemplateValid(templateCode, templateParams);
        // 校验手机号码是否存在
        mobile = this.checkMobile(mobile, userId, userType);

        // 创建发送日志
        String content = smsTemplateService.formatSmsTemplateContent(template.getContent(), templateParams);
        Long sendLogId = smsSendLogService.createSmsSendLog(mobile, userId, userType, template, content, templateParams);

        // 如果模板被禁用，则直接标记发送失败。也就说，不发短信，嘿嘿。
        if (CommonStatusEnum.DISABLE.getStatus().equals(template.getStatus())) {
            smsSendLogService.updateSmsSendLogFailure(sendLogId, SmsSendFailureTypeEnum.SMS_TEMPLATE_DISABLE.getType());
            return;
        }
        // 如果模板未禁用，发送 MQ 消息。目的是，异步化调用短信平台
        smsProducer.sendSmsSendMessage(sendLogId, mobile, template.getChannelId(), template.getApiTemplateId(), templateParams);
    }

    @Override
    public void sendBatchSms(List<String> mobiles, List<Long> userIds, Integer userType,
                             String templateCode, Map<String, Object> templateParams) {
        // 校验短信模板是否存在
        SysSmsTemplateDO template = this.checkSmsTemplateValid(templateCode, templateParams);
    }

    private SysSmsTemplateDO checkSmsTemplateValid(String templateCode, Map<String, Object> templateParams) {
        // 短信模板不存在
        SysSmsTemplateDO template = smsTemplateService.getSmsTemplateByCode(templateCode);
        if (template == null) {
            throw exception(SMS_TEMPLATE_NOT_EXISTS);
        }
        // 参数不够
        if (CollUtil.isNotEmpty(template.getParams())) {
            template.getParams().forEach(param -> {
                if (!templateParams.containsKey(param)) {
                    throw exception(SMS_SEND_MOBILE_TEMPLATE_PARAM_MISS);
                }
            });
        }
        // 移除多余参数
        if (CollUtil.isEmpty(template.getParams())) {
            templateParams.clear();
        } else {
            templateParams.entrySet().removeIf(entry -> !template.getParams().contains(entry.getKey()));
        }
        return template;
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
        return null;
    }

    @Override
    public void doSendSms(SysSmsSendMessage message) {

    }

    @Override
    public Object smsSendCallbackHandle(ServletRequest request) {
        SmsResultDetail smsResultDetail = smsClientFactory.getSmsResultDetailFromCallbackQuery(request);
        logService.updateSendLogByResultDetail(smsResultDetail);
        return smsResultDetail.getCallbackResponseBody();
    }

}
