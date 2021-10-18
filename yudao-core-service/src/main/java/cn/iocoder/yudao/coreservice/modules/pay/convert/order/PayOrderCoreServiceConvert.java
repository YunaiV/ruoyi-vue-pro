package cn.iocoder.yudao.coreservice.modules.pay.convert.order;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayOrderCreateReqDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PayOrderCoreServiceConvert {

    PayOrderCoreServiceConvert INSTANCE = Mappers.getMapper(PayOrderCoreServiceConvert.class);

    PayOrderDO convert(PayOrderCreateReqDTO bean);

}
