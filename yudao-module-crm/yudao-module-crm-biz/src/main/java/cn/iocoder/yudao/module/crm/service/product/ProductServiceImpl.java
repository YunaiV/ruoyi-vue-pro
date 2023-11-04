package cn.iocoder.yudao.module.crm.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.ProductCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.ProductExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.ProductPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.ProductUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.product.ProductConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.ProductDO;
import cn.iocoder.yudao.module.crm.dal.mysql.product.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.PRODUCT_NO_EXISTS;

/**
 * 产品 Service 实现类
 *
 * @author ZanGe丶
 */
@Service
@Validated
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductMapper productMapper;

    @Override
    public Long createProduct(ProductCreateReqVO createReqVO) {
        //校验产品编号是否存在
        validateProductNo(createReqVO.getNo());
        // 插入
        ProductDO product = ProductConvert.INSTANCE.convert(createReqVO);
        productMapper.insert(product);
        // 返回
        return product.getId();
    }

    @Override
    public void updateProduct(ProductUpdateReqVO updateReqVO) {
        // 校验存在
        validateProductExists(updateReqVO.getId(), updateReqVO.getNo());
        // 更新
        ProductDO updateObj = ProductConvert.INSTANCE.convert(updateReqVO);
        productMapper.updateById(updateObj);
    }

    @Override
    public void deleteProduct(Long id) {
        // 校验存在
        validateProductExists(id, null);
        // 删除
        productMapper.deleteById(id);
    }

    private void validateProductExists(Long id, String no) {
        ProductDO product = productMapper.selectById(id);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        if (no != null && no.equals(product.getNo())) {
            throw exception(PRODUCT_NO_EXISTS);
        }
    }

    @Override
    public ProductDO getProduct(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public List<ProductDO> getProductList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return productMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ProductDO> getProductPage(ProductPageReqVO pageReqVO) {
        return productMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ProductDO> getProductList(ProductExportReqVO exportReqVO) {
        return productMapper.selectList(exportReqVO);
    }

    private void validateProductNo(String no) {
        ProductDO product = productMapper.selectOne(new LambdaQueryWrapper<ProductDO>().eq(ProductDO::getNo, no));
        if (product != null) {
            throw exception(PRODUCT_NO_EXISTS);
        }
    }
}
