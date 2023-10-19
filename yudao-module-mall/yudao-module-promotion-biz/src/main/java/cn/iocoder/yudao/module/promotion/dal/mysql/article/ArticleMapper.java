package cn.iocoder.yudao.module.promotion.dal.mysql.article;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.article.vo.article.ArticlePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.article.vo.article.AppArticlePageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.article.ArticleDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 文章管理 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface ArticleMapper extends BaseMapperX<ArticleDO> {

    default PageResult<ArticleDO> selectPage(ArticlePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ArticleDO>()
                .eqIfPresent(ArticleDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(ArticleDO::getTitle, reqVO.getTitle())
                .eqIfPresent(ArticleDO::getAuthor, reqVO.getAuthor())
                .eqIfPresent(ArticleDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ArticleDO::getSpuId, reqVO.getSpuId())
                .eqIfPresent(ArticleDO::getRecommendHot, reqVO.getRecommendHot())
                .eqIfPresent(ArticleDO::getRecommendBanner, reqVO.getRecommendBanner())
                .betweenIfPresent(ArticleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ArticleDO::getId));
    }

    default List<ArticleDO> selectList(Boolean recommendHot, Boolean recommendBanner) {
        return selectList(new LambdaQueryWrapperX<ArticleDO>()
                .eqIfPresent(ArticleDO::getRecommendHot, recommendHot)
                .eqIfPresent(ArticleDO::getRecommendBanner, recommendBanner));
    }

    default PageResult<ArticleDO> selectPage(AppArticlePageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<ArticleDO>()
                .eqIfPresent(ArticleDO::getCategoryId, pageReqVO.getCategoryId()));
    }

    default void updateBrowseCount(Long id) {
        update(null, new LambdaUpdateWrapper<ArticleDO>()
                .eq(ArticleDO::getId, id)
                .setSql("browse_count = browse_count + 1"));
    }

}
