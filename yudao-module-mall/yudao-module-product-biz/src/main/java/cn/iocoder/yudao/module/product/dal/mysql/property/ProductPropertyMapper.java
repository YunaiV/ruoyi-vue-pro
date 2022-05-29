package cn.iocoder.yudao.module.product.dal.mysql.property;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.*;

/**
 * 规格名称 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductPropertyMapper extends BaseMapperX<ProductPropertyDO> {

    default PageResult<ProductPropertyDO> selectPage(ProductPropertyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductPropertyDO>()
                .likeIfPresent(ProductPropertyDO::getName, reqVO.getName())
                .eqIfPresent(ProductPropertyDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ProductPropertyDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(ProductPropertyDO::getId));
    }

    default List<ProductPropertyDO> selectList(ProductPropertyExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<ProductPropertyDO>()
                .likeIfPresent(ProductPropertyDO::getName, reqVO.getName())
                .eqIfPresent(ProductPropertyDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ProductPropertyDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(ProductPropertyDO::getId));
    }

}
