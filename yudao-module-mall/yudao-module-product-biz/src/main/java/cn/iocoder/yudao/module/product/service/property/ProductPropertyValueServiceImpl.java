package cn.iocoder.yudao.module.product.service.property;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValuePageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueRespVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueUpdateReqVO;
import cn.iocoder.yudao.module.product.convert.propertyvalue.ProductPropertyValueConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyDO;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyValueDO;
import cn.iocoder.yudao.module.product.dal.mysql.property.ProductPropertyValueMapper;
import cn.iocoder.yudao.module.product.service.property.bo.ProductPropertyValueDetailRespBO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        if (productPropertyValueMapper.selectByName(createReqVO.getPropertyId(), createReqVO.getName()) != null) {
            throw exception(PROPERTY_VALUE_EXISTS);
        }
        ProductPropertyValueDO convert = ProductPropertyValueConvert.INSTANCE.convert(createReqVO);
        productPropertyValueMapper.insert(convert);
        return convert.getId();
    }

    @Override
    public void updatePropertyValue(ProductPropertyValueUpdateReqVO updateReqVO) {
        ProductPropertyValueDO productPropertyValueDO = productPropertyValueMapper.selectByName(updateReqVO.getPropertyId(), updateReqVO.getName());
        if (productPropertyValueDO != null && !productPropertyValueDO.getId().equals(updateReqVO.getId())) {
            throw exception(PROPERTY_VALUE_EXISTS);
        }
        ProductPropertyValueDO convert = ProductPropertyValueConvert.INSTANCE.convert(updateReqVO);
        productPropertyValueMapper.updateById(convert);
    }

    @Override
    public void deletePropertyValue(Long id) {
        productPropertyValueMapper.deleteById(id);
    }

    @Override
    public ProductPropertyValueRespVO getPropertyValue(Long id) {
        ProductPropertyValueDO productPropertyValueDO = productPropertyValueMapper.selectOne(new LambdaQueryWrapper<ProductPropertyValueDO>()
                .eq(ProductPropertyValueDO::getId, id));
        return ProductPropertyValueConvert.INSTANCE.convert(productPropertyValueDO);
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
    public List<ProductPropertyValueRespVO> getPropertyValueListByPropertyId(List<Long> propertyIds) {
        return ProductPropertyValueConvert.INSTANCE.convertList(productPropertyValueMapper.selectList("property_id", propertyIds));
    }

    @Override
    public Integer getPropertyValueCountByPropertyId(Long propertyId) {
        return productPropertyValueMapper.selectCountByPropertyId(propertyId);
    }

    @Override
    public PageResult<ProductPropertyValueRespVO> getPropertyValueListPage(ProductPropertyValuePageReqVO pageReqVO) {
        return ProductPropertyValueConvert.INSTANCE.convertPage(productPropertyValueMapper.selectPage(pageReqVO));
    }

    @Override
    public void deletePropertyValueByPropertyId(Long propertyId) {
        productPropertyValueMapper.deleteByPropertyId(propertyId);
    }

}
