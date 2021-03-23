package cn.iocoder.dashboard.modules.system.service.sms.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.dashboard.common.enums.UserTypeEnum;
import cn.iocoder.dashboard.framework.sms.client.AbstractSmsClient;
import cn.iocoder.dashboard.framework.sms.core.SmsBody;
import cn.iocoder.dashboard.framework.sms.core.SmsClientFactory;
import cn.iocoder.dashboard.framework.sms.core.SmsResultDetail;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.modules.system.mq.producer.sms.SmsSendStreamProducer;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsChannelService;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsQueryLogService;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsService;
import cn.iocoder.dashboard.modules.system.service.sms.SysSmsTemplateService;
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
    private SysUserService userService;

    @Resource
    private SysSmsChannelService channelService;


    @Resource
    private SysSmsQueryLogService logService;

    @Resource
    private SmsSendStreamProducer smsProducer;

    @Resource
    private SmsClientFactory smsClientFactory;

    @Override
    public void sendSingleSms(String mobile, Long userId, Integer userType,
                              String templateCode, Map<String, Object> templateParams) {
        // 校验短信模板是否存在
        SysSmsTemplateDO template = this.checkSmsTemplateExists(templateCode);
        // 校验手机号码是否存在
        mobile = this.checkMobile(mobile, userId, userType);
    }

    @Override
    public void sendBatchSms(List<String> mobiles, List<Long> userIds, Integer userType,
                             String templateCode, Map<String, Object> templateParams) {
        // 校验短信模板是否存在
        SysSmsTemplateDO template = this.checkSmsTemplateExists(templateCode);
    }

    private SysSmsTemplateDO checkSmsTemplateExists(String templateCode) {
        SysSmsTemplateDO template = smsTemplateService.getSmsTemplateByCode(templateCode);
        if (template == null) {
            throw exception(SMS_TEMPLATE_NOT_EXISTS);
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
