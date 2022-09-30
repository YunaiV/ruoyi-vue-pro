package cn.iocoder.yudao.module.trade.convert.order;

import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TradeOrderItemConvert {

    TradeOrderItemConvert INSTANCE = Mappers.getMapper(TradeOrderItemConvert.class);

    List<TradeOrderItemDO> convertList(List<PriceCalculateRespDTO.OrderItem> items);

}
