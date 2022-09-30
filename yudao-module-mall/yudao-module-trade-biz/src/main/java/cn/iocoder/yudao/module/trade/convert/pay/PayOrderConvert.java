package cn.iocoder.yudao.module.trade.convert.pay;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.module.pay.api.order.PayOrderInfoCreateReqDTO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Date;

/**
 * @author LeeYan9
 * @since 2022-08-26
 */
@Mapper
public interface PayOrderConvert {

    PayOrderConvert INSTANCE = Mappers.getMapper(PayOrderConvert.class);

    @Mappings({
            @Mapping(source = "payPrice", target = "amount"),
            @Mapping(target = "expireTime", source = "cancelTime" , qualifiedByName = "convertCreateTimeToPayExpireTime")
    })
    PayOrderInfoCreateReqDTO convert(TradeOrderDO tradeOrderDO);

    @Named("convertCreateTimeToPayExpireTime")
    default Date convertCreateTimeToPayExpireTime(Date cancelTime) {
        return DateUtil.offsetMinute(new Date(), 30);
    }
}
