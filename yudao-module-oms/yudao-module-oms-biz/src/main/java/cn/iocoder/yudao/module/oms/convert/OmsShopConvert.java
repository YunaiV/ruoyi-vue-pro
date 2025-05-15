package cn.iocoder.yudao.module.oms.convert;


import cn.iocoder.yudao.module.oms.api.dto.OmsShopDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import cn.iocoder.yudao.module.oms.controller.admin.shop.vo.OmsShopSaveReqVO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/4/1$
 */
@Mapper
public interface OmsShopConvert {

    OmsShopConvert INSTANCE = Mappers.getMapper(OmsShopConvert.class);

    //    @Mappings(
//        @Mapping(target = "platformCode", source = "platformCode")
//    )
    OmsShopDO convert(OmsShopSaveReqVO saveReqDTO);

    List<OmsShopDO> toOmsShopDOs(List<OmsShopSaveReqDTO> saveReqDTOs);

    OmsShopDTO toOmsShopDTO(OmsShopDO omsShopDO);
}
