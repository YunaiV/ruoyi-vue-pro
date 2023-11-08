package cn.iocoder.yudao.module.product.dal.dataobject.favorite;

import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import lombok.Data;

/**
 * 商品收藏 DO
 *
 * @author 芋道源码
 */
@Data
public class ProductFavoriteDetailDO extends ProductFavoriteDO {

    ProductSpuDO spuDO;
}
