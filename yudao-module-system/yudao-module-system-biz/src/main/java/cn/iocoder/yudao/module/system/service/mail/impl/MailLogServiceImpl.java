package cn.iocoder.yudao.module.system.service.mail.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.MailLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.MailLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailLogDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailLogMapper;
import cn.iocoder.yudao.module.system.enums.mail.MailSendStatusEnum;
import cn.iocoder.yudao.module.system.enums.sms.SmsSendStatusEnum;
import cn.iocoder.yudao.module.system.service.mail.MailLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 *  邮箱日志实现类
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Service
@Validated
public class MailLogServiceImpl implements MailLogService {

    @Autowired
    MailLogMapper mailLogMapper;

    @Override
    public PageResult<MailLogDO> getMailLogPage(MailLogPageReqVO pageVO) {
        return mailLogMapper.selectPage(pageVO);
    }

    @Override
    public List<MailLogDO> getMailLogList(MailLogExportReqVO exportReqVO) {
        return mailLogMapper.selectList(exportReqVO);
    }

    @Override
    public Long createMailLog(MailAccountDO mailAccountDO , MailTemplateDO mailTemplateDO , String from, String content, List<String> tos, String title, Boolean isSend) {
        MailLogDO.MailLogDOBuilder logDOBuilder = MailLogDO.builder();
        logDOBuilder.from(mailAccountDO.getFrom());
        logDOBuilder.accountId(mailAccountDO.getId());
        logDOBuilder.content(content);
        logDOBuilder.title(title);
        logDOBuilder.templateCode(mailTemplateDO.getCode());
        logDOBuilder.templateId(mailTemplateDO.getId());
        logDOBuilder.to(tos.toString());
        logDOBuilder.sendTime(new Date());
        logDOBuilder.sendStatus(Objects.equals(isSend, true) ? MailSendStatusEnum.INIT.getStatus()
                : MailSendStatusEnum.IGNORE.getStatus());

        MailLogDO mailLogDO = logDOBuilder.build();
        mailLogMapper.insert(mailLogDO);
        return mailLogDO.getId();
    }

    // TODO @wangjingyi：不需要返回 id 呀
    @Override
    public Long updateSmsSendResult(Long logId, String result) {
        MailLogDO.MailLogDOBuilder logDOBuilder = MailLogDO.builder();
        logDOBuilder.id(logId);
        logDOBuilder.sendResult(result);
        MailLogDO mailLogDO = logDOBuilder.build();
        mailLogMapper.updateById(mailLogDO);
        return logId;
    }

    // TODO @wangjingyi：无用的方法，需要进行删除
    public Long create(){
        MailLogDO mailLogDO = new MailLogDO();
        mailLogMapper.insert(mailLogDO);
        return mailLogDO.getId();
    }
}
