package cn.iocoder.yudao.module.trade.convert.order;

import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
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
     *
     * @param tradeOrder 交易订单
     * @param items sku列表价格
     * @return 订单项
     */
    @Mappings({
            @Mapping(source = "tradeOrder.userId", target = "userId"),
            @Mapping(source = "tradeOrder.orderId", target = "orderId")
    })
    default List<TradeOrderItemDO> convertList(TradeOrderDO tradeOrder, List<PriceCalculateRespDTO.Item> items) {
        // TODO @Com: Mapstruct 生成会报错
        throw new UnsupportedOperationException("无法实现");
    }
}
