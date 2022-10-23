package cn.iocoder.yudao.module.product.service.category;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.product.controller.admin.category.vo.ProductCategoryCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.category.vo.ProductCategoryListReqVO;
import cn.iocoder.yudao.module.product.controller.admin.category.vo.ProductCategoryUpdateReqVO;
import cn.iocoder.yudao.module.product.convert.category.ProductCategoryConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.mysql.category.ProductCategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

/**
 * 商品分类 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ProductCategoryServiceImpl implements ProductCategoryService {

    @Resource
    private ProductCategoryMapper productCategoryMapper;

    @Override
    public Long createCategory(ProductCategoryCreateReqVO createReqVO) {
        // 校验父分类存在
        validateParentProductCategory(createReqVO.getParentId());

        // 插入
        ProductCategoryDO category = ProductCategoryConvert.INSTANCE.convert(createReqVO);
        productCategoryMapper.insert(category);
        // 返回
        return category.getId();
    }

    @Override
    public void updateCategory(ProductCategoryUpdateReqVO updateReqVO) {
        // 校验分类是否存在
        validateProductCategoryExists(updateReqVO.getId());
        // 校验父分类存在
        validateParentProductCategory(updateReqVO.getParentId());

        // 更新
        ProductCategoryDO updateObj = ProductCategoryConvert.INSTANCE.convert(updateReqVO);
        productCategoryMapper.updateById(updateObj);
    }

    @Override
    public void deleteCategory(Long id) {
        // 校验分类是否存在
        validateProductCategoryExists(id);
        // 校验是否还有子分类
        if (productCategoryMapper.selectCountByParentId(id) > 0) {
            throw exception(CATEGORY_EXISTS_CHILDREN);
        }
        // TODO 芋艿 补充只有不存在商品才可以删除
        // 删除
        productCategoryMapper.deleteById(id);
    }

    private void validateParentProductCategory(Long id) {
        // 如果是根分类，无需验证
        if (Objects.equals(id, ProductCategoryDO.PARENT_ID_NULL)) {
            return;
        }
        // 父分类不存在
        ProductCategoryDO category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw exception(CATEGORY_PARENT_NOT_EXISTS);
        }
        // 父分类不能是二级分类
        if (Objects.equals(id, ProductCategoryDO.PARENT_ID_NULL)) {
            throw exception(CATEGORY_PARENT_NOT_FIRST_LEVEL);
        }
    }

    private void validateProductCategoryExists(Long id) {
        ProductCategoryDO category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw exception(CATEGORY_NOT_EXISTS);
        }
    }

    @Override
    public void validateCategoryLevel(Long id) {
        // TODO @芋艿：在看看，杂能优化下
        Long parentId = id;
        int i = 2;
        for (; i >= 0; --i) {
            ProductCategoryDO category = productCategoryMapper.selectById(parentId);
            parentId = category.getParentId();
            if(Objects.equals(parentId, ProductCategoryDO.PARENT_ID_NULL)){
                break;
            }
        }
        if (!Objects.equals(parentId, ProductCategoryDO.PARENT_ID_NULL) || i != 0) {
            throw exception(CATEGORY_LEVEL_ERROR);
        }
    }

    @Override
    public ProductCategoryDO getCategory(Long id) {
        return productCategoryMapper.selectById(id);
    }

    @Override
    public List<ProductCategoryDO> getEnableCategoryList(Collection<Long> ids) {
        return productCategoryMapper.selectBatchIds(ids);
    }

    @Override
    public List<ProductCategoryDO> getEnableCategoryList(ProductCategoryListReqVO listReqVO) {
        return productCategoryMapper.selectList(listReqVO);
    }

    @Override
    public List<ProductCategoryDO> getEnableCategoryList() {
        return productCategoryMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

}
