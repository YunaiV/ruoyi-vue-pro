package cn.iocoder.yudao.module.product.convert.favorite;

import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteReqVO;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteRespVO;
import cn.iocoder.yudao.module.product.dal.dataobject.favorite.ProductFavoriteDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductFavoriteConvert {

    ProductFavoriteConvert INSTANCE = Mappers.getMapper(ProductFavoriteConvert.class);

    ProductFavoriteDO convert(Long userId, AppFavoriteReqVO reqVO);
    @Mapping(target = "id", source = "favoriteDO.id")
    @Mapping(target = "spuName", source = "spuDO.name")
    AppFavoriteRespVO convert(ProductSpuDO spuDO, ProductFavoriteDO favoriteDO);
}
