package cn.iocoder.yudao.module.trade.convert.pay;

import cn.iocoder.yudao.module.pay.api.order.PayOrderDataCreateReqDTO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import org.mapstruct.factory.Mappers;

/**
 * @author LeeYan9
 * @since 2022-08-26
 */
public interface PayOrderConvert {

    PayOrderConvert INSTANCE = Mappers.getMapper(PayOrderConvert.class);


    PayOrderDataCreateReqDTO convert(TradeOrderDO tradeOrderDO);
}
