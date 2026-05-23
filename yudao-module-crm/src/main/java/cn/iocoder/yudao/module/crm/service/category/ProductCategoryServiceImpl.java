package cn.iocoder.yudao.module.crm.service.category;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.category.vo.ProductCategoryPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.category.vo.ProductCategorySaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.crm.dal.mysql.category.ProductCategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;

/**
 * 商品分类 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ProductCategoryServiceImpl implements ProductCategoryService {

    @Resource
    private ProductCategoryMapper categoryMapper;

    @Override
    public Long createCategory(ProductCategorySaveReqVO createReqVO) {
        // 校验分类名称唯一性
        validateCategoryNameUnique(null, createReqVO.getName());

        // 插入分类
        ProductCategoryDO category = BeanUtils.toBean(createReqVO, ProductCategoryDO.class);
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public void updateCategory(ProductCategorySaveReqVO updateReqVO) {
        // 校验分类存在
        validateCategoryExists(updateReqVO.getId());
        // 校验分类名称唯一性
        validateCategoryNameUnique(updateReqVO.getId(), updateReqVO.getName());

        // 更新分类
        ProductCategoryDO updateObj = BeanUtils.toBean(updateReqVO, ProductCategoryDO.class);
        categoryMapper.updateById(updateObj);
    }

    @Override
    public void deleteCategory(Long id) {
        // 校验分类存在
        validateCategoryExists(id);

        // 校验是否有子分类
        List<ProductCategoryDO> children = categoryMapper.selectListByParentId(id);
        if (!children.isEmpty()) {
            throw exception(CATEGORY_HAS_CHILDREN);
        }

        // 删除分类
        categoryMapper.deleteById(id);
    }

    @Override
    public ProductCategoryDO getCategory(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public PageResult<ProductCategoryDO> getCategoryPage(ProductCategoryPageReqVO pageReqVO) {
        return categoryMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ProductCategoryDO> getCategoryListByParentId(Long parentId) {
        return categoryMapper.selectListByParentId(parentId);
    }

    /**
     * 校验分类是否存在
     *
     * @param id 分类ID
     * @return 分类对象
     */
    private ProductCategoryDO validateCategoryExists(Long id) {
        ProductCategoryDO category = categoryMapper.selectById(id);
        if (category == null) {
            throw exception(CATEGORY_NOT_EXISTS);
        }
        return category;
    }

    /**
     * 校验分类名称唯一性
     *
     * @param id   分类ID
     * @param name 分类名称
     */
    private void validateCategoryNameUnique(Long id, String name) {
        if (name == null) {
            return;
        }
        ProductCategoryDO category = categoryMapper.selectByName(name);
        if (category == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的分类
        if (id == null) {
            throw exception(CATEGORY_NAME_EXISTS);
        }
        if (!category.getId().equals(id)) {
            throw exception(CATEGORY_NAME_EXISTS);
        }
    }

}
