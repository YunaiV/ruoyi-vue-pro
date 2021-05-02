package cn.iocoder.yudao.adminserver.modules.system.convert.sms;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.adminserver.modules.system.controller.sms.vo.template.SysSmsTemplateCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.sms.vo.template.SysSmsTemplateExcelVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.sms.vo.template.SysSmsTemplateRespVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.sms.vo.template.SysSmsTemplateUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysSmsTemplateConvert {

    SysSmsTemplateConvert INSTANCE = Mappers.getMapper(SysSmsTemplateConvert.class);

    SysSmsTemplateDO convert(SysSmsTemplateCreateReqVO bean);

    SysSmsTemplateDO convert(SysSmsTemplateUpdateReqVO bean);

    SysSmsTemplateRespVO convert(SysSmsTemplateDO bean);

    List<SysSmsTemplateRespVO> convertList(List<SysSmsTemplateDO> list);

    PageResult<SysSmsTemplateRespVO> convertPage(PageResult<SysSmsTemplateDO> page);

    List<SysSmsTemplateExcelVO> convertList02(List<SysSmsTemplateDO> list);

}
