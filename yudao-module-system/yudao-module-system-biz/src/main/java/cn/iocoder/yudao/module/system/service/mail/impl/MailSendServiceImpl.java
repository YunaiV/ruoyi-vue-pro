package cn.iocoder.yudao.module.system.service.mail.impl;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.system.convert.mail.MailAccountConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailAccountMapper;
import cn.iocoder.yudao.module.system.mq.message.mail.MailSendMessage;
import cn.iocoder.yudao.module.system.mq.producer.mail.MailProducer;
import cn.iocoder.yudao.module.system.service.mail.MailLogService;
import cn.iocoder.yudao.module.system.service.mail.MailSendService;
import cn.iocoder.yudao.module.system.service.mail.MailTemplateService;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private MailAccountMapper mailAccountMapper;
    @Resource
    private MailTemplateService mailTemplateService;
    @Resource
    private MailLogService mailLogService;
    @Resource
    private MailProducer mailProducer;


    @Override
    public void sendMail(Long userId, Integer userType, String templateCode, String from,String to, String content, Map<String, Object> templateParams) {
        // TODO @@wangjingyi：发送的时候，参考下短信；DONE
        //校验邮箱模版是否合法
        MailTemplateDO mailTemplateDO =  this.checkMailTemplateValid(templateCode);
        // 创建发送日志。如果模板被禁用，则不发送短信，只记录日志
        Boolean isSend = CommonStatusEnum.ENABLE.getStatus().equals(mailTemplateDO.getStatus());
        //校验账号信息是否合法
        MailAccountDO mailAccountDO = this.checkMailAccountValid(from);
        Map<String , String> params = MailAccountConvert.INSTANCE.convertToMap(mailAccountDO , content);
        content = mailTemplateService.formatMailTemplateContent(mailTemplateDO.getContent(), params);
        Long sendLogId = mailLogService.createMailLog(userId,userType,to,mailAccountDO , mailTemplateDO , content,  templateParams,  isSend);
        List<KeyValue<String,Object>> newTemplateParams = buildTemplateParams(mailTemplateDO,templateParams);
        // 发送 MQ 消息，异步执行发送短信
        if (isSend) {
            mailProducer.sendMailSendMessage(sendLogId,mailAccountDO , mailTemplateDO ,content, newTemplateParams,to);
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
            String messageId = MailUtil.send(account,message.getTo(),message.getTitle(),message.getContent(),false,null);
            mailLogService.updateMailSendResult(message.getLogId() , messageId);
        }catch (Exception e){
            mailLogService.updateFailMailSendResult(message.getLogId() , e.getMessage());
        }

    }

    private MailTemplateDO checkMailTemplateValid(String templateCode) {
        MailTemplateDO mailTemplateDO = mailTemplateService.getMailTemplateByCodeFromCache(templateCode);
        if (mailTemplateDO == null){
            throw exception(MAIL_TEMPLATE_NOT_EXISTS);
        }
        return mailTemplateDO;
    }
    /**
     * 将参数模板，处理成有序的 KeyValue 数组
     *
     * @param template 邮箱模板
     * @param templateParams 原始参数
     * @return 处理后的参数
     */
    @VisibleForTesting
    public List<KeyValue<String, Object>> buildTemplateParams(MailTemplateDO template, Map<String, Object> templateParams) {
        return template.getParams().stream().map(key -> {
            Object value = templateParams.get(key);
            if (value == null) {
                throw exception(MAIL_SEND_TEMPLATE_PARAM_MISS, key);
            }
            return new KeyValue<>(key, value);
        }).collect(Collectors.toList());
    }
}
