package cn.iocoder.yudao.module.crm.service.product;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.productcategory.CrmProductCategoryCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.productcategory.CrmProductCategoryListReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.productcategory.CrmProductCategoryUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.product.CrmProductCategoryConvert;
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
 * 产品分类 Service 实现类
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
        // TODO zange：参考 mall： ProductCategoryServiceImpl 补充下必要的参数校验；
        // 校验父分类存在
        validateParentProductCategory(createReqVO.getParentId());
        // 分类名称是否存在
        CrmProductCategoryDO dbProductCategory = productCategoryMapper.selectByName(createReqVO.getName());
        if (dbProductCategory != null) {
            return dbProductCategory.getId();
        }
        // 插入
        CrmProductCategoryDO productCategory = CrmProductCategoryConvert.INSTANCE.convert(createReqVO);
        productCategoryMapper.insert(productCategory);
        // 返回
        return productCategory.getId();
    }

    @Override
    public void updateProductCategory(CrmProductCategoryUpdateReqVO updateReqVO) {
        // TODO zange：参考 mall： ProductCategoryServiceImpl 补充下必要的参数校验；
        // 校验存在
        validateProductCategoryExists(updateReqVO.getId());
        // 校验父分类存在
        validateParentProductCategory(updateReqVO.getParentId());
        // 校验名字重复
        CrmProductCategoryDO productCategoryDO = productCategoryMapper.selectByName(updateReqVO.getName());
        if (productCategoryDO != null &&
                ObjUtil.notEqual(productCategoryDO.getId(), updateReqVO.getId())) {
            throw exception(PRODUCT_CATEGORY_EXISTS);
        }
        // 更新
        CrmProductCategoryDO updateObj = CrmProductCategoryConvert.INSTANCE.convert(updateReqVO);
        productCategoryMapper.updateById(updateObj);
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
