package cn.iocoder.yudao.module.product.dal.mysql.spu;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.*;

/**
 * 商品spu Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductSpuMapper extends BaseMapperX<ProductSpuDO> {

    default PageResult<ProductSpuDO> selectPage(SpuPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductSpuDO>()
                .likeIfPresent(ProductSpuDO::getName, reqVO.getName())
                .eqIfPresent(ProductSpuDO::getSellPoint, reqVO.getSellPoint())
                .eqIfPresent(ProductSpuDO::getDescription, reqVO.getDescription())
                .eqIfPresent(ProductSpuDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(ProductSpuDO::getPicUrls, reqVO.getPicUrls())
                .eqIfPresent(ProductSpuDO::getSort, reqVO.getSort())
                .eqIfPresent(ProductSpuDO::getLikeCount, reqVO.getLikeCount())
                .eqIfPresent(ProductSpuDO::getPrice, reqVO.getPrice())
                .eqIfPresent(ProductSpuDO::getQuantity, reqVO.getQuantity())
                .eqIfPresent(ProductSpuDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ProductSpuDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(ProductSpuDO::getId));
    }

    default List<ProductSpuDO> selectList(SpuExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<ProductSpuDO>()
                .likeIfPresent(ProductSpuDO::getName, reqVO.getName())
                .eqIfPresent(ProductSpuDO::getSellPoint, reqVO.getSellPoint())
                .eqIfPresent(ProductSpuDO::getDescription, reqVO.getDescription())
                .eqIfPresent(ProductSpuDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(ProductSpuDO::getPicUrls, reqVO.getPicUrls())
                .eqIfPresent(ProductSpuDO::getSort, reqVO.getSort())
                .eqIfPresent(ProductSpuDO::getLikeCount, reqVO.getLikeCount())
                .eqIfPresent(ProductSpuDO::getPrice, reqVO.getPrice())
                .eqIfPresent(ProductSpuDO::getQuantity, reqVO.getQuantity())
                .eqIfPresent(ProductSpuDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ProductSpuDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(ProductSpuDO::getId));
    }

}
