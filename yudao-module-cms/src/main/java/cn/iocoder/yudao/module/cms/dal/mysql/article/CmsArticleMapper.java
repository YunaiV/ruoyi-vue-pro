package cn.iocoder.yudao.module.cms.dal.mysql.article;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.cms.controller.admin.article.vo.CmsArticlePageReqVO;
import cn.iocoder.yudao.module.cms.dal.dataobject.article.CmsArticleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CmsArticleMapper extends BaseMapperX<CmsArticleDO> {

    default CmsArticleDO selectBySlug(@Param("slug") String slug, @Param("tenantId") Long tenantId) {
        return selectOne(CmsArticleDO::getSlug, slug, CmsArticleDO::getTenantId, tenantId);
    }

    default PageResult<CmsArticleDO> selectPage(CmsArticlePageReqVO reqVO) {
        LambdaQueryWrapperX<CmsArticleDO> queryWrapper = new LambdaQueryWrapperX<CmsArticleDO>()
                .likeIfPresent(CmsArticleDO::getTitle, reqVO.getTitle())
                .eqIfPresent(CmsArticleDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(CmsArticleDO::getStatus, reqVO.getStatus())
                // TODO: Add filtering by tagId if CmsArticleTag join is implemented here or in service
                .orderByDesc(CmsArticleDO::getCreateTime); // Default sort, can be made dynamic

        return selectPage(reqVO, queryWrapper);
    }

    default void incrementViews(@Param("id") Long id) {
        CmsArticleDO article = new CmsArticleDO();
        article.setId(id);
        article.setViews(1); // This is a placeholder, actual increment logic is more complex
        // MyBatis Plus does not directly support increment via updateById with a partial object like this.
        // Usually, this would be an XML-defined statement or a service-layer read-modify-write.
        // For now, this method signature is a placeholder for the intent.
        // A proper implementation would be:
        // UPDATE cms_article SET views = views + 1 WHERE id = #{id} and deleted = 0
        // This method will likely be removed or implemented via XML or a custom BaseMapperX method.
        update("UPDATE cms_article SET views = views + 1 WHERE id = #{id} AND deleted = 0", null);
    }
}
