package cn.iocoder.yudao.module.system.service.mail.impl;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.system.convert.mail.MailAccountConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailAccountMapper;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailTemplateMapper;
import cn.iocoder.yudao.module.system.mq.message.mail.MailSendMessage;
import cn.iocoder.yudao.module.system.mq.producer.mail.MailProducer;
import cn.iocoder.yudao.module.system.service.mail.MailLogService;
import cn.iocoder.yudao.module.system.service.mail.MailSendService;
import cn.iocoder.yudao.module.system.service.mail.MailTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;

/**
 * 邮箱模版 服务实现类
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Service
@Validated
@Slf4j
public class MailSendServiceImpl implements MailSendService {

    @Resource
    private MailTemplateMapper mailTemplateMapper;
    @Resource
    private MailAccountMapper mailAccountMapper;
    @Resource
    private MailTemplateService mailTemplateService;
    @Resource
    private MailLogService mailLogService;
    @Resource
    private MailProducer mailProducer;


    @Override
    public void sendMail(String templateCode, String from , String content , List<String> tos , String title) {
        // TODO @@wangjingyi：发送的时候，参考下短信；DONE
        //校验邮箱模版是否合法
        MailTemplateDO mailTemplateDO =  this.checkMailTemplateValid(templateCode);
        // 创建发送日志。如果模板被禁用，则不发送短信，只记录日志
        Boolean isSend = CommonStatusEnum.ENABLE.getStatus().equals(mailTemplateDO.getStatus());
        //校验账号信息是否合法
        MailAccountDO mailAccountDO = this.checkMailAccountValid(from);
        Map<String , String> params = MailAccountConvert.INSTANCE.convertToMap(mailAccountDO , content);
        content = mailTemplateService.formatMailTemplateContent(mailTemplateDO.getContent(), params);
        Long sendLogId = mailLogService.createMailLog(mailAccountDO , mailTemplateDO , from , content , tos , title , isSend);

        // 后续功能 TODO ：附件查询
        //List<String> fileIds = mailSendVO.getFileIds();

        // 发送 MQ 消息，异步执行发送短信
        if (isSend) {
            mailProducer.sendMailSendMessage(mailAccountDO , mailTemplateDO ,content , tos , title , sendLogId);
        }
    }

    private MailAccountDO checkMailAccountValid(String from) {
        MailAccountDO mailAccountDO = mailAccountMapper.selectOneByFrom(from);
        if(null == mailAccountDO){
            throw exception(MAIL_ACCOUNT_NOT_EXISTS);
        }
        return mailAccountDO;
    }

    @Override
    public void doSendMail(MailSendMessage message) {
        // TODO @wangjingyi：直接使用 hutool 发送，不要封装 mail client 哈，因为短信的客户端都是比较统一的 DONE
        //装载账号信息
        MailAccount account  = MailAccountConvert.INSTANCE.convertAccount(message);
        //发送邮件
        try{
            String messageId = MailUtil.send(account,message.getTos(),message.getTitle(),message.getContent(),false,null);
            mailLogService.updateMailSendResult(message.getLogId() , messageId);
        }catch (Exception e){
            mailLogService.updateMailSendResult(message.getLogId() , e.getMessage());
        }

    }

    private MailTemplateDO checkMailTemplateValid(String templateCode) {
        MailTemplateDO mailTemplateDO = mailTemplateService.getMailTemplateByCodeFromCache(templateCode);
        if (mailTemplateDO == null){
            throw exception(MAIL_TEMPLATE_NOT_EXISTS);
        }
        return mailTemplateDO;
    }

    private void validateMailTemplateExists(Long id) {
        if (mailTemplateMapper.selectById(id) == null) {
            throw exception(MAIL_TEMPLATE_NOT_EXISTS);
        }
    }

    private void validateMailTemplateOnlyByCode(String code){
        if (mailTemplateMapper.selectOneByCode(code) != null) {
            throw exception(MAIL_TEMPLATE_EXISTS);
        }
    }
}
