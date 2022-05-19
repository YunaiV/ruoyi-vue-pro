package cn.iocoder.yudao.module.product.dal.mysql.propertyvalue;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.propertyvalue.PropertyValueDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.product.controller.admin.propertyvalue.vo.*;

/**
 * 规格值 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface PropertyValueMapper extends BaseMapperX<PropertyValueDO> {

    default PageResult<PropertyValueDO> selectPage(PropertyValuePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PropertyValueDO>()
                .eqIfPresent(PropertyValueDO::getPropertyId, reqVO.getPropertyId())
                .likeIfPresent(PropertyValueDO::getName, reqVO.getName())
                .eqIfPresent(PropertyValueDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(PropertyValueDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(PropertyValueDO::getId));
    }

    default List<PropertyValueDO> selectList(PropertyValueExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<PropertyValueDO>()
                .eqIfPresent(PropertyValueDO::getPropertyId, reqVO.getPropertyId())
                .likeIfPresent(PropertyValueDO::getName, reqVO.getName())
                .eqIfPresent(PropertyValueDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(PropertyValueDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(PropertyValueDO::getId));
    }

    default List<PropertyValueDO> getPropertyValueListByPropertyId(List<Long> propertyIds){
        return selectList(new LambdaQueryWrapperX<PropertyValueDO>()
                .inIfPresent(PropertyValueDO::getPropertyId, propertyIds));
    }

    default void deletePropertyValueByPropertyId(Long propertyId){
        LambdaQueryWrapperX<PropertyValueDO> queryWrapperX = new LambdaQueryWrapperX<>();
        queryWrapperX.eq(PropertyValueDO::getPropertyId, propertyId).eq(PropertyValueDO::getDeleted, false);
        delete(queryWrapperX);
    }
}
