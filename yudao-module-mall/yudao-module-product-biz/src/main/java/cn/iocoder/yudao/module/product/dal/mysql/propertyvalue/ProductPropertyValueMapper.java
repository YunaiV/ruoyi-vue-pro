package cn.iocoder.yudao.module.product.dal.mysql.propertyvalue;

import java.util.*;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyValueDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 规格值 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductPropertyValueMapper extends BaseMapperX<ProductPropertyValueDO> {

    default List<ProductPropertyValueDO> getPropertyValueListByPropertyId(List<Long> propertyIds){
        return selectList(new LambdaQueryWrapperX<ProductPropertyValueDO>()
                .inIfPresent(ProductPropertyValueDO::getPropertyId, propertyIds));
    }

    default void deletePropertyValueByPropertyId(Long propertyId){
        LambdaQueryWrapperX<ProductPropertyValueDO> queryWrapperX = new LambdaQueryWrapperX<>();
        queryWrapperX.eq(ProductPropertyValueDO::getPropertyId, propertyId)
                .eq(ProductPropertyValueDO::getDeleted, false);
        delete(queryWrapperX);
    }
}
