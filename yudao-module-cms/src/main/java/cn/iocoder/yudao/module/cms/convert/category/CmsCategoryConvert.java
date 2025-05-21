package cn.iocoder.yudao.module.cms.convert.category;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cms.controller.admin.category.vo.*;
import cn.iocoder.yudao.module.cms.dal.dataobject.category.CmsCategoryDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CmsCategoryConvert {

    CmsCategoryConvert INSTANCE = Mappers.getMapper(CmsCategoryConvert.class);

    CmsCategoryDO convert(CmsCategoryCreateReqVO bean);

    CmsCategoryDO convert(CmsCategoryUpdateReqVO bean);

    CmsCategoryRespVO convert(CmsCategoryDO bean);

    List<CmsCategoryRespVO> convertList(List<CmsCategoryDO> list);

    List<CmsCategorySimpleRespVO> convertListSimple(List<CmsCategoryDO> list);

    PageResult<CmsCategoryRespVO> convertPage(PageResult<CmsCategoryDO> page);
}
