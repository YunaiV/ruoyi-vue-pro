package cn.iocoder.yudao.module.oms.convert;

import cn.iocoder.yudao.module.oms.api.dto.OmsOrderSaveReqDTO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface OmsOrderConvert {
    OmsOrderConvert INSTANCE = Mappers.getMapper(OmsOrderConvert.class);

    List<OmsOrderDO> toOmsOrderDOs(List<OmsOrderSaveReqDTO> saveReqDTOs);
}
