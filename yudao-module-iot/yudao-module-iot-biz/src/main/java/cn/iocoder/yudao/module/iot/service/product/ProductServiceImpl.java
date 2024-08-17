package cn.iocoder.yudao.module.iot.service.product;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.ProductPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.ProductSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.ProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.product.ProductMapper;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.PRODUCT_IDENTIFICATION_EXISTS;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;

/**
 * iot 产品 Service 实现类
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
        // 不传自动生成产品标识
        createIdentification(createReqVO);
        // 校验产品标识是否重复
        validateProductIdentification(createReqVO.getIdentification());
        // 插入
        ProductDO product = BeanUtils.toBean(createReqVO, ProductDO.class);
        productMapper.insert(product);
        // 返回
        return product.getId();
    }

    private void validateProductIdentification(@NotEmpty(message = "产品标识不能为空") String identification) {
        if (productMapper.selectByIdentification(identification) != null) {
            throw exception(PRODUCT_IDENTIFICATION_EXISTS);
        }
    }

    private void createIdentification(ProductSaveReqVO createReqVO) {
        if (StrUtil.isNotBlank(createReqVO.getIdentification())) {
            return;
        }
        // 生成 19 位数字
        createReqVO.setIdentification(String.valueOf(IdUtil.getSnowflake(1, 1).nextId()));
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

    public static void main(String[] args) {
        System.out.println(String.valueOf(IdUtil.getSnowflake(1, 1).nextId()));
    }
}