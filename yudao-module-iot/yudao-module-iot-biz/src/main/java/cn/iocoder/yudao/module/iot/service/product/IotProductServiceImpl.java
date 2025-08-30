package cn.iocoder.yudao.module.iot.service.product;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.product.IotProductMapper;
import cn.iocoder.yudao.module.iot.dal.redis.RedisKeyConstants;
import cn.iocoder.yudao.module.iot.enums.product.IotProductStatusEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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

    @Resource
    private IotDevicePropertyService devicePropertyDataService;
    @Resource
    private IotDeviceService deviceService;

    @Override
    public Long createProduct(IotProductSaveReqVO createReqVO) {
        // 1. 校验 ProductKey
        if (productMapper.selectByProductKey(createReqVO.getProductKey()) != null) {
            throw exception(PRODUCT_KEY_EXISTS);
        }

        // 2. 插入
        IotProductDO product = BeanUtils.toBean(createReqVO, IotProductDO.class)
                .setStatus(IotProductStatusEnum.UNPUBLISHED.getStatus());
        productMapper.insert(product);
        return product.getId();
    }

    @Override
    @CacheEvict(value = RedisKeyConstants.PRODUCT, key = "#updateReqVO.id")
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
    @CacheEvict(value = RedisKeyConstants.PRODUCT, key = "#id")
    public void deleteProduct(Long id) {
        // 1.1 校验存在
        IotProductDO product = validateProductExists(id);
        // 1.2 发布状态不可删除
        validateProductStatus(product);
        // 1.3 校验是否有设备
        if (deviceService.getDeviceCountByProductId(id) > 0) {
            throw exception(PRODUCT_DELETE_FAIL_HAS_DEVICE);
        }

        // 2. 删除
        productMapper.deleteById(id);
    }

    @Override
    public IotProductDO validateProductExists(Long id) {
        IotProductDO product = productMapper.selectById(id);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        return product;
    }

    @Override
    public IotProductDO validateProductExists(String productKey) {
        IotProductDO product = productMapper.selectByProductKey(productKey);
        if (product == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        return product;
    }

    private void validateProductStatus(IotProductDO product) {
        if (Objects.equals(product.getStatus(), IotProductStatusEnum.PUBLISHED.getStatus())) {
            throw exception(PRODUCT_STATUS_NOT_DELETE);
        }
    }

    @Override
    public IotProductDO getProduct(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    @Cacheable(value = RedisKeyConstants.PRODUCT, key = "#id", unless = "#result == null")
    @TenantIgnore // 忽略租户信息
    public IotProductDO getProductFromCache(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public IotProductDO getProductByProductKey(String productKey) {
        return productMapper.selectByProductKey(productKey);
    }

    @Override
    public PageResult<IotProductDO> getProductPage(IotProductPageReqVO pageReqVO) {
        return productMapper.selectPage(pageReqVO);
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisKeyConstants.PRODUCT, key = "#id")
    public void updateProductStatus(Long id, Integer status) {
        // 1. 校验存在
        validateProductExists(id);

        // 2. 更新为发布状态，需要创建产品超级表数据模型
        // TODO @芋艿：【待定 001】1）是否需要操作后，在 redis 进行缓存，实现一个“快照”的情况，类似 tl；
        if (Objects.equals(status, IotProductStatusEnum.PUBLISHED.getStatus())) {
            devicePropertyDataService.defineDevicePropertyData(id);
        }

        // 3. 更新
        IotProductDO updateObj = IotProductDO.builder().id(id).status(status).build();
        productMapper.updateById(updateObj);
    }

    @Override
    public List<IotProductDO> getProductList() {
        return productMapper.selectList();
    }

    @Override
    public Long getProductCount(LocalDateTime createTime) {
        return productMapper.selectCountByCreateTime(createTime);
    }

    @Override
    public void validateProductsExist(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<IotProductDO> products = productMapper.selectByIds(ids);
        if (products.size() != ids.size()) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
    }

}