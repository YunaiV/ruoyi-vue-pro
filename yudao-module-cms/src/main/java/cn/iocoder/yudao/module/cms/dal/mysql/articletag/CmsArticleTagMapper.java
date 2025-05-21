package cn.iocoder.yudao.module.cms.dal.mysql.articletag;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.cms.dal.dataobject.articletag.CmsArticleTagDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface CmsArticleTagMapper extends BaseMapperX<CmsArticleTagDO> {

    default List<CmsArticleTagDO> selectListByArticleId(Long articleId) {
        return selectList(CmsArticleTagDO::getArticleId, articleId);
    }

    default List<CmsArticleTagDO> selectListByTagId(Long tagId) {
        return selectList(CmsArticleTagDO::getTagId, tagId);
    }

    default void deleteByArticleId(Long articleId) {
        delete(new LambdaQueryWrapperX<CmsArticleTagDO>()
                .eq(CmsArticleTagDO::getArticleId, articleId));
    }

    default void deleteByArticleIdAndTagIds(@Param("articleId") Long articleId,
                                            @Param("tagIds") Collection<Long> tagIds) {
        delete(new LambdaQueryWrapperX<CmsArticleTagDO>()
                .eq(CmsArticleTagDO::getArticleId, articleId)
                .in(CmsArticleTagDO::getTagId, tagIds));
    }
}
