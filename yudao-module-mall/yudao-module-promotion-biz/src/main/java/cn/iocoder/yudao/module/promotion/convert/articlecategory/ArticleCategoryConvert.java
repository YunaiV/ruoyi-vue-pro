package cn.iocoder.yudao.module.promotion.convert.articlecategory;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.articlecategory.vo.*;
import cn.iocoder.yudao.module.promotion.dal.dataobject.articlecategory.ArticleCategoryDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 文章分类 Convert
 *
 * @author HUIHUI
 */
@Mapper
public interface ArticleCategoryConvert {

    ArticleCategoryConvert INSTANCE = Mappers.getMapper(ArticleCategoryConvert.class);

    ArticleCategoryDO convert(ArticleCategoryCreateReqVO bean);

    ArticleCategoryDO convert(ArticleCategoryUpdateReqVO bean);

    ArticleCategoryRespVO convert(ArticleCategoryDO bean);

    List<ArticleCategoryRespVO> convertList(List<ArticleCategoryDO> list);

    PageResult<ArticleCategoryRespVO> convertPage(PageResult<ArticleCategoryDO> page);

    List<ArticleCategoryExcelVO> convertList02(List<ArticleCategoryDO> list);

    List<ArticleCategorySimpleRespVO> convertList03(List<ArticleCategoryDO> list);

}
