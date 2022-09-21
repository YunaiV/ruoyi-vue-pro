package cn.iocoder.yudao.module.product.dal.mysql.spu;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品spu Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductSpuMapper extends BaseMapperX<ProductSpuDO> {

    default PageResult<ProductSpuDO> selectPage(ProductSpuPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductSpuDO>()
                .likeIfPresent(ProductSpuDO::getName, reqVO.getName())
                .eqIfPresent(ProductSpuDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(ProductSpuDO::getStatus, reqVO.getStatus())
                .leIfPresent(ProductSpuDO::getSalesCount, reqVO.getSalesCountMax())
                .geIfPresent(ProductSpuDO::getSalesCount, reqVO.getSalesCountMin())
                .leIfPresent(ProductSpuDO::getMarketPrice, reqVO.getMarketPriceMax())
                .geIfPresent(ProductSpuDO::getMarketPrice, reqVO.getMarketPriceMin())
                .orderByDesc(ProductSpuDO::getSort));
    }

    default PageResult<ProductSpuDO> selectPage(ProductSpuPageReqVO reqVO, List<Long> spuIds) {
        LambdaQueryWrapperX<ProductSpuDO> productSpuDOLambdaQueryWrapperX = new LambdaQueryWrapperX<ProductSpuDO>()
                .likeIfPresent(ProductSpuDO::getName, reqVO.getName())
                .eqIfPresent(ProductSpuDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(ProductSpuDO::getStatus, reqVO.getStatus())
                .leIfPresent(ProductSpuDO::getSalesCount, reqVO.getSalesCountMax())
                .geIfPresent(ProductSpuDO::getSalesCount, reqVO.getSalesCountMin())
                .leIfPresent(ProductSpuDO::getMarketPrice, reqVO.getMarketPriceMax())
                .geIfPresent(ProductSpuDO::getMarketPrice, reqVO.getMarketPriceMin())
                .orderByDesc(ProductSpuDO::getSort);

        if(reqVO.getTabStatus()!= null && reqVO.getTabStatus() == 2){
            productSpuDOLambdaQueryWrapperX.inIfPresent(ProductSpuDO::getId, spuIds);
        }else{
            productSpuDOLambdaQueryWrapperX.eqIfPresent(ProductSpuDO::getStatus, reqVO.getTabStatus());
        }

        return selectPage(reqVO, productSpuDOLambdaQueryWrapperX);
    }


}
