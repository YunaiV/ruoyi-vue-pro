package cn.iocoder.yudao.module.iot.service.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.product.IotProductMapper;
import cn.iocoder.yudao.module.iot.enums.product.IotProductStatusEnum;
import cn.iocoder.yudao.module.iot.service.productthingmodel.IotProductThingModelService;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

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
    @Lazy
    private IotProductThingModelService thinkModelFunctionService;

    @Override
    public Long createProduct(IotProductSaveReqVO createReqVO) {
        // 1. 生成 ProductKey
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
    public PageResult<IotProductDO> getProductPage(IotProductPageReqVO pageReqVO) {
        return productMapper.selectPage(pageReqVO);
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void updateProductStatus(Long id, Integer status) {
        // 1. 校验存在
        validateProductExists(id);
        // 2. 更新
        IotProductDO updateObj = IotProductDO.builder().id(id).status(status).build();
        // 3. 产品是发布状态
        if (Objects.equals(status, IotProductStatusEnum.PUBLISHED.getStatus())) {
            // 3.1 创建超级表数据模型
            thinkModelFunctionService.createSuperTableDataModel(id);
        }
        productMapper.updateById(updateObj);
    }

    @Override
    public List<IotProductDO> getProductList() {
        return productMapper.selectList();
    }

}