package cn.iocoder.yudao.module.system.convert.sms;

import cn.iocoder.yudao.module.system.controller.admin.sms.vo.template.SmsTemplateCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.sms.vo.template.SmsTemplateExcelVO;
import cn.iocoder.yudao.module.system.controller.admin.sms.vo.template.SmsTemplateRespVO;
import cn.iocoder.yudao.module.system.controller.admin.sms.vo.template.SmsTemplateUpdateReqVO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SmsTemplateConvert {

    SmsTemplateConvert INSTANCE = Mappers.getMapper(SmsTemplateConvert.class);

    SysSmsTemplateDO convert(SmsTemplateCreateReqVO bean);

    SysSmsTemplateDO convert(SmsTemplateUpdateReqVO bean);

    SmsTemplateRespVO convert(SysSmsTemplateDO bean);

    List<SmsTemplateRespVO> convertList(List<SysSmsTemplateDO> list);

    PageResult<SmsTemplateRespVO> convertPage(PageResult<SysSmsTemplateDO> page);

    List<SmsTemplateExcelVO> convertList02(List<SysSmsTemplateDO> list);

}
