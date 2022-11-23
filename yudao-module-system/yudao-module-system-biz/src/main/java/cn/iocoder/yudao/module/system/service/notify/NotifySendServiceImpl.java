package cn.iocoder.yudao.module.system.service.notify;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.notify.NotifyMessageMapper;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.NOTICE_NOT_FOUND;

/**
 * 站内信发送 Service 实现类
 *
 * @author xrcoder
 */
@Service
@Validated
@Slf4j
public class NotifySendServiceImpl implements NotifySendService {

    @Resource
    private NotifyTemplateService notifyTemplateService;

    @Resource
    private NotifyMessageMapper notifyMessageMapper;

    @Override
    public Long sendSingleNotifyToAdmin(Long userId, Long templateId, Map<String, Object> templateParams) {

        return sendSingleNotify(userId, UserTypeEnum.ADMIN.getValue(), templateId, templateParams);
    }

    @Override
    public Long sendSingleNotifyToMember(Long userId, Long templateId, Map<String, Object> templateParams) {
        return sendSingleNotify(userId, UserTypeEnum.MEMBER.getValue(), templateId, templateParams);
    }

    @Override
    public Long sendSingleNotify(Long userId, Integer userType, Long templateId, Map<String, Object> templateParams) {
        // 校验短信模板是否合法
        NotifyTemplateDO template = this.checkNotifyTemplateValid(templateId);
        String content = notifyTemplateService.formatNotifyTemplateContent(template.getContent(), templateParams);

        // todo 模板状态未开启时的业务
        NotifyMessageDO notifyMessageDO = new NotifyMessageDO();
        notifyMessageDO.setContent(content);
        notifyMessageDO.setTitle(template.getTitle());
        notifyMessageDO.setReadStatus(false);
        notifyMessageDO.setReadTime(new Date());
        notifyMessageDO.setTemplateId(templateId);
        notifyMessageDO.setUserId(userId);
        notifyMessageDO.setUserType(userType);
        notifyMessageMapper.insert(notifyMessageDO);
        return notifyMessageDO.getId();
    }

    // 此注解的含义
    @VisibleForTesting
    public NotifyTemplateDO checkNotifyTemplateValid(Long templateId) {
        // 获得短信模板。考虑到效率，从缓存中获取
        NotifyTemplateDO template = notifyTemplateService.getNotifyTemplate(templateId);
        // 短信模板不存在
        if (template == null) {
            throw exception(NOTICE_NOT_FOUND);
        }
        return template;
    }

}
