package cn.iocoder.yudao.module.cms.service.category.impl;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.cms.controller.admin.category.vo.CmsCategoryCreateReqVO;
import cn.iocoder.yudao.module.cms.controller.admin.category.vo.CmsCategoryPageReqVO;
import cn.iocoder.yudao.module.cms.controller.admin.category.vo.CmsCategoryUpdateReqVO;
import cn.iocoder.yudao.module.cms.convert.category.CmsCategoryConvert;
import cn.iocoder.yudao.module.cms.dal.dataobject.category.CmsCategoryDO;
import cn.iocoder.yudao.module.cms.dal.mysql.category.CmsCategoryMapper;
import cn.iocoder.yudao.module.cms.service.category.CmsCategoryService;
// TODO: Add relevant ErrorCodeConstants, for now using placeholders from system module
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.DEPT_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.DEPT_PARENT_ERROR;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.DEPT_PARENT_IS_CHILD;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.DEPT_NOT_FOUND;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.DEPT_EXITS_CHILDREN;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
@Validated
@Slf4j
public class CmsCategoryServiceImpl implements CmsCategoryService {

    @Resource
    private CmsCategoryMapper categoryMapper;

    @Resource
    private CmsCategoryConvert categoryConvert;


    @Override
    public Long createCategory(CmsCategoryCreateReqVO createReqVO) {
        // Validate parent category
        validateParentCategory(createReqVO.getParentId(), null); // null for id as it's a new category

        // Validate name uniqueness
        validateCategoryNameUnique(createReqVO.getName(), createReqVO.getParentId(), null);

        // Validate slug uniqueness
        validateCategorySlugUnique(createReqVO.getSlug(), null);

        CmsCategoryDO category = categoryConvert.convert(createReqVO);
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public void updateCategory(CmsCategoryUpdateReqVO updateReqVO) {
        // Validate self exists
        validateCategoryExists(updateReqVO.getId());

        // Validate parent category
        validateParentCategory(updateReqVO.getParentId(), updateReqVO.getId());

        // Validate name uniqueness
        validateCategoryNameUnique(updateReqVO.getName(), updateReqVO.getParentId(), updateReqVO.getId());

        // Validate slug uniqueness
        validateCategorySlugUnique(updateReqVO.getSlug(), updateReqVO.getId());

        CmsCategoryDO updateDO = categoryConvert.convert(updateReqVO);
        categoryMapper.updateById(updateDO);
    }

    @Override
    public void deleteCategory(Long id) {
        // Validate self exists
        validateCategoryExists(id);
        // Validate if has children
        if (categoryMapper.selectCountByParentId(id, null) > 0) { // Assuming tenantId can be null for global check or needs to be passed
            throw ServiceExceptionUtil.exception(DEPT_EXITS_CHILDREN); // Placeholder error
        }
        categoryMapper.deleteById(id);
    }

    @Override
    public CmsCategoryDO getCategory(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    @TenantIgnore // Slugs are often used in URLs and might be cross-tenant or need specific tenant handling
    public CmsCategoryDO getCategoryBySlug(String slug) {
        // This might need tenant_id depending on business rules.
        // If slug is unique per tenant, pass tenantId from context.
        // For now, assuming global uniqueness or handled by TenantIgnore + application logic if needed.
        return categoryMapper.selectBySlug(slug, null); // Passing null as tenantId for now
    }

    @Override
    public List<CmsCategoryDO> getCategoryList(CmsCategoryPageReqVO reqVO) {
        // This method is intended for simple lists, usually for UI selectors.
        // PageResult from mapper is not directly used here, but the reqVO can carry filter conditions.
        // If reqVO has parentId, filter by it.
        if (reqVO.getParentId() != null) {
             return categoryMapper.selectListByParentId(reqVO.getParentId(), null); // TODO: tenantId
        }
        // Otherwise, fetch based on other filters like name, or just return all (limited for safety).
        // For now, let's assume it fetches all if no parentId, which might be too broad.
        // A more specific implementation based on exact needs for "simple list" is required.
        // For example, it could fetch all root categories if parentId is explicitly null or a special value.
        return categoryMapper.selectList(); // This fetches all, adjust as needed.
    }
    
    @Override
    public PageResult<CmsCategoryDO> getCategoryPage(CmsCategoryPageReqVO pageReqVO) {
        return categoryMapper.selectPage(pageReqVO);
    }

    private void validateParentCategory(Long parentId, Long selfId) {
        if (parentId == null || parentId == 0) { // 0 or null can represent root
            return;
        }
        // Cannot set self as parent
        if (Objects.equals(parentId, selfId)) {
            throw ServiceExceptionUtil.exception(DEPT_PARENT_ERROR); // Placeholder
        }
        CmsCategoryDO parentCategory = categoryMapper.selectById(parentId);
        if (parentCategory == null) {
            throw ServiceExceptionUtil.exception(DEPT_PARENT_ERROR); // Placeholder: Parent not found
        }
        // Check for circular dependency: if the intended parent is actually a child of the current category (selfId)
        if (selfId != null) {
            CmsCategoryDO currentCategory = categoryMapper.selectById(selfId);
            if (currentCategory == null) { // Should not happen if called after validateCategoryExists
                 throw ServiceExceptionUtil.exception(DEPT_NOT_FOUND);
            }
            // Traverse up from parentCategory to see if selfId is an ancestor
            CmsCategoryDO tempParent = parentCategory;
            while(tempParent != null && tempParent.getParentId() != null && tempParent.getParentId() != 0) {
                if(Objects.equals(tempParent.getParentId(), selfId)) {
                     throw ServiceExceptionUtil.exception(DEPT_PARENT_IS_CHILD); // Placeholder
                }
                tempParent = categoryMapper.selectById(tempParent.getParentId());
            }
        }
    }

    private void validateCategoryNameUnique(String name, Long parentId, Long selfId) {
        // In this CMS context, name uniqueness might be global or per parent.
        // Assuming name is unique within the same parent category and tenant.
        // If parentId is null or 0, it means root level.
        Long effectiveParentId = (parentId == null) ? 0L : parentId; // Normalize parentId for query consistency if 0 means root

        // TODO: This selectByNameAndTenantId might need adjustment if parentId is part of uniqueness
        // CmsCategoryDO categoryWithName = categoryMapper.selectByNameAndParentIdAndTenantId(name, effectiveParentId, TenantContextHolder.getTenantId());
        CmsCategoryDO categoryWithName = categoryMapper.selectByNameAndTenantId(name, null); // Placeholder: Needs tenantId and potentially parentId

        if (categoryWithName != null && !Objects.equals(categoryWithName.getId(), selfId)) {
            throw ServiceExceptionUtil.exception(DEPT_NAME_DUPLICATE); // Placeholder
        }
    }

    private void validateCategorySlugUnique(String slug, Long selfId) {
        // Slugs are usually unique across all categories within a tenant, or even globally.
        // Assuming unique per tenant for now.
        CmsCategoryDO categoryWithSlug = categoryMapper.selectBySlug(slug, null); // TODO: tenantId

        if (categoryWithSlug != null && !Objects.equals(categoryWithSlug.getId(), selfId)) {
            // throw ServiceExceptionUtil.exception(CMS_CATEGORY_SLUG_DUPLICATE); // Define this error code
            throw ServiceExceptionUtil.exception(DEPT_NAME_DUPLICATE); // Placeholder for now
        }
    }

    private void validateCategoryExists(Long id) {
        if (categoryMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception(DEPT_NOT_FOUND); // Placeholder
        }
    }
}
