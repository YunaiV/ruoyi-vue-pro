package cn.iocoder.yudao.module.iot.service.product;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.IotProductPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.IotProductSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.product.IotProductMapper;
import cn.iocoder.yudao.module.iot.enums.product.IotProductStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 产品 Service 实现类
 *
 * @author ahh
 */
@Service
@Validated
public class IotProductServiceImpl implements IotProductService {

    @Resource
    private IotProductMapper productMapper;

    @Override
    public Long createProduct(IotProductSaveReqVO createReqVO) {
        // 1. 生成 ProductKey
        createProductKey(createReqVO);
        // 2. 插入
        IotProductDO product = BeanUtils.toBean(createReqVO, IotProductDO.class);
        productMapper.insert(product);
        return product.getId();
    }

    /**
     * 创建 ProductKey
     *
     * @param createReqVO 创建信息
     */
    private void createProductKey(IotProductSaveReqVO createReqVO) {
        String productKey = createReqVO.getProductKey();
        // 1. productKey为空，生成随机的 11 位字符串
        if (StrUtil.isEmpty(productKey)) {
            productKey = UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        }
        // 2. 校验唯一性
        if (productMapper.selectByProductKey(productKey) != null) {
            throw exception(PRODUCT_IDENTIFICATION_EXISTS);
        }
        createReqVO.setProductKey(productKey);
    }

    @Override
    public void updateProduct(IotProductSaveReqVO updateReqVO) {
        updateReqVO.setProductKey(null); // 不更新产品标识
        // 1.1 校验存在
        IotProductDO iotProductDO = validateProductExists(updateReqVO.getId());
        // 1.2 发布状态不可更新
        validateProductStatus(iotProductDO);
        // 2. 更新
        IotProductDO updateObj = BeanUtils.toBean(updateReqVO, IotProductDO.class);
        productMapper.updateById(updateObj);
    }

    @Override
    public void deleteProduct(Long id) {
        // 1.1 校验存在
        IotProductDO iotProductDO = validateProductExists(id);
        // 1.2 发布状态不可删除
        validateProductStatus(iotProductDO);
        // 2. 删除
        productMapper.deleteById(id);
    }

    private IotProductDO validateProductExists(Long id) {
        IotProductDO iotProductDO = productMapper.selectById(id);
        if (iotProductDO == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        return iotProductDO;
    }

    private void validateProductStatus(IotProductDO iotProductDO) {
        if (Objects.equals(iotProductDO.getStatus(), IotProductStatusEnum.PUBLISHED.getType())) {
            throw exception(PRODUCT_STATUS_NOT_DELETE);
        }
    }

    @Override
    public IotProductDO getProduct(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public PageResult<IotProductDO> getProductPage(IotProductPageReqVO pageReqVO) {
        return productMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateProductStatus(Long id, Integer status) {
        // 1. 校验存在
        validateProductExists(id);
        // 2. 更新
        IotProductDO updateObj = IotProductDO.builder().id(id).status(status).build();
        productMapper.updateById(updateObj);
    }

    @Override
    public List<IotProductDO> listAllProducts() {
        return productMapper.selectList();
    }

}