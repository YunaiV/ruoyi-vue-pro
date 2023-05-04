package cn.iocoder.yudao.module.product.convert.favorite;

import cn.iocoder.yudao.module.product.controller.app.favorite.vo.AppFavoriteReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.favorite.ProductFavoriteDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 喜爱商品 Convert
 *
 * @author jason
 */
@Mapper
public interface ProductFavoriteConvert {

    ProductFavoriteConvert INSTANCE = Mappers.getMapper(ProductFavoriteConvert.class);

    ProductFavoriteDO convert(Long userId, AppFavoriteReqVO reqVO);
}
