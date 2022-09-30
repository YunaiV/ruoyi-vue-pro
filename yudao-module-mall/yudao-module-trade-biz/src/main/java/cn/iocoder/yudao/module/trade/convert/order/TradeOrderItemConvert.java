package cn.iocoder.yudao.module.trade.convert.order;

import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author LeeYan9
 * @since 2022-08-26
 */
@Mapper
public interface TradeOrderItemConvert {

    TradeOrderItemConvert INSTANCE = Mappers.getMapper(TradeOrderItemConvert.class);

    /**
     * @param items sku列表价格
     * @return 订单项
     */
    List<TradeOrderItemDO> convertList(List<PriceCalculateRespDTO.OrderItem> items);
}
