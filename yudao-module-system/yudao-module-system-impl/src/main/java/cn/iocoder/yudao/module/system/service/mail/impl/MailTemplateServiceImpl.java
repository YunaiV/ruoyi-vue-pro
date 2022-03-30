package cn.iocoder.yudao.module.system.service.mail.impl;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplatePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateUpdateReqVO;
import cn.iocoder.yudao.module.system.convert.mail.MailTemplateConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailTemplateMapper;
import cn.iocoder.yudao.module.system.service.mail.MailTemplateService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
// TODO @wangjingyi：需要 @Validated 注解，开启参数校验
public class MailTemplateServiceImpl implements MailTemplateService {

    @Resource
    private MailTemplateMapper mailTemplateMapper;

    @Override
    public Long create(MailTemplateCreateReqVO createReqVO) {
        // name 要校验唯一
        Map<String , String> map = new HashMap<>();
        map.put("name" , createReqVO.getName()); // TODO @wangjingyi：模板名重复没关系的；code 不能重复
        this.validateMailTemplateOnly(map);
        MailTemplateDO mailTemplateDO = MailTemplateConvert.INSTANCE.convert(createReqVO);
        mailTemplateMapper.insert(mailTemplateDO);
        return mailTemplateDO.getId();
    }

    @Override
    public void update(MailTemplateUpdateReqVO updateReqVO) {
        // username 要校验唯一
        Map<String , String> map = new HashMap<>();
        map.put("username" , updateReqVO.getUsername());  // TODO @wangjingyi：模板名重复没关系的；code 不能重复
        this.validateMailTemplateOnly(map);
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

    private void validateMailTemplateExists(Long id) {
        if (mailTemplateMapper.selectById(id) == null) {
            throw exception(MAIL_TEMPLATE_NOT_EXISTS);
        }
    }

    private void validateMailTemplateOnly(Map params){
        QueryWrapper queryWrapper = new QueryWrapper<MailTemplateDO>();
        params.forEach((k , v)->{
            queryWrapper.like(k , v);
        });
        if (mailTemplateMapper.selectOne(queryWrapper) != null) {
            throw exception(MAIL_TEMPLATE_EXISTS);
        }
    }
}
