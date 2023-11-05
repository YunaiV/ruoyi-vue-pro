package cn.iocoder.yudao.module.crm.service.productcategory;

import cn.iocoder.yudao.module.crm.controller.admin.productcategory.vo.ProductCategoryCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.productcategory.vo.ProductCategoryListReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.productcategory.vo.ProductCategoryUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.productcategory.ProductCategoryConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.productcategory.ProductCategoryDO;
import cn.iocoder.yudao.module.crm.dal.mysql.productcategory.ProductCategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.PRODUCT_CATEGORY_NOT_EXISTS;

// TODO @zange：这个类所在的包，放到 product 下；
/**
 * 产品分类 Service 实现类
 *
 * @author ZanGe丶
 */
@Service
@Validated
public class ProductCategoryServiceImpl implements ProductCategoryService {

    @Resource
    private ProductCategoryMapper productCategoryMapper;

    @Override
    public Long createProductCategory(ProductCategoryCreateReqVO createReqVO) {
        // TODO zange：参考 mall： ProductCategoryServiceImpl 补充下必要的参数校验；
        // 插入
        ProductCategoryDO productCategory = ProductCategoryConvert.INSTANCE.convert(createReqVO);
        productCategoryMapper.insert(productCategory);
        // 返回
        return productCategory.getId();
    }

    @Override
    public void updateProductCategory(ProductCategoryUpdateReqVO updateReqVO) {
        // TODO zange：参考 mall： ProductCategoryServiceImpl 补充下必要的参数校验；
        // 校验存在
        validateProductCategoryExists(updateReqVO.getId());
        // 更新
        ProductCategoryDO updateObj = ProductCategoryConvert.INSTANCE.convert(updateReqVO);
        productCategoryMapper.updateById(updateObj);
    }

    @Override
    public void deleteProductCategory(Long id) {
        // TODO zange：参考 mall： ProductCategoryServiceImpl 补充下必要的参数校验；
        // 校验存在
        validateProductCategoryExists(id);
        // 删除
        productCategoryMapper.deleteById(id);
    }

    private void validateProductCategoryExists(Long id) {
        if (productCategoryMapper.selectById(id) == null) {
            throw exception(PRODUCT_CATEGORY_NOT_EXISTS);
        }
    }

    @Override
    public ProductCategoryDO getProductCategory(Long id) {
        return productCategoryMapper.selectById(id);
    }

    @Override
    public List<ProductCategoryDO> getProductCategoryList(ProductCategoryListReqVO treeListReqVO) {
        return productCategoryMapper.selectList(treeListReqVO);
    }

}
