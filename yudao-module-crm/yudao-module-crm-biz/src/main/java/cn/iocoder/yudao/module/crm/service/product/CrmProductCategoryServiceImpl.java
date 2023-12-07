package cn.iocoder.yudao.module.crm.service.product;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.category.CrmProductCategoryCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.category.CrmProductCategoryListReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductCategoryDO;
import cn.iocoder.yudao.module.crm.dal.mysql.product.CrmProductCategoryMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductCategoryDO.PARENT_ID_NULL;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;

/**
 * CRM 产品分类 Service 实现类
 *
 * @author ZanGe丶
 */
@Service
@Validated
public class CrmProductCategoryServiceImpl implements CrmProductCategoryService {

    @Resource(name = "crmProductCategoryMapper")
    private CrmProductCategoryMapper productCategoryMapper;

    @Resource
    @Lazy // 延迟加载，解决循环依赖问题
    private CrmProductService crmProductService;

    @Override
    public Long createProductCategory(CrmProductCategoryCreateReqVO createReqVO) {
        // 1.1 校验父分类存在
        validateParentProductCategory(createReqVO.getParentId());
        // 1.2 分类名称是否存在
        validateProductNameExists(null, createReqVO.getParentId(), createReqVO.getName());
        // 2. 插入
        CrmProductCategoryDO category = BeanUtils.toBean(createReqVO, CrmProductCategoryDO.class);
        productCategoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public void updateProductCategory(CrmProductCategoryCreateReqVO updateReqVO) {
        // 1.1 校验存在
        validateProductCategoryExists(updateReqVO.getId());
        // 1.2 校验父分类存在
        validateParentProductCategory(updateReqVO.getParentId());
        // 1.3 分类名称是否存在
        validateProductNameExists(updateReqVO.getId(), updateReqVO.getParentId(), updateReqVO.getName());
        // 2. 更新
        CrmProductCategoryDO updateObj = BeanUtils.toBean(updateReqVO, CrmProductCategoryDO.class);
        productCategoryMapper.updateById(updateObj);
    }

    private void validateProductCategoryExists(Long id) {
        if (productCategoryMapper.selectById(id) == null) {
            throw exception(PRODUCT_CATEGORY_NOT_EXISTS);
        }
    }

    private void validateParentProductCategory(Long id) {
        // 如果是根分类，无需验证
        if (Objects.equals(id, PARENT_ID_NULL)) {
            return;
        }
        // 父分类不存在
        CrmProductCategoryDO category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw exception(PRODUCT_CATEGORY_PARENT_NOT_EXISTS);
        }
        // 父分类不能是二级分类
        if (!Objects.equals(category.getParentId(), PARENT_ID_NULL)) {
            throw exception(PRODUCT_CATEGORY_PARENT_NOT_FIRST_LEVEL);
        }
    }

    private void validateProductNameExists(Long id, Long parentId, String name) {
        CrmProductCategoryDO category = productCategoryMapper.selectByParentIdAndName(parentId, name);
        if (category == null
            || category.getId().equals(id)) {
            return;
        }
        throw exception(PRODUCT_CATEGORY_EXISTS);
    }

    @Override
    public void deleteProductCategory(Long id) {
        // TODO zange：参考 mall： ProductCategoryServiceImpl 补充下必要的参数校验；
        // 校验存在
        validateProductCategoryExists(id);
        // 校验是否还有子分类
        if (productCategoryMapper.selectCountByParentId(id) > 0) {
            throw exception(product_CATEGORY_EXISTS_CHILDREN);
        }
        // 校验是否被产品使用
        if (crmProductService.getProductByCategoryId(id) !=null) {
            throw exception(PRODUCT_CATEGORY_USED);
        }
        // 删除
        productCategoryMapper.deleteById(id);
    }

    @Override
    public CrmProductCategoryDO getProductCategory(Long id) {
        return productCategoryMapper.selectById(id);
    }

    @Override
    public List<CrmProductCategoryDO> getProductCategoryList(CrmProductCategoryListReqVO listReqVO) {
        return productCategoryMapper.selectList(listReqVO);
    }

    @Override
    public List<CrmProductCategoryDO> getProductCategoryList(Collection<Long> ids) {
        return productCategoryMapper.selectBatchIds(ids);
    }

}
