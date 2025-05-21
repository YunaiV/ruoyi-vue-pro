package cn.iocoder.yudao.module.cms.service.article.impl;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.cms.controller.admin.article.vo.CmsArticleCreateReqVO;
import cn.iocoder.yudao.module.cms.controller.admin.article.vo.CmsArticlePageReqVO;
import cn.iocoder.yudao.module.cms.controller.admin.article.vo.CmsArticleRespVO;
import cn.iocoder.yudao.module.cms.controller.admin.article.vo.CmsArticleUpdateReqVO;
import cn.iocoder.yudao.module.cms.controller.admin.tag.vo.CmsTagSimpleRespVO;
import cn.iocoder.yudao.module.cms.convert.article.CmsArticleConvert;
import cn.iocoder.yudao.module.cms.convert.tag.CmsTagConvert;
import cn.iocoder.yudao.module.cms.dal.dataobject.article.CmsArticleDO;
import cn.iocoder.yudao.module.cms.dal.dataobject.category.CmsCategoryDO;
import cn.iocoder.yudao.module.cms.dal.dataobject.tag.CmsTagDO;
import cn.iocoder.yudao.module.cms.dal.mysql.article.CmsArticleMapper;
import cn.iocoder.yudao.module.cms.service.article.CmsArticleService;
import cn.iocoder.yudao.module.cms.service.articletag.CmsArticleTagService;
import cn.iocoder.yudao.module.cms.service.category.CmsCategoryService;
import cn.iocoder.yudao.module.cms.service.tag.CmsTagService;

// TODO: Add relevant ErrorCodeConstants for CMS Article
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*; // Using system as placeholder


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.CollectionUtils;


import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Validated
@Slf4j
public class CmsArticleServiceImpl implements CmsArticleService {

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PUBLISHED = 1;

    @Resource
    private CmsArticleMapper articleMapper;

    @Resource
    private CmsArticleConvert articleConvert;

    @Resource
    private CmsCategoryService categoryService;

    @Resource
    private CmsTagService tagService;

    @Resource
    private CmsArticleTagService articleTagService;

    @Resource
    private CmsTagConvert tagConvert; // For converting List<CmsTagDO> to List<CmsTagSimpleRespVO>

    @Override
    @Transactional
    public Long createArticle(CmsArticleCreateReqVO createReqVO) {
        validateCategoryExists(createReqVO.getCategoryId());
        // Tag existence validation will be handled by articleTagService or here if preferred
        // For now, assuming articleTagService handles individual tag validation.
        validateArticleSlugUnique(createReqVO.getSlug(), null);

        CmsArticleDO article = articleConvert.convert(createReqVO);
        article.setStatus(STATUS_DRAFT);
        // TODO: Set authorId from current user context: article.setAuthorId(SecurityFrameworkUtils.getLoginUserId());
        articleMapper.insert(article);

        if (!CollectionUtils.isEmpty(createReqVO.getTagIds())) {
            articleTagService.createArticleTagList(article.getId(), createReqVO.getTagIds());
        }
        return article.getId();
    }

    @Override
    @Transactional
    public void updateArticle(CmsArticleUpdateReqVO updateReqVO) {
        validateArticleExists(updateReqVO.getId());
        validateCategoryExists(updateReqVO.getCategoryId());
        // Tag existence validation for updateReqVO.getTagIds() will be handled by articleTagService
        validateArticleSlugUnique(updateReqVO.getSlug(), updateReqVO.getId());

        CmsArticleDO updateDO = articleConvert.convert(updateReqVO);
        articleMapper.updateById(updateDO);

        // Handle update of article-tag associations
        // Pass null if tagIds is not meant to be updated, or an empty set to clear all tags.
        // The current VOs don't distinguish between "not provided" and "empty list for clearing".
        // Assuming if tagIds is in the VO, it's the source of truth.
        articleTagService.updateArticleTags(updateDO.getId(), updateReqVO.getTagIds());
    }

    @Override
    @Transactional
    public void deleteArticle(Long id) {
        validateArticleExists(id);
        articleTagService.deleteArticleTagsByArticleId(id);
        // TODO: Delete article comments if any
        articleMapper.deleteById(id);
    }

    @Override
    public CmsArticleRespVO getArticle(Long id) {
        CmsArticleDO article = articleMapper.selectById(id);
        if (article == null) {
            throw ServiceExceptionUtil.exception(DEPT_NOT_FOUND); // Placeholder for ARTICLE_NOT_EXISTS
        }
        return enrichArticleRespVO(article);
    }

    @Override
    @TenantIgnore
    public CmsArticleRespVO getArticleBySlug(String slug) {
        CmsArticleDO article = articleMapper.selectBySlug(slug, null);
        if (article == null) {
            throw ServiceExceptionUtil.exception(DEPT_NOT_FOUND); // Placeholder for ARTICLE_NOT_EXISTS
        }
        return enrichArticleRespVO(article);
    }

    @Override
    public PageResult<CmsArticleRespVO> getArticlePage(CmsArticlePageReqVO pageReqVO) {
        PageResult<CmsArticleDO> pageResult = articleMapper.selectPage(pageReqVO);
        List<CmsArticleRespVO> enrichedList = pageResult.getList().stream()
                .map(this::enrichArticleRespVO)
                .collect(Collectors.toList());
        return new PageResult<>(enrichedList, pageResult.getTotal());
    }

    @Override
    @Transactional
    public void publishArticle(Long id) {
        CmsArticleDO article = validateArticleExistsAndReturn(id);
        if (Objects.equals(article.getStatus(), STATUS_PUBLISHED)) {
            return; 
        }
        CmsArticleDO updateDO = new CmsArticleDO();
        updateDO.setId(id);
        updateDO.setStatus(STATUS_PUBLISHED);
        updateDO.setPublishedAt(LocalDateTime.now());
        articleMapper.updateById(updateDO);
    }

    @Override
    @Transactional
    public void unpublishArticle(Long id) {
        CmsArticleDO article = validateArticleExistsAndReturn(id);
        if (Objects.equals(article.getStatus(), STATUS_DRAFT)) {
            return;
        }
        CmsArticleDO updateDO = new CmsArticleDO();
        updateDO.setId(id);
        updateDO.setStatus(STATUS_DRAFT);
        articleMapper.updateById(updateDO);
    }
    
    @Override
    @Transactional
    public void incrementArticleViews(Long id) {
        validateArticleExists(id);
        articleMapper.incrementViews(id);
    }

    private CmsArticleRespVO enrichArticleRespVO(CmsArticleDO article) {
        if (article == null) {
            return null;
        }
        CmsArticleRespVO respVO = articleConvert.convert(article);

        if (article.getCategoryId() != null) {
            CmsCategoryDO category = categoryService.getCategory(article.getCategoryId());
            if (category != null) {
                respVO.setCategoryName(category.getName());
            }
        }

        Set<Long> tagIds = articleTagService.getTagIdsByArticleId(article.getId());
        respVO.setTagIds(tagIds != null ? tagIds : Collections.emptySet()); // Set the tagIds to be converted

        if (!CollectionUtils.isEmpty(tagIds)) {
            // Fetch CmsTagDOs based on tagIds
            // Assuming CmsTagService has a method like getTagListByIds(Collection<Long> ids)
            // For simplicity, if such a method doesn't exist, this part needs adjustment.
            // Let's assume for now CmsTagService.getTagList can take a list of IDs (not directly supported by current PageVO)
            // Or, we fetch one by one (inefficient) or add a new service method.
            // Placeholder: create a list of CmsTagDOs by fetching each tag
            List<CmsTagDO> tagDOs = tagIds.stream()
                                          .map(tagId -> tagService.getTag(tagId))
                                          .filter(Objects::nonNull)
                                          .collect(Collectors.toList());
            respVO.setTags(tagConvert.convertListSimple(tagDOs));
        } else {
            respVO.setTags(Collections.emptyList());
        }
        return respVO;
    }

    private void validateCategoryExists(Long categoryId) {
        if (categoryId != null) {
            CmsCategoryDO category = categoryService.getCategory(categoryId);
            if (category == null) {
                throw ServiceExceptionUtil.exception(DEPT_PARENT_NOT_EXITS); // Placeholder
            }
        } else {
             throw ServiceExceptionUtil.exception(DEPT_PARENT_NOT_EXITS); // Placeholder
        }
    }

    // Removed validateTagsExist as CmsArticleTagService will handle individual tag validation internally or this responsibility can be shifted there.

    private void validateArticleSlugUnique(String slug, Long selfId) {
        if (slug == null || slug.trim().isEmpty()) {
            throw ServiceExceptionUtil.exception(DEPT_NAME_NOT_NULL); // Placeholder
        }
        CmsArticleDO articleWithSlug = articleMapper.selectBySlug(slug, null); 
        if (articleWithSlug != null && !Objects.equals(articleWithSlug.getId(), selfId)) {
            throw ServiceExceptionUtil.exception(DEPT_NAME_DUPLICATE); // Placeholder
        }
    }

    private void validateArticleExists(Long id) {
        if (articleMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception(DEPT_NOT_FOUND); // Placeholder
        }
    }
    
    private CmsArticleDO validateArticleExistsAndReturn(Long id) {
        CmsArticleDO article = articleMapper.selectById(id);
        if (article == null) {
            throw ServiceExceptionUtil.exception(DEPT_NOT_FOUND); // Placeholder
        }
        return article;
    }
}
