package cn.iocoder.yudao.module.system.service.mail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.MailLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.MailLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailLogDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailLogMapper;
import cn.iocoder.yudao.module.system.enums.mail.MailSendStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 邮件日志 Service 实现类
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Service
@Validated
public class MailLogServiceImpl implements MailLogService {

    @Resource
    private MailLogMapper mailLogMapper;

    @Override
    public PageResult<MailLogDO> getMailLogPage(MailLogPageReqVO pageVO) {
        return mailLogMapper.selectPage(pageVO);
    }

    @Override
    public List<MailLogDO> getMailLogList(MailLogExportReqVO exportReqVO) {
        return mailLogMapper.selectList(exportReqVO);
    }

    @Override
    public Long createMailLog(Long userId, Integer userType, String toMail,
                              MailAccountDO account, MailTemplateDO template,
                              String templateContent, Map<String, Object> templateParams, Boolean isSend) {
        MailLogDO.MailLogDOBuilder logDOBuilder = MailLogDO.builder();
        // 根据是否要发送，设置状态
        logDOBuilder.sendStatus(Objects.equals(isSend, true) ? MailSendStatusEnum.INIT.getStatus()
                : MailSendStatusEnum.IGNORE.getStatus())
                // 用户信息
                .userId(userId).userType(userType).toMail(toMail)
                .accountId(account.getId()).fromMail(account.getMail())
                // 模板相关字段
                .templateId(template.getId()).templateCode(template.getCode()).templateNickname(template.getNickname())
                .templateTitle(template.getTitle()).templateContent(templateContent).templateParams(templateParams);

        // 插入数据库
        MailLogDO logDO = logDOBuilder.build();
        mailLogMapper.insert(logDO);
        return logDO.getId();
    }

    // TODO @wangjingyi：还是加几个字段哈，日志上。sendStatus，成功、失败；messageId 消息标号。sendException 记录发送的异常。这样界面才好筛选邮件的发送结果。DONE
    @Override
    public void updateMailSendResult(Long logId, String result) {
        MailLogDO.MailLogDOBuilder logDOBuilder = MailLogDO.builder();
        logDOBuilder.id(logId).sendTime(new Date()).sendResult(result).sendMessageId(result).sendStatus(MailSendStatusEnum.SUCCESS.getStatus());
        MailLogDO mailLogDO = logDOBuilder.build();
        mailLogMapper.updateById(mailLogDO);
    }

    @Override
    public void updateFailMailSendResult(Long logId, String exception) {
        MailLogDO.MailLogDOBuilder logDOBuilder = MailLogDO.builder();
        logDOBuilder.id(logId).sendTime(new Date()).sendResult(exception).sendStatus(MailSendStatusEnum.FAILURE.getStatus());
        MailLogDO mailLogDO = logDOBuilder.build();
        mailLogMapper.updateById(mailLogDO);
    }
}
