package cn.iocoder.yudao.module.product.service.property;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValuePageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueUpdateReqVO;
import cn.iocoder.yudao.module.product.convert.propertyvalue.ProductPropertyValueConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyDO;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyValueDO;
import cn.iocoder.yudao.module.product.dal.mysql.property.ProductPropertyValueMapper;
import cn.iocoder.yudao.module.product.service.property.bo.ProductPropertyValueDetailRespBO;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.PROPERTY_VALUE_EXISTS;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.PROPERTY_VALUE_NOT_EXISTS;

/**
 * 商品属性值 Service 实现类
 *
 * @author LuoWenFeng
 */
@Service
@Validated
public class ProductPropertyValueServiceImpl implements ProductPropertyValueService {

    @Resource
    private ProductPropertyValueMapper productPropertyValueMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private ProductPropertyService productPropertyService;

    @Override
    public Long createPropertyValue(ProductPropertyValueCreateReqVO createReqVO) {
        // 如果已经添加过该属性值，直接返回
        ProductPropertyValueDO dbValue = productPropertyValueMapper.selectByName(
                createReqVO.getPropertyId(), createReqVO.getName());
        if (dbValue != null) {
            return dbValue.getId();
        }

        // 新增
        ProductPropertyValueDO value = ProductPropertyValueConvert.INSTANCE.convert(createReqVO);
        productPropertyValueMapper.insert(value);
        return value.getId();
    }

    @Override
    public void updatePropertyValue(ProductPropertyValueUpdateReqVO updateReqVO) {
        validatePropertyValueExists(updateReqVO.getId());
        // 校验名字唯一
        ProductPropertyValueDO productPropertyValueDO = productPropertyValueMapper.selectByName
                (updateReqVO.getPropertyId(), updateReqVO.getName());
        if (productPropertyValueDO != null && !productPropertyValueDO.getId().equals(updateReqVO.getId())) {
            throw exception(PROPERTY_VALUE_EXISTS);
        }

        // 更新
        ProductPropertyValueDO updateObj = ProductPropertyValueConvert.INSTANCE.convert(updateReqVO);
        productPropertyValueMapper.updateById(updateObj);
    }

    @Override
    public void deletePropertyValue(Long id) {
        validatePropertyValueExists(id);
        productPropertyValueMapper.deleteById(id);
    }

    private void validatePropertyValueExists(Long id) {
        if (productPropertyValueMapper.selectById(id) == null) {
            throw exception(PROPERTY_VALUE_NOT_EXISTS);
        }
    }

    @Override
    public ProductPropertyValueDO getPropertyValue(Long id) {
        return productPropertyValueMapper.selectById(id);
    }

    @Override
    public List<ProductPropertyValueDO> getPropertyValueListByPropertyId(Collection<Long> propertyIds) {
        return productPropertyValueMapper.selectListByPropertyId(propertyIds);
    }

    @Override
    public List<ProductPropertyValueDetailRespBO> getPropertyValueDetailList(Collection<Long> ids) {
        // 获得属性值列表
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ProductPropertyValueDO> values = productPropertyValueMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(values)) {
            return Collections.emptyList();
        }
        // 获得属性项列表
        List<ProductPropertyDO> keys = productPropertyService.getPropertyList(
                convertSet(values, ProductPropertyValueDO::getPropertyId));
        // 组装明细
        return ProductPropertyValueConvert.INSTANCE.convertList(values, keys);
    }

    @Override
    public Integer getPropertyValueCountByPropertyId(Long propertyId) {
        return productPropertyValueMapper.selectCountByPropertyId(propertyId);
    }

    @Override
    public PageResult<ProductPropertyValueDO> getPropertyValuePage(ProductPropertyValuePageReqVO pageReqVO) {
        return productPropertyValueMapper.selectPage(pageReqVO);
    }

    @Override
    public void deletePropertyValueByPropertyId(Long propertyId) {
        productPropertyValueMapper.deleteByPropertyId(propertyId);
    }

}
