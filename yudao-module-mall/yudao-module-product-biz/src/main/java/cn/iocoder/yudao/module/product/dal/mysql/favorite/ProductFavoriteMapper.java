package cn.iocoder.yudao.module.product.dal.mysql.favorite;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.product.controller.admin.favorite.vo.ProductFavoritePageReqVO;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoritePageReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.favorite.ProductFavoriteDO;
import cn.iocoder.yudao.module.product.dal.dataobject.favorite.ProductFavoriteDetailDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductFavoriteMapper extends BaseMapperX<ProductFavoriteDO> {

    default ProductFavoriteDO selectByUserIdAndSpuId(Long userId, Long spuId) {
        return selectOne(ProductFavoriteDO::getUserId, userId,
                ProductFavoriteDO::getSpuId, spuId);
    }

    default PageResult<ProductFavoriteDO> selectPageByUserAndType(Long userId, AppFavoritePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapper<ProductFavoriteDO>()
                .eq(ProductFavoriteDO::getUserId, userId)
                .orderByDesc(ProductFavoriteDO::getId));
    }

    default PageResult<ProductFavoriteDetailDO> selectPageByUserAndFields(ProductFavoritePageReqVO reqVO) {
        MPJLambdaWrapper<ProductFavoriteDO> wrapper =  new MPJLambdaWrapper<ProductFavoriteDO>()
                .selectAll(ProductFavoriteDO.class)
                .eq(ProductFavoriteDO::getUserId, reqVO.getUserId())
                .selectAssociation(ProductSpuDO.class, ProductFavoriteDetailDO::getSpuDO);
        if(StringUtils.isNotEmpty(reqVO.getName())){
            wrapper.likeRight(ProductSpuDO::getName, reqVO.getName());
        }
        if(StringUtils.isNotEmpty(reqVO.getName()) && StringUtils.isNotEmpty(reqVO.getKeyword())){
            wrapper.or();
        }
        if(StringUtils.isNotEmpty(reqVO.getKeyword())){
            wrapper.likeRight(ProductSpuDO::getKeyword, reqVO.getKeyword());
        }

        if(reqVO.getCreateTime() != null){
            if (reqVO.getCreateTime()[0] != null && reqVO.getCreateTime()[1] != null) {
                wrapper.between(ProductFavoriteDO::getCreateTime, reqVO.getCreateTime()[0], reqVO.getCreateTime()[1]);
            }
            if (reqVO.getCreateTime()[0] != null) {
                wrapper.ge(ProductFavoriteDO::getCreateTime, reqVO.getCreateTime()[0]);
            }
            if (reqVO.getCreateTime()[1] != null) {
                wrapper.le(ProductFavoriteDO::getCreateTime, reqVO.getCreateTime()[1]);
            }
        }

        return selectJoinPage(reqVO, ProductFavoriteDetailDO.class, wrapper);
    }

    default Long selectCountByUserId(Long userId) {
        return selectCount(ProductFavoriteDO::getUserId, userId);
    }

}
