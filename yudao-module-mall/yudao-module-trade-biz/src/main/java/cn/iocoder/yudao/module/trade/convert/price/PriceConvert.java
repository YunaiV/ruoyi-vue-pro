package cn.iocoder.yudao.module.trade.convert.price;

import cn.iocoder.yudao.module.market.api.price.dto.PriceCalculateReqDTO;
import cn.iocoder.yudao.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PriceConvert {

    PriceConvert INSTANCE = Mappers.getMapper(PriceConvert.class);

    @Mappings(
            @Mapping(source = "userId" , target = "userId")
    )
    PriceCalculateReqDTO convert(AppTradeOrderCreateReqVO createReqVO, Long userId);

}
