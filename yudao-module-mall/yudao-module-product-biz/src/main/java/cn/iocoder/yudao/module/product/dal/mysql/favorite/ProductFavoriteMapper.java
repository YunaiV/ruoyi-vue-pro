package cn.iocoder.yudao.module.product.dal.mysql.favorite;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.product.dal.dataobject.favorite.ProductFavoriteDO;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteRespVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductFavoriteMapper extends BaseMapperX<ProductFavoriteDO> {

    default ProductFavoriteDO selectByUserAndSpuAndType(Long userId, Long spuId, Integer type) {
        Assert.notNull(userId, "the userId must not be null");
        Assert.notNull(spuId, "the spuId must not be null");
        Assert.notNull(type, "the type must not be null");
        return selectOne(new LambdaQueryWrapperX<ProductFavoriteDO>()
                .eq(ProductFavoriteDO::getUserId, userId)
                .eq(ProductFavoriteDO::getSpuId, spuId)
                .eq(ProductFavoriteDO::getType, type));
    }

    default PageResult<ProductFavoriteDO> selectPageByUserAndType(Long userId, Integer type, PageParam pageParam) {
        Assert.notNull(userId, "the userId must not be null");
        Assert.notNull(type, "the type must not be null");
        Assert.notNull(pageParam, "the pageParam must not be null");
        return selectPage(pageParam, new LambdaQueryWrapper<ProductFavoriteDO>()
                .eq(ProductFavoriteDO::getUserId, userId)
                .eq(ProductFavoriteDO::getType, type)
                .orderByDesc(ProductFavoriteDO::getId));
    }
}
