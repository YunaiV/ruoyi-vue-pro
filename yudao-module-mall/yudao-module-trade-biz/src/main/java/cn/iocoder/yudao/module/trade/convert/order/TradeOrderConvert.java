package cn.iocoder.yudao.module.trade.convert.order;

import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateRespDTO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @author LeeYan9
 * @since 2022-08-26
 */
@Mapper
public interface TradeOrderConvert {

    TradeOrderConvert INSTANCE = Mappers.getMapper(TradeOrderConvert.class);

    @Mapping(source = "order.couponId", target = "couponId")
    TradeOrderDO convert(AppTradeOrderCreateReqVO createReqVO, PriceCalculateRespDTO.Order order);
}
