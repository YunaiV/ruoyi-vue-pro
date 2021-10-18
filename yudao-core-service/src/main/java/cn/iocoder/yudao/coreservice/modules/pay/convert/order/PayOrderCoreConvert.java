package cn.iocoder.yudao.coreservice.modules.pay.convert.order;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderExtensionDO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.dto.PayOrderSubmitReqDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PayOrderCoreConvert {

    PayOrderCoreConvert INSTANCE = Mappers.getMapper(PayOrderCoreConvert.class);

    PayOrderDO convert(PayOrderCreateReqDTO bean);

    PayOrderExtensionDO convert(PayOrderSubmitReqDTO bean);

}
