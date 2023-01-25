package cn.iocoder.yudao.module.system.service.mail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.MailLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.MailLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailLogDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailLogMapper;
import cn.iocoder.yudao.module.system.enums.mail.MailSendStatusEnum;
import cn.iocoder.yudao.module.system.service.mail.MailLogService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    public Long createMailLog(Long userId,Integer userType,String to,MailAccountDO mailAccountDO, MailTemplateDO template , String templateContent, Map<String, Object> templateParams, Boolean isSend) {
        MailLogDO.MailLogDOBuilder logDOBuilder = MailLogDO.builder();
        // TODO @wangjingyi：使用 builder 的时候，不用每个 set 是一行。DONE
        // 根据是否要发送，设置状态
        logDOBuilder.sendStatus(Objects.equals(isSend, true) ? MailSendStatusEnum.INIT.getStatus()
                : MailSendStatusEnum.IGNORE.getStatus())
                // 设置邮箱相关字段
                .fromMail(mailAccountDO.getMail())
                .accountId(mailAccountDO.getId())
                // TODO @wangjingyi：userId、userType
                //用户信息
                .userId(userId).userType(userType)
                //模版信息
                .templateId(template.getId()).templateParams(templateParams).templateContent(templateContent);


        logDOBuilder.fromMail(mailAccountDO.getMail());
        logDOBuilder.accountId(mailAccountDO.getId());
        // TODO @wangjingyi：每个接收人一条日志。发送多个人，就调用多次，业务方。因为某个邮箱有问题，会导致所有都发送失败。 DONE
        // 设置模板相关字段
        // TODO @wangjingyi：可以参考下 sms 短信的逻辑，templateContent、templateParams
        // TODO @wangjingyi：有结果的时候，才是 sendTime 哈 DONE
        //logDOBuilder.sendTime(new Date());

        // 插入数据库
        MailLogDO logDO = logDOBuilder.build();
        mailLogMapper.insert(logDO);
        return logDO.getId();
    }

    // TODO @wangjingyi：还是加几个字段哈，日志上。sendStatus，成功、失败；messageId 消息标号。sendException 记录发送的异常。这样界面才好筛选邮件的发送结果。DONE
    @Override
    public void updateMailSendResult(Long logId, String result) {
        MailLogDO.MailLogDOBuilder logDOBuilder = MailLogDO.builder();
        logDOBuilder.id(logId).sendTime(new Date()).sendResult(result).messageId(result).sendStatus(MailSendStatusEnum.SUCCESS.getStatus());
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
