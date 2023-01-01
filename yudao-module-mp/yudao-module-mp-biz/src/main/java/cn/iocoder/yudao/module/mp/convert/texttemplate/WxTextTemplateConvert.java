package cn.iocoder.yudao.module.mp.convert.texttemplate;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.mp.controller.admin.texttemplate.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.texttemplate.WxTextTemplateDO;

/**
 * 文本模板 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface WxTextTemplateConvert {

    WxTextTemplateConvert INSTANCE = Mappers.getMapper(WxTextTemplateConvert.class);

    WxTextTemplateDO convert(WxTextTemplateCreateReqVO bean);

    WxTextTemplateDO convert(WxTextTemplateUpdateReqVO bean);

    WxTextTemplateRespVO convert(WxTextTemplateDO bean);

    List<WxTextTemplateRespVO> convertList(List<WxTextTemplateDO> list);

    PageResult<WxTextTemplateRespVO> convertPage(PageResult<WxTextTemplateDO> page);

    List<WxTextTemplateExcelVO> convertList02(List<WxTextTemplateDO> list);

}
