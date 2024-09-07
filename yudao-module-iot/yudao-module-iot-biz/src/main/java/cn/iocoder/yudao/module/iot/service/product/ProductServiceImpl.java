package cn.iocoder.yudao.module.iot.service.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.ProductPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.ProductSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.ProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.product.ProductMapper;
import cn.iocoder.yudao.module.iot.enums.product.IotProductStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import java.util.UUID;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.PRODUCT_STATUS_NOT_DELETE;

/**
 * IOT 产品 Service 实现类
 *
 * @author ahh
 */
@Service
@Validated
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductMapper productMapper;

    @Override
    public Long createProduct(ProductSaveReqVO createReqVO) {
        // 生成 ProductKey
        createProductKey(createReqVO);
        // 插入
        ProductDO product = BeanUtils.toBean(createReqVO, ProductDO.class);
        productMapper.insert(product);
        return product.getId();
    }

    /**
     * 创建 ProductKey
     *
     * @param createReqVO 创建信息
     */
    private void createProductKey(ProductSaveReqVO createReqVO) {
        // TODO @haohao：应该前端没传递的时候，才生成哇？ps：需要校验下唯一性，万一有重复；
        // 生成随机的 11 位字符串
        String productKey = UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        createReqVO.setProductKey(productKey);
    }

    @Override
    public void updateProduct(ProductSaveReqVO updateReqVO) {
        updateReqVO.setProductKey(null); // 不更新产品标识
        // 校验存在
        validateProductExists(updateReqVO.getId());
        // TODO @haohao：如果已经发布，允许编辑么？
        // 更新
        ProductDO updateObj = BeanUtils.toBean(updateReqVO, ProductDO.class);
        productMapper.updateById(updateObj);
    }

    @Override
    public void deleteProduct(Long id) {
        // TODO @haohao：这里最好只查询一次哈
        // 1.1 校验存在
        validateProductExists(id);
        // 1.2 发布状态不可删除
        validateProductStatus(id);
        // 2. 删除
        productMapper.deleteById(id);
    }

    private void validateProductExists(Long id) {
        if (productMapper.selectById(id) == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
    }

    private void validateProductStatus(Long id) {
        ProductDO product = productMapper.selectById(id);
        if (Objects.equals(product.getStatus(), IotProductStatusEnum.PUBLISHED.getType())) {
            throw exception(PRODUCT_STATUS_NOT_DELETE);
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

    @Override
    public void updateProductStatus(Long id, Integer status) {
        // 校验存在
        validateProductExists(id);
        // 更新
        ProductDO updateObj = ProductDO.builder().id(id).status(status).build();
        productMapper.updateById(updateObj);
    }

}