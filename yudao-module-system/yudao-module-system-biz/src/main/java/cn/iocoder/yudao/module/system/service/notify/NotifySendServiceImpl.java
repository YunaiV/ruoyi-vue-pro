package cn.iocoder.yudao.module.system.service.notify;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyTemplateDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.notify.NotifyMessageMapper;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.NOTICE_NOT_FOUND;

// TODO @luowenfeng：可以直接合并到 NotifyMessageService 中；之前 sms 台复杂，所以没合并。
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

    @Resource
    private AdminUserService userService;

    @Override
    public Long sendSingleNotifyToAdmin(Long userId, String templateCode, Map<String, Object> templateParams) {
        return sendSingleNotify(userId, UserTypeEnum.ADMIN.getValue(), templateCode, templateParams);
    }

    @Override
    public Long sendSingleNotifyToMember(Long userId, String templateCode, Map<String, Object> templateParams) {
        return sendSingleNotify(userId, UserTypeEnum.MEMBER.getValue(), templateCode, templateParams);
    }

    @Override
    public Long sendSingleNotify(Long userId, Integer userType, String templateCode, Map<String, Object> templateParams) {
        // 校验短信模板是否合法
        NotifyTemplateDO template = this.checkNotifyTemplateValid(templateCode);
        String content = notifyTemplateService.formatNotifyTemplateContent(template.getContent(), templateParams);
        // 获得用户
        AdminUserDO sendUser = userService.getUser(getLoginUserId());

        // todo 模板状态未开启时的业务；如果未开启，就直接 return 好了；
        NotifyMessageDO notifyMessageDO = new NotifyMessageDO();
        notifyMessageDO.setContent(content);
        notifyMessageDO.setTitle(template.getTitle());
        notifyMessageDO.setReadStatus(false);
        notifyMessageDO.setTemplateId(template.getId());
        notifyMessageDO.setTemplateCode(templateCode);
        notifyMessageDO.setUserId(userId);
        notifyMessageDO.setUserType(userType);
        notifyMessageDO.setSendTime(new Date());
        notifyMessageDO.setSendUserId(sendUser.getId());
        notifyMessageDO.setSendUserName(sendUser.getUsername());
        notifyMessageMapper.insert(notifyMessageDO);
        return notifyMessageDO.getId();
    }

    // 此注解的含义
    @VisibleForTesting
    public NotifyTemplateDO checkNotifyTemplateValid(String templateCode) {
        // 获得短信模板。考虑到效率，从缓存中获取
        NotifyTemplateDO template = notifyTemplateService.getNotifyTemplateByCodeFromCache(templateCode);
        // 短信模板不存在
        if (template == null) {
            throw exception(NOTICE_NOT_FOUND);
        }
        return template;
    }

}
