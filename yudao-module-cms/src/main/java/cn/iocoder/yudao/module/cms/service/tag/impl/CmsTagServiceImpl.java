package cn.iocoder.yudao.module.cms.service.tag.impl;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.cms.controller.admin.tag.vo.CmsTagCreateReqVO;
import cn.iocoder.yudao.module.cms.controller.admin.tag.vo.CmsTagPageReqVO;
import cn.iocoder.yudao.module.cms.controller.admin.tag.vo.CmsTagUpdateReqVO;
import cn.iocoder.yudao.module.cms.convert.tag.CmsTagConvert;
import cn.iocoder.yudao.module.cms.dal.dataobject.tag.CmsTagDO;
import cn.iocoder.yudao.module.cms.dal.mysql.tag.CmsTagMapper;
import cn.iocoder.yudao.module.cms.service.tag.CmsTagService;
// TODO: Add relevant ErrorCodeConstants, for now using placeholders like:
// import static cn.iocoder.yudao.module.cms.enums.ErrorCodeConstants.*; 
// Using system error codes as placeholders for now
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.DICT_TYPE_NAME_DUPLICATE; // Placeholder for TAG_NAME_DUPLICATE
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.DICT_TYPE_NOT_EXISTS; // Placeholder for TAG_NOT_EXISTS
// import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.TAG_SLUG_DUPLICATE; // Needs definition
// import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.TAG_IN_USE; // Needs definition for delete check

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
@Validated
@Slf4j
public class CmsTagServiceImpl implements CmsTagService {

    @Resource
    private CmsTagMapper tagMapper;

    @Resource
    private CmsTagConvert tagConvert;

    // TODO: Inject CmsArticleTagMapper to check for usage before deletion

    @Override
    public Long createTag(CmsTagCreateReqVO createReqVO) {
        validateTagNameUnique(createReqVO.getName(), null);
        validateTagSlugUnique(createReqVO.getSlug(), null);

        CmsTagDO tag = tagConvert.convert(createReqVO);
        tagMapper.insert(tag);
        return tag.getId();
    }

    @Override
    public void updateTag(CmsTagUpdateReqVO updateReqVO) {
        validateTagExists(updateReqVO.getId());
        validateTagNameUnique(updateReqVO.getName(), updateReqVO.getId());
        validateTagSlugUnique(updateReqVO.getSlug(), updateReqVO.getId());

        CmsTagDO updateDO = tagConvert.convert(updateReqVO);
        tagMapper.updateById(updateDO);
    }

    @Override
    public void deleteTag(Long id) {
        validateTagExists(id);
        // TODO: Validate if tag is used by any articles (cms_article_tag table)
        // if (articleTagMapper.selectCountByTagId(id) > 0) {
        //     throw ServiceExceptionUtil.exception(TAG_IN_USE);
        // }
        tagMapper.deleteById(id);
    }

    @Override
    public CmsTagDO getTag(Long id) {
        return tagMapper.selectById(id);
    }

    @Override
    @TenantIgnore // Slugs might be cross-tenant or need specific handling
    public CmsTagDO getTagBySlug(String slug) {
        return tagMapper.selectBySlug(slug, null); // Assuming global slug for now or tenantId is handled elsewhere
    }

    @Override
    public List<CmsTagDO> getTagList(CmsTagPageReqVO reqVO) {
        // For simple list, usually for UI selectors.
        // This could be enhanced to use filters from reqVO if needed.
        return tagMapper.selectList(new LambdaQueryWrapperX<CmsTagDO>()
                .likeIfPresent(CmsTagDO::getName, reqVO.getName())
                .likeIfPresent(CmsTagDO::getSlug, reqVO.getSlug())
                .orderByAsc(CmsTagDO::getName)); // Example sort
    }

    @Override
    public PageResult<CmsTagDO> getTagPage(CmsTagPageReqVO pageReqVO) {
        return tagMapper.selectPage(pageReqVO);
    }

    private void validateTagNameUnique(String name, Long selfId) {
        CmsTagDO tagWithName = tagMapper.selectByName(name, null); // TODO: Pass tenantId
        if (tagWithName != null && !Objects.equals(tagWithName.getId(), selfId)) {
            throw ServiceExceptionUtil.exception(DICT_TYPE_NAME_DUPLICATE); // Placeholder for TAG_NAME_DUPLICATE
        }
    }

    private void validateTagSlugUnique(String slug, Long selfId) {
        CmsTagDO tagWithSlug = tagMapper.selectBySlug(slug, null); // TODO: Pass tenantId
        if (tagWithSlug != null && !Objects.equals(tagWithSlug.getId(), selfId)) {
            // throw ServiceExceptionUtil.exception(TAG_SLUG_DUPLICATE); // Define this error code
            throw ServiceExceptionUtil.exception(DICT_TYPE_NAME_DUPLICATE); // Placeholder for now
        }
    }

    private void validateTagExists(Long id) {
        if (tagMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception(DICT_TYPE_NOT_EXISTS); // Placeholder for TAG_NOT_EXISTS
        }
    }
}
