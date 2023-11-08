package cn.iocoder.yudao.module.crm.dal.mysql.product;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.ProductDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.*;

/**
 * 产品 Mapper
 *
 * @author ZanGe丶
 */
@Mapper
public interface ProductMapper extends BaseMapperX<ProductDO> {

    default PageResult<ProductDO> selectPage(ProductPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductDO>()
                .likeIfPresent(ProductDO::getName, reqVO.getName())
                .likeIfPresent(ProductDO::getNo, reqVO.getNo())
                .eqIfPresent(ProductDO::getUnit, reqVO.getUnit())
                .eqIfPresent(ProductDO::getPrice, reqVO.getPrice())
                .eqIfPresent(ProductDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ProductDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(ProductDO::getDescription, reqVO.getDescription())
                .eqIfPresent(ProductDO::getOwnerUserId, reqVO.getOwnerUserId())
                .betweenIfPresent(ProductDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ProductDO::getId));
    }

    default List<ProductDO> selectList(ProductExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<ProductDO>()
                .likeIfPresent(ProductDO::getName, reqVO.getName())
                .likeIfPresent(ProductDO::getNo, reqVO.getNo())
                .eqIfPresent(ProductDO::getUnit, reqVO.getUnit())
                .eqIfPresent(ProductDO::getPrice, reqVO.getPrice())
                .eqIfPresent(ProductDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ProductDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(ProductDO::getDescription, reqVO.getDescription())
                .eqIfPresent(ProductDO::getOwnerUserId, reqVO.getOwnerUserId())
                .betweenIfPresent(ProductDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ProductDO::getId));
    }

}
