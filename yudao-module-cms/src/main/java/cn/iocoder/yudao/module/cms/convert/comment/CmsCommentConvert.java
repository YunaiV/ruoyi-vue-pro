package cn.iocoder.yudao.module.cms.convert.comment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cms.controller.admin.comment.vo.CmsCommentCreateReqVO;
import cn.iocoder.yudao.module.cms.controller.admin.comment.vo.CmsCommentRespVO;
import cn.iocoder.yudao.module.cms.controller.admin.comment.vo.CmsCommentUpdateReqVO;
import cn.iocoder.yudao.module.cms.dal.dataobject.comment.CmsCommentDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CmsCommentConvert {

    CmsCommentConvert INSTANCE = Mappers.getMapper(CmsCommentConvert.class);

    CmsCommentDO convert(CmsCommentCreateReqVO bean);

    // For update, typically you'd load the DO then map specific fields from the VO.
    // If UpdateReqVO only contains status, this direct mapping is fine for that field.
    // If more fields are updatable via Admin, ensure they are in UpdateReqVO and mapped.
    @Mappings({
        @Mapping(target = "id", ignore = true), // Usually ID is not mapped from an update VO directly to DO id
        @Mapping(target = "status", source = "status") // Example: only status is updated from this VO
        // Add other fields if admin can update them, e.g. content
        // @Mapping(target = "content", source = "content")
    })
    CmsCommentDO convert(CmsCommentUpdateReqVO bean, @MappingTarget CmsCommentDO targetDO);


    @Mappings({
        // If CmsCommentRespVO has fields like `articleTitle` or `userNickname`
        // that are enriched in the service layer, MapStruct will ignore them here by default if not in CmsCommentDO.
        // We will handle enrichment in the service (CmsCommentServiceImpl#enrichCommentRespVO).
    })
    CmsCommentRespVO convert(CmsCommentDO bean);

    List<CmsCommentRespVO> convertList(List<CmsCommentDO> list);

    PageResult<CmsCommentRespVO> convertPage(PageResult<CmsCommentDO> page);
}
