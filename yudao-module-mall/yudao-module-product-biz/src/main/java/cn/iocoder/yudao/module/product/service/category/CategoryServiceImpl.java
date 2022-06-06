package cn.iocoder.yudao.module.product.service.category;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.category.vo.*;
import cn.iocoder.yudao.module.product.convert.category.CategoryConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.category.CategoryDO;
import cn.iocoder.yudao.module.product.dal.mysql.category.CategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

/**
 * 商品分类 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CategoryServiceImpl implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public Long createCategory(CategoryCreateReqVO createReqVO) {
        // 校验父分类存在
        this.validateCategoryExists(createReqVO.getParentId(), CATEGORY_PARENT_NOT_EXISTS);
        // 插入
        CategoryDO category = CategoryConvert.INSTANCE.convert(createReqVO);
        categoryMapper.insert(category);
        // 返回
        return category.getId();
    }

    @Override
    public void updateCategory(CategoryUpdateReqVO updateReqVO) {
        // 校验父分类存在
        this.validateCategoryExists(updateReqVO.getParentId(), CATEGORY_PARENT_NOT_EXISTS);
        // 校验分类是否存在
        this.validateCategoryExists(updateReqVO.getId(), CATEGORY_NOT_EXISTS);
        // 更新
        CategoryDO updateObj = CategoryConvert.INSTANCE.convert(updateReqVO);
        categoryMapper.updateById(updateObj);
    }

    @Override
    public void deleteCategory(Long id) {
        // TODO 芋艿 补充只有不存在商品才可以删除
        // 校验分类是否存在
        CategoryDO categoryDO = this.validateCategoryExists(id, CATEGORY_NOT_EXISTS);
        // 校验是否还有子分类
        if (categoryMapper.selectCountByParentId(categoryDO.getParentId()) > 0) {
            throw ServiceExceptionUtil.exception(CATEGORY_EXISTS_CHILDREN);
        }
        // 删除
        categoryMapper.deleteById(id);
    }

    private CategoryDO validateCategoryExists(Long id, ErrorCode errorCode) {
        // TODO franky：0 要枚举哈
        if (id == 0) {
            return new CategoryDO().setId(id);
        }
        CategoryDO categoryDO = categoryMapper.selectById(id);
        if (categoryDO == null) {
            throw exception(errorCode);
        }
        return categoryDO;
    }

    @Override
    public void validatedCategoryById(Long categoryId) {
        this.validateCategoryExists(categoryId, CATEGORY_NOT_EXISTS);
    }

    @Override
    public CategoryDO getCategory(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public List<CategoryDO> getCategoryList(Collection<Long> ids) {
        return categoryMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CategoryDO> getCategoryPage(CategoryPageReqVO pageReqVO) {
        return categoryMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CategoryDO> getCategoryList(CategoryExportReqVO exportReqVO) {
        return categoryMapper.selectList(exportReqVO);
    }

    @Override
    public List<CategoryDO> getCategoryTreeList(CategoryTreeListReqVO treeListReqVO) {
        return categoryMapper.selectList(treeListReqVO);
    }

}
