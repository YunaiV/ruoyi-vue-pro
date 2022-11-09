package cn.iocoder.yudao.module.trade.convert.sku;

import cn.iocoder.yudao.module.product.api.sku.dto.SkuDecrementStockBatchReqDTO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author LeeYan9
 * @since 2022-08-26
 */
@Mapper
public interface ProductSkuConvert {

    ProductSkuConvert INSTANCE = Mappers.getMapper(ProductSkuConvert.class);

    List<SkuDecrementStockBatchReqDTO.Item> convert(List<TradeOrderItemDO> tradeOrderItems);
}
