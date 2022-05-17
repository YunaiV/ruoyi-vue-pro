package cn.iocoder.yudao.module.product.service.property;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.property.PropertyDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.product.convert.property.PropertyConvert;
import cn.iocoder.yudao.module.product.dal.mysql.property.PropertyMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

/**
 * 规格名称 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PropertyServiceImpl implements PropertyService {

    @Resource
    private PropertyMapper propertyMapper;

    @Override
    public Long createProperty(PropertyCreateReqVO createReqVO) {
        // 插入
        PropertyDO property = PropertyConvert.INSTANCE.convert(createReqVO);
        propertyMapper.insert(property);
        // 返回
        return property.getId();
    }

    @Override
    public void updateProperty(PropertyUpdateReqVO updateReqVO) {
        // 校验存在
        this.validatePropertyExists(updateReqVO.getId());
        // 更新
        PropertyDO updateObj = PropertyConvert.INSTANCE.convert(updateReqVO);
        propertyMapper.updateById(updateObj);
    }

    @Override
    public void deleteProperty(Long id) {
        // 校验存在
        this.validatePropertyExists(id);
        // 删除
        propertyMapper.deleteById(id);
    }

    private void validatePropertyExists(Long id) {
        if (propertyMapper.selectById(id) == null) {
            throw exception(PROPERTY_NOT_EXISTS);
        }
    }

    @Override
    public PropertyDO getProperty(Long id) {
        return propertyMapper.selectById(id);
    }

    @Override
    public List<PropertyDO> getPropertyList(Collection<Long> ids) {
        return propertyMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<PropertyDO> getPropertyPage(PropertyPageReqVO pageReqVO) {
        return propertyMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PropertyDO> getPropertyList(PropertyExportReqVO exportReqVO) {
        return propertyMapper.selectList(exportReqVO);
    }

}
