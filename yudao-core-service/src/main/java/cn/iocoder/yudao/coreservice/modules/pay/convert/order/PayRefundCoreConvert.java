package cn.iocoder.yudao.coreservice.modules.pay.convert.order;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayRefundDO;
import cn.iocoder.yudao.coreservice.modules.pay.service.order.bo.PayRefundPostReqBO;
import cn.iocoder.yudao.framework.pay.core.client.dto.PayRefundUnifiedRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper
public interface PayRefundCoreConvert {

    PayRefundCoreConvert INSTANCE = Mappers.getMapper(PayRefundCoreConvert.class);

    PayRefundPostReqBO convert(PayRefundUnifiedRespDTO respDTO);

    //TODO 太多需要处理了， 暂时不用
    @Mappings(value = {
            @Mapping(source = "amount", target = "payAmount"),
            @Mapping(source = "id", target = "orderId"),
            @Mapping(target = "status",ignore = true)
    })
    PayRefundDO convert(PayOrderDO orderDO);
}
