package cn.iocoder.yudao.module.system.service.mail.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplatePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateUpdateReqVO;
import cn.iocoder.yudao.module.system.convert.mail.MailTemplateConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailTemplateMapper;
import cn.iocoder.yudao.module.system.mq.producer.mail.MailProducer;
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
 * 邮箱模版 Service 实现类
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
    private MailProducer mailProducer;

    /**
     * 邮件模板缓存
     * key：邮箱模板编码 {@link MailTemplateDO#getId()}
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Map<Long, MailTemplateDO> mailTemplateCache;

    @Override
    @PostConstruct
    public void initLocalCache() {
        if (true) {
            return;
        }
        List<MailTemplateDO> mailTemplateDOList = this.loadMailTemplateIfUpdate(null);
        if (CollUtil.isEmpty(mailTemplateDOList)) {
            return;
        }

        // 写入缓存
        mailTemplateCache = CollectionUtils.convertMap(mailTemplateDOList, MailTemplateDO::getId);
        log.info("[initLocalCache][初始化 mailTemplate 数量为 {}]", mailTemplateDOList.size());
    }

    @Override
    public Long create(MailTemplateCreateReqVO createReqVO) {
        // 要校验存在
        validateMailTemplateExists(createReqVO.getId());
        MailTemplateDO mailTemplateDO = MailTemplateConvert.INSTANCE.convert(createReqVO);
        mailTemplateMapper.insert(mailTemplateDO);
        // TODO @wangjingyi：mq 更新 DONE
        mailProducer.sendMailTemplateRefreshMessage();
        return mailTemplateDO.getId();
    }

    @Override
    public void update(@Valid MailTemplateUpdateReqVO updateReqVO) {
        // 校验是否唯一
        // TODO @wangjingyi：参考下我在 account 给的唯一校验的说明。DONE
        this.validateMailTemplateOnlyByCode(updateReqVO.getId(),updateReqVO.getCode());
        MailTemplateDO mailTemplateDO = MailTemplateConvert.INSTANCE.convert(updateReqVO);
        mailTemplateMapper.updateById(mailTemplateDO);
        // TODO @wangjingyi：mq 更新 DONE
        mailProducer.sendMailTemplateRefreshMessage();
    }

    @Override
    public void delete(Long id) {
        // 校验是否存在
        this.validateMailTemplateExists(id);
        mailTemplateMapper.deleteById(id);
        // TODO @wangjingyi：mq 更新 DONE
        mailProducer.sendMailTemplateRefreshMessage();
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
    public String formatMailTemplateContent(String content, Map<String, String> params) {
        return StrUtil.format(content, params);
    }

    private void validateMailTemplateExists(Long id) {
        if (mailTemplateCache.get(id) == null) {
            throw exception(MAIL_TEMPLATE_NOT_EXISTS);
        }
    }

    private void validateMailTemplateOnlyByCode(Long id ,String code){
        mailTemplateCache.forEach((key,value)->{
            if (value.getCode().equals(code)){
                if (!key.equals(id)){
                    throw exception(MAIL_TEMPLATE_EXISTS);
                }
            }
        });
    }
    /**
     * 如果邮件模板发生变化，从数据库中获取最新的全量邮件模板。
     * 如果未发生变化，则返回空
     *
     * @param maxUpdateTime 当前邮件模板的最大更新时间
     * @return 邮件模板列表
     */
    private List<MailTemplateDO> loadMailTemplateIfUpdate(Date maxUpdateTime) {
        // 第一步，判断是否要更新。
        if (maxUpdateTime == null) { // 如果更新时间为空，说明 DB 一定有新数据
            log.info("[loadMailTemplateIfUpdate][首次加载全量邮件模板]");
        } else { // 判断数据库中是否有更新的邮件模板
            if (mailTemplateMapper.selectByMaxUpdateTime(maxUpdateTime) == 0) {
                return null;
            }
            log.info("[loadSmsTemplateIfUpdate][增量加载全量邮件模板]");
        }
        // 第二步，如果有更新，则从数据库加载所有邮件模板
        return mailTemplateMapper.selectList();
    }

    @Override
    public long countByAccountId(Long accountId) {
        return mailTemplateMapper.selectCountByAccountId(accountId);
    }

}
