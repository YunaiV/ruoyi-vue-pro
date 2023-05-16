package cn.iocoder.yudao.module.product.dal.mysql.favorite;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.favorite.ProductFavoriteDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductFavoriteMapper extends BaseMapperX<ProductFavoriteDO> {

    default ProductFavoriteDO selectByUserAndSpuAndType(Long userId, Long spuId, Integer type) {
        return selectOne(new LambdaQueryWrapperX<ProductFavoriteDO>()
                .eq(ProductFavoriteDO::getUserId, userId)
                .eq(ProductFavoriteDO::getSpuId, spuId)
                .eq(ProductFavoriteDO::getType, type));
    }

    default PageResult<ProductFavoriteDO> selectPageByUserAndType(Long userId, Integer type, PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapper<ProductFavoriteDO>()
                .eq(ProductFavoriteDO::getUserId, userId)
                .eq(ProductFavoriteDO::getType, type)
                .orderByDesc(ProductFavoriteDO::getId));
    }

}
