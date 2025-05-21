package cn.iocoder.yudao.module.cms.service.article;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cms.controller.admin.article.vo.CmsArticleCreateReqVO;
import cn.iocoder.yudao.module.cms.controller.admin.article.vo.CmsArticlePageReqVO;
import cn.iocoder.yudao.module.cms.controller.admin.article.vo.CmsArticleRespVO;
import cn.iocoder.yudao.module.cms.controller.admin.article.vo.CmsArticleUpdateReqVO;

import javax.validation.Valid;

public interface CmsArticleService {

    /**
     * Create an article
     *
     * @param createReqVO Create request VO
     * @return Article ID
     */
    Long createArticle(@Valid CmsArticleCreateReqVO createReqVO);

    /**
     * Update an article
     *
     * @param updateReqVO Update request VO
     */
    void updateArticle(@Valid CmsArticleUpdateReqVO updateReqVO);

    /**
     * Delete an article
     *
     * @param id Article ID
     */
    void deleteArticle(Long id);

    /**
     * Get an article by ID, enriched with category and tag information.
     *
     * @param id Article ID
     * @return Enriched Article Response VO
     */
    CmsArticleRespVO getArticle(Long id);

    /**
     * Get an article by slug, enriched with category and tag information.
     *
     * @param slug Article slug
     * @return Enriched Article Response VO
     */
    CmsArticleRespVO getArticleBySlug(String slug);
    
    /**
     * Get article page, enriched with category and tag information.
     *
     * @param pageReqVO Page request VO
     * @return Page of enriched articles
     */
    PageResult<CmsArticleRespVO> getArticlePage(CmsArticlePageReqVO pageReqVO);

    /**
     * Publish an article
     * @param id Article ID
     */
    void publishArticle(Long id);

    /**
     * Unpublish an article (set to draft status)
     * @param id Article ID
     */
    void unpublishArticle(Long id);

    /**
     * Increment view count for an article
     * @param id Article ID
     */
    void incrementArticleViews(Long id); // Added this method as per previous CmsArticleMapper plan
}
