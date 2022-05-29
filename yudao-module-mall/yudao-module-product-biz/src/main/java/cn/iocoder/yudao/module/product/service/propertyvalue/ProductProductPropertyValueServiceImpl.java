package cn.iocoder.yudao.module.product.service.propertyvalue;

import cn.iocoder.yudao.module.product.controller.admin.propertyvalue.vo.*;
import cn.iocoder.yudao.module.product.convert.propertyvalue.ProductPropertyValueConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.propertyvalue.ProductPropertyValueDO;
import cn.iocoder.yudao.module.product.dal.mysql.propertyvalue.ProductPropertyValueMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.PROPERTY_VALUE_NOT_EXISTS;

/**
 * 规格值 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ProductProductPropertyValueServiceImpl implements ProductPropertyValueService {

    @Resource
    private ProductPropertyValueMapper productPropertyValueMapper;

    // TODO @franky：这个合并到 property 中。他们本身是在一起的哈。基本不存在只查询规格，而不查询规格值。

    @Override
    public Integer createPropertyValue(ProductPropertyValueCreateReqVO createReqVO) {
        // 插入
        ProductPropertyValueDO propertyValue = ProductPropertyValueConvert.INSTANCE.convert(createReqVO);
        productPropertyValueMapper.insert(propertyValue);
        // 返回
        return propertyValue.getId();
    }

    @Override
    public void updatePropertyValue(ProductPropertyValueUpdateReqVO updateReqVO) {
        // 校验存在
        this.validatePropertyValueExists(updateReqVO.getId());
        // 更新
        ProductPropertyValueDO updateObj = ProductPropertyValueConvert.INSTANCE.convert(updateReqVO);
        productPropertyValueMapper.updateById(updateObj);
    }

    @Override
    public void deletePropertyValue(Integer id) {
        // 校验存在
        this.validatePropertyValueExists(id);
        // 删除
        productPropertyValueMapper.deleteById(id);
    }

    private void validatePropertyValueExists(Integer id) {
        if (productPropertyValueMapper.selectById(id) == null) {
            throw exception(PROPERTY_VALUE_NOT_EXISTS);
        }
    }

    @Override
    public ProductPropertyValueDO getPropertyValue(Integer id) {
        return productPropertyValueMapper.selectById(id);
    }

    @Override
    public List<ProductPropertyValueDO> getPropertyValueList(Collection<Integer> ids) {
        return productPropertyValueMapper.selectBatchIds(ids);
    }

    @Override
    public void batchInsert(List<ProductPropertyValueDO> propertyValues) {
        productPropertyValueMapper.insertBatch(propertyValues);
    }

    @Override
    public List<ProductPropertyValueDO> getPropertyValueListByPropertyId(List<Long> propertyIds) {
        return productPropertyValueMapper.getPropertyValueListByPropertyId(propertyIds);
    }

    @Override
    public void deletePropertyValueByPropertyId(Long propertyId) {
        productPropertyValueMapper.deletePropertyValueByPropertyId(propertyId);
    }

}
