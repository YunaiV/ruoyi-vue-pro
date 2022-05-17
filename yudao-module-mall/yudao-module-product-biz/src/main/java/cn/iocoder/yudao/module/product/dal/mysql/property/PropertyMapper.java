package cn.iocoder.yudao.module.product.dal.mysql.property;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.property.PropertyDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.*;

/**
 * 规格名称 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface PropertyMapper extends BaseMapperX<PropertyDO> {

    default PageResult<PropertyDO> selectPage(PropertyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PropertyDO>()
                .likeIfPresent(PropertyDO::getName, reqVO.getName())
                .eqIfPresent(PropertyDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(PropertyDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(PropertyDO::getId));
    }

    default List<PropertyDO> selectList(PropertyExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<PropertyDO>()
                .likeIfPresent(PropertyDO::getName, reqVO.getName())
                .eqIfPresent(PropertyDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(PropertyDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(PropertyDO::getId));
    }

}
