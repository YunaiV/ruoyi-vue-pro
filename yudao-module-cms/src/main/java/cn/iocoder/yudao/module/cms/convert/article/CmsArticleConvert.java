package cn.iocoder.yudao.module.cms.convert.article;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cms.controller.admin.article.vo.CmsArticleCreateReqVO;
import cn.iocoder.yudao.module.cms.controller.admin.article.vo.CmsArticleRespVO;
import cn.iocoder.yudao.module.cms.controller.admin.article.vo.CmsArticleUpdateReqVO;
import cn.iocoder.yudao.module.cms.convert.category.CmsCategoryConvert;
import cn.iocoder.yudao.module.cms.convert.tag.CmsTagConvert;
import cn.iocoder.yudao.module.cms.dal.dataobject.article.CmsArticleDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CmsCategoryConvert.class, CmsTagConvert.class})
public interface CmsArticleConvert {

    CmsArticleConvert INSTANCE = Mappers.getMapper(CmsArticleConvert.class);

    CmsArticleDO convert(CmsArticleCreateReqVO bean);

    CmsArticleDO convert(CmsArticleUpdateReqVO bean);

    @Mappings({
        // If CmsArticleRespVO has fields like `categoryName` or `tags` that are not directly in CmsArticleDO,
        // but are enriched in the service layer, MapStruct will ignore them here by default.
        // We will handle enrichment in the service (CmsArticleServiceImpl#enrichArticleRespVO).
        // If tagIds are directly on the DO (which they are, as @TableField(exist=false)), MapStruct can map them.
        @Mapping(source = "tagIds", target = "tagIds")
    })
    CmsArticleRespVO convert(CmsArticleDO bean);

    List<CmsArticleRespVO> convertList(List<CmsArticleDO> list); // Typically not used if enrichment is per item

    PageResult<CmsArticleRespVO> convertPage(PageResult<CmsArticleDO> page);
}
