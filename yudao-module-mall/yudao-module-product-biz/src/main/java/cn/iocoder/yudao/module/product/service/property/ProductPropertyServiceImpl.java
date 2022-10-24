package cn.iocoder.yudao.module.product.service.property;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.property.*;
import cn.iocoder.yudao.module.product.convert.property.ProductPropertyConvert;
import cn.iocoder.yudao.module.product.convert.propertyvalue.ProductPropertyValueConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyDO;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyValueDO;
import cn.iocoder.yudao.module.product.dal.mysql.property.ProductPropertyMapper;
import cn.iocoder.yudao.module.product.dal.mysql.property.ProductPropertyValueMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.PROPERTY_EXISTS;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.PROPERTY_NOT_EXISTS;

/**
 * 规格名称 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ProductPropertyServiceImpl implements ProductPropertyService {

    @Resource
    private ProductPropertyMapper productPropertyMapper;

    @Resource
    private ProductPropertyValueMapper productPropertyValueMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProperty(ProductPropertyCreateReqVO createReqVO) {
        // 校验存在
        if (productPropertyMapper.selectByName(createReqVO.getName()) != null) {
            throw exception(PROPERTY_EXISTS);
        }
        // 插入
        ProductPropertyDO property = ProductPropertyConvert.INSTANCE.convert(createReqVO);
        productPropertyMapper.insert(property);
        // 返回
        return property.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProperty(ProductPropertyUpdateReqVO updateReqVO) {
        // 校验存在
        this.validatePropertyExists(updateReqVO.getId());
        ProductPropertyDO productPropertyDO = productPropertyMapper.selectByName(updateReqVO.getName());
        if (productPropertyDO != null && !productPropertyDO.getId().equals(updateReqVO.getId())) {
            throw exception(PROPERTY_EXISTS);
        }
        // 更新
        ProductPropertyDO updateObj = ProductPropertyConvert.INSTANCE.convert(updateReqVO);
        productPropertyMapper.updateById(updateObj);
    }

    @Override
    public void deleteProperty(Long id) {
        // 校验存在
        this.validatePropertyExists(id);
        // 删除
        productPropertyMapper.deleteById(id);
        //同步删除属性值
        productPropertyValueMapper.deletePropertyValueByPropertyId(id);
    }

    private void validatePropertyExists(Long id) {
        if (productPropertyMapper.selectById(id) == null) {
            throw exception(PROPERTY_NOT_EXISTS);
        }
    }

    @Override
    public List<ProductPropertyRespVO> getPropertyList(ProductPropertyListReqVO listReqVO) {
        return ProductPropertyConvert.INSTANCE.convertList(productPropertyMapper.selectList(new LambdaQueryWrapperX<ProductPropertyDO>()
                .likeIfPresent(ProductPropertyDO::getName, listReqVO.getName())
                .eqIfPresent(ProductPropertyDO::getStatus, listReqVO.getStatus())));
    }

    @Override
    public PageResult<ProductPropertyRespVO> getPropertyPage(ProductPropertyPageReqVO pageReqVO) {
        //获取属性列表
        PageResult<ProductPropertyDO> pageResult = productPropertyMapper.selectPage(pageReqVO);
        return ProductPropertyConvert.INSTANCE.convertPage(pageResult);
    }

    @Override
    public ProductPropertyRespVO getProperty(Long id) {
        ProductPropertyDO property = productPropertyMapper.selectById(id);
        return ProductPropertyConvert.INSTANCE.convert(property);
    }

    @Override
    public List<ProductPropertyRespVO> getPropertyList(Collection<Long> ids) {
        return ProductPropertyConvert.INSTANCE.convertList(productPropertyMapper.selectBatchIds(ids));
    }

    @Override
    public List<ProductPropertyAndValueRespVO> getPropertyAndValueList(ProductPropertyListReqVO listReqVO) {
        List<ProductPropertyRespVO> propertyList = getPropertyList(listReqVO);

        // 查询属性值
        List<ProductPropertyValueDO> valueDOList = productPropertyValueMapper.selectListByPropertyId(CollectionUtils.convertList(propertyList, ProductPropertyRespVO::getId));
        Map<Long, List<ProductPropertyValueDO>> valueDOMap = CollectionUtils.convertMultiMap(valueDOList, ProductPropertyValueDO::getPropertyId);
        return CollectionUtils.convertList(propertyList, m -> {
            ProductPropertyAndValueRespVO productPropertyAndValueRespVO = ProductPropertyConvert.INSTANCE.convert(m);
            productPropertyAndValueRespVO.setValues(ProductPropertyValueConvert.INSTANCE.convertList(valueDOMap.get(m.getId())));
            return productPropertyAndValueRespVO;
        });
    }
}
