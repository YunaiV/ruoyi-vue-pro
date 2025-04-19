package cn.iocoder.yudao.module.oms.convert;

import cn.iocoder.yudao.module.oms.api.dto.OmsOrderItemSaveReqDTO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsOrderItemDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface OmsOrderItemConvert {
    OmsOrderItemConvert INSTANCE = Mappers.getMapper(OmsOrderItemConvert.class);

    List<OmsOrderItemDO> toOmsOrderItemDOs(List<OmsOrderItemSaveReqDTO> saveReqDTOs);

    OmsOrderItemDO toOmsOrderItemDO(OmsOrderItemSaveReqDTO saveReqDTO);
}
