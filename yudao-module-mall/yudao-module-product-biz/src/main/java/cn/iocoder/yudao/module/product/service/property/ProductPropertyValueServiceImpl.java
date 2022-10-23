package cn.iocoder.yudao.module.product.service.property;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValuePageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueRespVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueUpdateReqVO;
import cn.iocoder.yudao.module.product.convert.propertyvalue.ProductPropertyValueConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyValueDO;
import cn.iocoder.yudao.module.product.dal.mysql.property.ProductPropertyValueMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.PROPERTY_VALUE_EXISTS;

/**
 * 规格值 Service 实现类
 *
 * @author LuoWenFeng
 */
@Service
@Validated
public class ProductPropertyValueServiceImpl implements ProductPropertyValueService {

    @Resource
    private ProductPropertyValueMapper productPropertyValueMapper;

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
        // TODO @luowenfeng：如果是自己的情况下，名字相同也是 ok 的呀~
        if (productPropertyValueMapper.selectByName(updateReqVO.getPropertyId(), updateReqVO.getName()) != null) {
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
    public List<ProductPropertyValueRespVO> getPropertyValueListByPropertyId(List<Long> id) {
        return ProductPropertyValueConvert.INSTANCE.convertList(productPropertyValueMapper.selectList("property_id", id));
    }

    @Override
    public PageResult<ProductPropertyValueRespVO> getPropertyValueListPage(ProductPropertyValuePageReqVO pageReqVO) {
        return ProductPropertyValueConvert.INSTANCE.convertPage(productPropertyValueMapper.selectPage(pageReqVO));
    }
}
