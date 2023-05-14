package cn.iocoder.yudao.module.product.convert.favorite;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
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

@Mapper
public interface ProductFavoriteConvert {

    ProductFavoriteConvert INSTANCE = Mappers.getMapper(ProductFavoriteConvert.class);

    ProductFavoriteDO convert(Long userId, AppFavoriteReqVO reqVO);

    @Mapping(target = "id", source = "favoriteDO.id")
    @Mapping(target = "spuName", source = "spuDO.name")
    AppFavoriteRespVO convert(ProductSpuDO spuDO, ProductFavoriteDO favoriteDO);

    default List<AppFavoriteRespVO> convertList(List<ProductFavoriteDO> productFavoriteDOList, List<ProductSpuDO> productSpuDOList) {
        List<AppFavoriteRespVO> resultList = new ArrayList<>(productFavoriteDOList.size());
        Map<Long, ProductSpuDO> spuMap = CollectionUtils.convertMap(productSpuDOList, ProductSpuDO::getId);
        for (ProductFavoriteDO item : productFavoriteDOList) {
            ProductSpuDO spuDO = spuMap.get(item.getSpuId());
            resultList.add(convert(spuDO, item));
        }
        return resultList;
    }
}
