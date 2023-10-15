package cn.iocoder.yudao.module.promotion.convert.article;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.article.vo.ArticleCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.article.vo.ArticleExcelVO;
import cn.iocoder.yudao.module.promotion.controller.admin.article.vo.ArticleRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.article.vo.ArticleUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.article.ArticleDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 文章管理 Convert
 *
 * @author HUIHUI
 */
@Mapper
public interface ArticleConvert {

    ArticleConvert INSTANCE = Mappers.getMapper(ArticleConvert.class);

    ArticleDO convert(ArticleCreateReqVO bean);

    ArticleDO convert(ArticleUpdateReqVO bean);

    ArticleRespVO convert(ArticleDO bean);

    List<ArticleRespVO> convertList(List<ArticleDO> list);

    PageResult<ArticleRespVO> convertPage(PageResult<ArticleDO> page);

    List<ArticleExcelVO> convertList02(List<ArticleDO> list);

}
