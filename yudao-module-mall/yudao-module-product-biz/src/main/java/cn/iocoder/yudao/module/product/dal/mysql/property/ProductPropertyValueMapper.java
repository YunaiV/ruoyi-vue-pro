package cn.iocoder.yudao.module.product.dal.mysql.property;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValuePageReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyValueDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 规格值 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductPropertyValueMapper extends BaseMapperX<ProductPropertyValueDO> {

    // TODO @franky：方法名，selectListByXXX。mapper 的操作都是 crud
    default List<ProductPropertyValueDO> getPropertyValueListByPropertyId(List<Long> propertyIds) {
        // TODO @franky：调用父类的 selectList
        return selectList(new LambdaQueryWrapperX<ProductPropertyValueDO>()
                .inIfPresent(ProductPropertyValueDO::getPropertyId, propertyIds));
    }

    default void deletePropertyValueByPropertyId(Long propertyId) {
        // TODO @franky：delete(new ) 即可
        LambdaQueryWrapperX<ProductPropertyValueDO> queryWrapperX = new LambdaQueryWrapperX<>();
        queryWrapperX.eq(ProductPropertyValueDO::getPropertyId, propertyId)
                .eq(ProductPropertyValueDO::getDeleted, false);
        delete(queryWrapperX);
    }

    default PageResult<ProductPropertyValueDO> selectPage(ProductPropertyValuePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductPropertyValueDO>()
                .eqIfPresent(ProductPropertyValueDO::getPropertyId, reqVO.getPropertyId())
                .likeIfPresent(ProductPropertyValueDO::getName, reqVO.getName())
                .eqIfPresent(ProductPropertyValueDO::getStatus, reqVO.getStatus())
                .orderByDesc(ProductPropertyValueDO::getId));
    }
}
