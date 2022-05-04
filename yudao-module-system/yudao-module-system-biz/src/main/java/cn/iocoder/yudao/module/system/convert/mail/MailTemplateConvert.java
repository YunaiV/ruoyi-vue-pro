package cn.iocoder.yudao.module.system.convert.mail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateBaseVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface MailTemplateConvert {
    MailTemplateConvert INSTANCE = Mappers.getMapper(MailTemplateConvert.class);

    MailTemplateDO convert(MailTemplateBaseVO baseVO);

    MailTemplateRespVO convert(MailTemplateDO mailTemplateDO);

    PageResult<MailTemplateRespVO> convertPage(PageResult<MailTemplateDO> pageResult);

    List<MailTemplateRespVO> convertList02(List<MailTemplateDO> list);
}
