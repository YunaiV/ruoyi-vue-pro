package cn.iocoder.yudao.module.oms.convert;


import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductDTO;
import cn.iocoder.yudao.module.oms.api.dto.OmsShopProductSaveReqDTO;
import cn.iocoder.yudao.module.oms.controller.admin.product.vo.OmsShopProductRespVO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface OmsShopProductConvert {

    OmsShopProductConvert INSTANCE = Mappers.getMapper(OmsShopProductConvert.class);

    List<OmsShopProductDO> toOmsShopProductDO(List<OmsShopProductSaveReqDTO> saveReqDTOs);

    @Mappings({
        @Mapping(source = "items", target = "items"),
        @Mapping(source = "shop", target = "shop")
    })
    OmsShopProductDTO toOmsShopProductDTO(OmsShopProductRespVO productVo);
}
