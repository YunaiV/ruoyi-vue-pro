package cn.iocoder.yudao.module.crm.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.product.CrmProductCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.product.CrmProductExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.product.CrmProductPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.product.CrmProductUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.product.CrmProductConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductCategoryDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductDO;
import cn.iocoder.yudao.module.crm.dal.mysql.product.CrmProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;

/**
 * 产品 Service 实现类
 *
 * @author ZanGe丶
 */
@Service
@Validated
public class CrmProductServiceImpl implements CrmProductService {

    @Resource
    private CrmProductMapper productMapper;
    @Resource
    private CrmProductCategoryService productCategoryService;

    @Override
    public Long createProduct(CrmProductCreateReqVO createReqVO) {
        // 校验产品编号是否存在
        validateProductNoDuplicate(createReqVO.getNo());
        // TODO @zange-ok：需要校验 categoryId 是否存在；
        validateProductCategoryExists(createReqVO.getCategoryId());
        // 插入
        CrmProductDO product = CrmProductConvert.INSTANCE.convert(createReqVO);
        productMapper.insert(product);
        // 返回
        return product.getId();
    }

    @Override
    public void updateProduct(CrmProductUpdateReqVO updateReqVO) {
        // 校验存在
        validateProductExists(updateReqVO.getId());
        // TODO @zange-ok：需要校验 categoryId 是否存在；
        validateProductCategoryExists(updateReqVO.getCategoryId());
        // 更新
        CrmProductDO updateObj = CrmProductConvert.INSTANCE.convert(updateReqVO);
        productMapper.updateById(updateObj);
    }

    @Override
    public void deleteProduct(Long id) {
        // 校验存在
        validateProductExists(id);
        // 删除
        productMapper.deleteById(id);
    }

    // TODO @zange-ok：validateProductExists 要不只校验是否存在；然后是否 no 重复，交给 validateProductNo，名字改成 validateProductNoDuplicate，和别的模块保持一致哈；
    private void validateProductExists(Long id) {
        CrmProductDO product = productMapper.selectById(id);
        if (product == null) {
            throw exception(CRM_PRODUCT_NOT_EXISTS);
        }
    }

    private void validateProductCategoryExists(Long categoryId) {
        CrmProductCategoryDO productCategory = productCategoryService.getProductCategory(categoryId);
        if (productCategory == null) {
            throw exception(CRM_PRODUCT_CATEGORY_NOT_EXISTS);
        }
    }

    @Override
    public CrmProductDO getProduct(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public List<CrmProductDO> getProductList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return productMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CrmProductDO> getProductPage(CrmProductPageReqVO pageReqVO) {
        return productMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CrmProductDO> getProductList(CrmProductExportReqVO exportReqVO) {
        return productMapper.selectList(exportReqVO);
    }

    @Override
    public CrmProductDO getProductByCategoryId(Long categoryId) {
        return productMapper.selectOne(new LambdaQueryWrapper<CrmProductDO>().eq(CrmProductDO::getCategoryId, categoryId));
    }

    private void validateProductNoDuplicate(String no) {
        CrmProductDO product = productMapper.selectOne(new LambdaQueryWrapper<CrmProductDO>().eq(CrmProductDO::getNo, no));
        if (product != null) {
            throw exception(CRM_PRODUCT_NO_EXISTS);
        }
    }

}
