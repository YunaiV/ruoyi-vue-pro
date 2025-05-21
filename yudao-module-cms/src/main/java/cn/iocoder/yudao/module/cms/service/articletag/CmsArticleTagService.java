package cn.iocoder.yudao.module.cms.service.articletag;

import cn.iocoder.yudao.module.cms.dal.dataobject.articletag.CmsArticleTagDO;

import java.util.List;
import java.util.Set;

public interface CmsArticleTagService {

    /**
     * Creates associations for a given article and a set of tag IDs.
     *
     * @param articleId The ID of the article.
     * @param tagIds    The set of tag IDs to associate.
     */
    void createArticleTagList(Long articleId, Set<Long> tagIds);

    /**
     * Updates the associations for an article.
     * This involves deleting old associations not in the new set and adding new ones.
     *
     * @param articleId The ID of the article.
     * @param tagIds    The new set of tag IDs for the article.
     */
    void updateArticleTags(Long articleId, Set<Long> tagIds);

    /**
     * Deletes all associations for an article.
     *
     * @param articleId The ID of the article whose tag associations are to be deleted.
     */
    void deleteArticleTagsByArticleId(Long articleId);

    /**
     * Retrieves a list of CmsArticleTagDO objects for a given article ID.
     *
     * @param articleId The ID of the article.
     * @return A list of CmsArticleTagDO objects.
     */
    List<CmsArticleTagDO> getArticleTagListByArticleId(Long articleId);

    /**
     * Retrieves a set of tag IDs for a given article ID.
     *
     * @param articleId The ID of the article.
     * @return A set of tag IDs.
     */
    Set<Long> getTagIdsByArticleId(Long articleId);

    /**
     * Retrieves a set of article IDs for a given tag ID.
     *
     * @param tagId The ID of the tag.
     * @return A set of article IDs.
     */
    Set<Long> getArticleIdsByTagId(Long tagId);
}
