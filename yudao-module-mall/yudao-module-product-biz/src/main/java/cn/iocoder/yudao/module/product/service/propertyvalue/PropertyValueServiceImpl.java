package cn.iocoder.yudao.module.product.service.propertyvalue;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.propertyvalue.vo.*;
import cn.iocoder.yudao.module.product.convert.propertyvalue.PropertyValueConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.propertyvalue.PropertyValueDO;
import cn.iocoder.yudao.module.product.dal.mysql.propertyvalue.PropertyValueMapper;
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
public class PropertyValueServiceImpl implements PropertyValueService {

    @Resource
    private PropertyValueMapper propertyValueMapper;

    // TODO @franky：这个合并到 property 中。他们本身是在一起的哈。基本不存在只查询规格，而不查询规格值。

    @Override
    public Integer createPropertyValue(PropertyValueCreateReqVO createReqVO) {
        // 插入
        PropertyValueDO propertyValue = PropertyValueConvert.INSTANCE.convert(createReqVO);
        propertyValueMapper.insert(propertyValue);
        // 返回
        return propertyValue.getId();
    }

    @Override
    public void updatePropertyValue(PropertyValueUpdateReqVO updateReqVO) {
        // 校验存在
        this.validatePropertyValueExists(updateReqVO.getId());
        // 更新
        PropertyValueDO updateObj = PropertyValueConvert.INSTANCE.convert(updateReqVO);
        propertyValueMapper.updateById(updateObj);
    }

    @Override
    public void deletePropertyValue(Integer id) {
        // 校验存在
        this.validatePropertyValueExists(id);
        // 删除
        propertyValueMapper.deleteById(id);
    }

    private void validatePropertyValueExists(Integer id) {
        if (propertyValueMapper.selectById(id) == null) {
            throw exception(PROPERTY_VALUE_NOT_EXISTS);
        }
    }

    @Override
    public PropertyValueDO getPropertyValue(Integer id) {
        return propertyValueMapper.selectById(id);
    }

    @Override
    public List<PropertyValueDO> getPropertyValueList(Collection<Integer> ids) {
        return propertyValueMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<PropertyValueDO> getPropertyValuePage(PropertyValuePageReqVO pageReqVO) {
        return propertyValueMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PropertyValueDO> getPropertyValueList(PropertyValueExportReqVO exportReqVO) {
        return propertyValueMapper.selectList(exportReqVO);
    }

    @Override
    public void batchInsert(List<PropertyValueDO> propertyValues) {
        propertyValueMapper.insertBatch(propertyValues);
    }

    @Override
    public List<PropertyValueDO> getPropertyValueListByPropertyId(List<Long> propertyIds) {
        return propertyValueMapper.getPropertyValueListByPropertyId(propertyIds);
    }

    @Override
    public void deletePropertyValueByPropertyId(Long propertyId) {
        propertyValueMapper.deletePropertyValueByPropertyId(propertyId);
    }

}
