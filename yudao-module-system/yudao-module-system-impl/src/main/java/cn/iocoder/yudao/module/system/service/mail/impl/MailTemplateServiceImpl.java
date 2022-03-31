package cn.iocoder.yudao.module.system.service.mail.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.send.MailReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplatePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateUpdateReqVO;
import cn.iocoder.yudao.module.system.convert.mail.MailAccountConvert;
import cn.iocoder.yudao.module.system.convert.mail.MailTemplateConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailAccountMapper;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailTemplateMapper;
import cn.iocoder.yudao.module.system.service.mail.MailAccountService;
import cn.iocoder.yudao.module.system.service.mail.MailTemplateService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
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
public class MailTemplateServiceImpl implements MailTemplateService {

    @Resource
    private MailTemplateMapper mailTemplateMapper;
    @Resource
    private MailAccountMapper mailAccountMapper;

    @Override
    public Long create(MailTemplateCreateReqVO createReqVO) {
        // code 要校验唯一
        this.validateMailTemplateOnlyByCode(createReqVO.getCode());
        MailTemplateDO mailTemplateDO = MailTemplateConvert.INSTANCE.convert(createReqVO);
        mailTemplateMapper.insert(mailTemplateDO);
        return mailTemplateDO.getId();
    }

    @Override
    public void update(@Valid MailTemplateUpdateReqVO updateReqVO) {
        // code 要校验唯一
        this.validateMailTemplateOnlyByCode(updateReqVO.getCode());
        MailTemplateDO mailTemplateDO = MailTemplateConvert.INSTANCE.convert(updateReqVO);
        // 校验是否存在
        this.validateMailTemplateExists(mailTemplateDO.getId());
        mailTemplateMapper.updateById(mailTemplateDO);
    }

    @Override
    public void delete(Long id) {
        // 校验是否存在
        this.validateMailTemplateExists(id);
        mailTemplateMapper.deleteById(id);
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
    public void sendMail(MailReqVO mailReqVO) {
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
