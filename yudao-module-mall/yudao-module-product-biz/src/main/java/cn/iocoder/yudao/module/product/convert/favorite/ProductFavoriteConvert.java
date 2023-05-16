package cn.iocoder.yudao.module.product.convert.favorite;

import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteReqVO;
import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteRespVO;
import cn.iocoder.yudao.module.product.dal.dataobject.favorite.ProductFavoriteDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

@Mapper
public interface ProductFavoriteConvert {

    ProductFavoriteConvert INSTANCE = Mappers.getMapper(ProductFavoriteConvert.class);

    ProductFavoriteDO convert(Long userId, AppFavoriteReqVO reqVO);

    @Mapping(target = "id", source = "favorite.id")
    @Mapping(target = "spuName", source = "spu.name")
    AppFavoriteRespVO convert(ProductSpuDO spu, ProductFavoriteDO favorite);

    default List<AppFavoriteRespVO> convertList(List<ProductFavoriteDO> favorites, List<ProductSpuDO> spus) {
        List<AppFavoriteRespVO> resultList = new ArrayList<>(favorites.size());
        Map<Long, ProductSpuDO> spuMap = convertMap(spus, ProductSpuDO::getId);
        for (ProductFavoriteDO favorite : favorites) {
            ProductSpuDO spuDO = spuMap.get(favorite.getSpuId());
            resultList.add(convert(spuDO, favorite));
        }
        return resultList;
    }

}
