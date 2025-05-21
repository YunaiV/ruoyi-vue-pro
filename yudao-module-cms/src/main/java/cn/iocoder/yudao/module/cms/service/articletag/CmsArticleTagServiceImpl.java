package cn.iocoder.yudao.module.cms.service.articletag;

import cn.iocoder.yudao.module.cms.dal.dataobject.articletag.CmsArticleTagDO;
import cn.iocoder.yudao.module.cms.dal.mysql.articletag.CmsArticleTagMapper;
import cn.iocoder.yudao.module.cms.service.article.CmsArticleService;
import cn.iocoder.yudao.module.cms.service.tag.CmsTagService;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Validated
public class CmsArticleTagServiceImpl implements CmsArticleTagService {

    @Resource
    private CmsArticleTagMapper articleTagMapper;

    @Resource
    private CmsArticleService articleService; // To validate article existence

    @Resource
    private CmsTagService tagService; // To validate tag existence

    @Override
    @Transactional
    public void createArticleTagList(Long articleId, Set<Long> tagIds) {
        // Validate article exists
        articleService.getArticle(articleId); // Throws if not found (assuming getArticle has this behavior)

        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        // Validate tags exist
        tagIds.forEach(tagId -> tagService.getTag(tagId)); // Throws if not found

        List<CmsArticleTagDO> articleTags = tagIds.stream()
                .map(tagId -> new CmsArticleTagDO().setArticleId(articleId).setTagId(tagId))
                .collect(Collectors.toList());
        articleTagMapper.insertBatch(articleTags);
    }

    @Override
    @Transactional
    public void updateArticleTags(Long articleId, Set<Long> tagIds) {
        // Validate article exists
        articleService.getArticle(articleId); // Throws if not found

        // Validate new tags exist (if any)
        if (!CollectionUtils.isEmpty(tagIds)) {
            tagIds.forEach(tagId -> tagService.getTag(tagId)); // Throws if not found
        }

        Set<Long> existingTagIds = getTagIdsByArticleId(articleId);

        Set<Long> tagsToAdd = Sets.difference(tagIds, existingTagIds);
        Set<Long> tagsToRemove = Sets.difference(existingTagIds, tagIds);

        if (!CollectionUtils.isEmpty(tagsToAdd)) {
            List<CmsArticleTagDO> newAssociations = tagsToAdd.stream()
                    .map(tagId -> new CmsArticleTagDO().setArticleId(articleId).setTagId(tagId))
                    .collect(Collectors.toList());
            articleTagMapper.insertBatch(newAssociations);
        }

        if (!CollectionUtils.isEmpty(tagsToRemove)) {
            articleTagMapper.deleteByArticleIdAndTagIds(articleId, tagsToRemove);
        }
    }

    @Override
    @Transactional
    public void deleteArticleTagsByArticleId(Long articleId) {
        articleTagMapper.deleteByArticleId(articleId);
    }

    @Override
    public List<CmsArticleTagDO> getArticleTagListByArticleId(Long articleId) {
        return articleTagMapper.selectListByArticleId(articleId);
    }

    @Override
    public Set<Long> getTagIdsByArticleId(Long articleId) {
        List<CmsArticleTagDO> associations = articleTagMapper.selectListByArticleId(articleId);
        return associations.stream().map(CmsArticleTagDO::getTagId).collect(Collectors.toSet());
    }

    @Override
    public Set<Long> getArticleIdsByTagId(Long tagId) {
        List<CmsArticleTagDO> associations = articleTagMapper.selectListByTagId(tagId);
        return associations.stream().map(CmsArticleTagDO::getArticleId).collect(Collectors.toSet());
    }
}
