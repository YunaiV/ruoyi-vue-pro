package cn.iocoder.yudao.module.system.service.mail.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.send.MailReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplatePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateUpdateReqVO;
import cn.iocoder.yudao.module.system.convert.mail.MailAccountConvert;
import cn.iocoder.yudao.module.system.convert.mail.MailTemplateConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.dal.dataobject.sms.SmsTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailAccountMapper;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailTemplateMapper;
import cn.iocoder.yudao.module.system.service.mail.MailTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.MAIL_TEMPLATE_EXISTS;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.MAIL_TEMPLATE_NOT_EXISTS;

/**
 * 邮箱模版 服务实现类
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Service
@Validated
@Slf4j
public class MailTemplateServiceImpl implements MailTemplateService {

    @Resource
    private MailTemplateMapper mailTemplateMapper;
    @Resource
    private MailAccountMapper mailAccountMapper;

    private volatile List<MailTemplateDO> mailTemplateDOList;

    /**
     * 邮件模板缓存
     * key：邮箱模板编码 {@link MailTemplateDO#getCode()}
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Map<String, MailTemplateDO> mailTemplateCache;

    private volatile Date maxUpdateTime;

    // TODO @wangjingyi：参考下别的模块的 initLocalCache 的实现
    @Override
    @PostConstruct
    public void initLocalCache() {
        if(maxUpdateTime == null){
            mailTemplateDOList = mailTemplateMapper.selectList();
        }else{
            if(mailTemplateMapper.selectByMaxUpdateTime(maxUpdateTime)<=0){
                return;
            }
        }
        if (CollUtil.isEmpty(mailTemplateDOList)) {
            return;
        }

        // 写入缓存
        mailTemplateCache = CollectionUtils.convertMap(mailTemplateDOList, MailTemplateDO::getCode);
        maxUpdateTime = CollectionUtils.getMaxValue(mailTemplateDOList, MailTemplateDO::getUpdateTime);
        log.info("[initLocalCache][初始化 mailTemplate 数量为 {}]", mailTemplateDOList.size());
    }

    @Override
    public Long create(MailTemplateCreateReqVO createReqVO) {
        // code 要校验唯一
        // TODO @wangjingyi：参考下我在 account 给的唯一校验的说明。
        this.validateMailTemplateOnlyByCode(createReqVO.getCode());
        MailTemplateDO mailTemplateDO = MailTemplateConvert.INSTANCE.convert(createReqVO);
        mailTemplateMapper.insert(mailTemplateDO);
        // TODO @wangjingyi：mq 更新
        return mailTemplateDO.getId();
    }

    @Override
    public void update(@Valid MailTemplateUpdateReqVO updateReqVO) {
        // 校验是否存在
        this.validateMailTemplateExists(updateReqVO.getId());
        MailTemplateDO mailTemplateDO = MailTemplateConvert.INSTANCE.convert(updateReqVO);
        mailTemplateMapper.updateById(mailTemplateDO);
        // TODO @wangjingyi：mq 更新
    }

    @Override
    public void delete(Long id) {
        // 校验是否存在
        this.validateMailTemplateExists(id);
        mailTemplateMapper.deleteById(id);
        // TODO @wangjingyi：mq 更新
    }

    @Override
    public MailTemplateDO getMailTemplate(Long id) {return mailTemplateMapper.selectById(id);}

    @Override
    public PageResult<MailTemplateDO> getMailTemplatePage(MailTemplatePageReqVO pageReqVO) {
        return mailTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MailTemplateDO> getMailTemplateList() {return mailTemplateMapper.selectList();}

    @Override
    public MailTemplateDO getMailTemplateByCodeFromCache(String code) {
        return mailTemplateCache.get(code);
    }

    @Override
    public void sendMail(MailReqVO mailReqVO) {
        // TODO @@wangjingyi：发送的时候，参考下短信；
        MailTemplateDO mailTemplateDO =  mailTemplateMapper.selectById(mailReqVO.getTemplateId());
        //查询账号信息
        MailAccountDO mailAccountDO = mailAccountMapper.selectOne(
                "from", mailReqVO.getFrom()
        );
        String content = mailReqVO.getContent();
        Map<String , String> params = MailAccountConvert.INSTANCE.convertToMap(mailAccountDO , content);
        content = StrUtil.format(mailTemplateDO.getContent(), params);

        // 后续功能 TODO ：附件查询
        //List<String> fileIds = mailSendVO.getFileIds();

        //装载账号信息
        MailAccount account  = MailAccountConvert.INSTANCE.convertAccount(mailAccountDO);

        //发送
        MailUtil.send(account , mailReqVO.getTos() , mailReqVO.getTitle() , content , false);
    }

    // TODO @@wangjingyi：单词拼写错误
    @Override
    public String formateMailTemplateContent(String content, Map<String, String> params) {
        return StrUtil.format(content, params);
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
