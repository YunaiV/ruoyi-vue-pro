package cn.iocoder.yudao.module.mp.convert.newstemplate;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.mp.controller.admin.newstemplate.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.newstemplate.WxNewsTemplateDO;

/**
 * 图文消息模板 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface WxNewsTemplateConvert {

    WxNewsTemplateConvert INSTANCE = Mappers.getMapper(WxNewsTemplateConvert.class);

    WxNewsTemplateDO convert(WxNewsTemplateCreateReqVO bean);

    WxNewsTemplateDO convert(WxNewsTemplateUpdateReqVO bean);

    WxNewsTemplateRespVO convert(WxNewsTemplateDO bean);

    List<WxNewsTemplateRespVO> convertList(List<WxNewsTemplateDO> list);

    PageResult<WxNewsTemplateRespVO> convertPage(PageResult<WxNewsTemplateDO> page);

    List<WxNewsTemplateExcelVO> convertList02(List<WxNewsTemplateDO> list);

}
