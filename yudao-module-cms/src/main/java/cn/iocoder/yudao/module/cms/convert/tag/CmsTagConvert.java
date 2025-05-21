package cn.iocoder.yudao.module.cms.convert.tag;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cms.controller.admin.tag.vo.*;
import cn.iocoder.yudao.module.cms.dal.dataobject.tag.CmsTagDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CmsTagConvert {

    CmsTagConvert INSTANCE = Mappers.getMapper(CmsTagConvert.class);

    CmsTagDO convert(CmsTagCreateReqVO bean);

    CmsTagDO convert(CmsTagUpdateReqVO bean);

    CmsTagRespVO convert(CmsTagDO bean);

    List<CmsTagRespVO> convertList(List<CmsTagDO> list);

    List<CmsTagSimpleRespVO> convertListSimple(List<CmsTagDO> list);

    PageResult<CmsTagRespVO> convertPage(PageResult<CmsTagDO> page);
}
