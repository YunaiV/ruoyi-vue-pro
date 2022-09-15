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

/**
 * <p>
 *
 * </p>
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
        ProductPropertyValueDO convert = ProductPropertyValueConvert.INSTANCE.convert(createReqVO);
        productPropertyValueMapper.insert(convert);
        return convert.getId();
    }

    @Override
    public void updatePropertyValue(ProductPropertyValueUpdateReqVO updateReqVO) {
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
