package cn.iocoder.yudao.module.iot.service.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.ProductPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.ProductSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.ProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.product.ProductMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;

/**
 * IOT 产品 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductMapper productMapper;

    @Override
    public Long createProduct(ProductSaveReqVO createReqVO) {
        // 插入
        ProductDO product = BeanUtils.toBean(createReqVO, ProductDO.class);
        productMapper.insert(product);
        // 返回
        return product.getId();
    }

    @Override
    public void updateProduct(ProductSaveReqVO updateReqVO) {
        // 校验存在
        validateProductExists(updateReqVO.getId());
        // 更新
        ProductDO updateObj = BeanUtils.toBean(updateReqVO, ProductDO.class);
        productMapper.updateById(updateObj);
    }

    @Override
    public void deleteProduct(Long id) {
        // 校验存在
        validateProductExists(id);
        // 删除
        productMapper.deleteById(id);
    }

    private void validateProductExists(Long id) {
        if (productMapper.selectById(id) == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
    }

    @Override
    public ProductDO getProduct(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public PageResult<ProductDO> getProductPage(ProductPageReqVO pageReqVO) {
        return productMapper.selectPage(pageReqVO);
    }

}