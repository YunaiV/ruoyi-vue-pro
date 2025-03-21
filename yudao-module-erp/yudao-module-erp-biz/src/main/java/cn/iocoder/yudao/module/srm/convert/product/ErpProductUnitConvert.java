package cn.iocoder.yudao.module.srm.convert.product;


import cn.iocoder.yudao.module.srm.api.product.dto.ErpProductUnitDTO;
import cn.iocoder.yudao.module.srm.dal.dataobject.product.ErpProductUnitDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface ErpProductUnitConvert {

    ErpProductUnitConvert INSTANCE = Mappers.getMapper(ErpProductUnitConvert.class);


    ErpProductUnitDTO convert(ErpProductUnitDO erpProductUnitDO);

    default List<ErpProductUnitDTO> convert(List<ErpProductUnitDO> erpProductUnitDOs) {
        return erpProductUnitDOs.stream().map(this::convert).toList();
    }

    // Map 转换
    default Map<Long, ErpProductUnitDTO> convert(Map<Long, ErpProductUnitDO> productUnitMap) {
        if (productUnitMap == null || productUnitMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, ErpProductUnitDTO> result = new HashMap<>();
        for (Map.Entry<Long, ErpProductUnitDO> entry : productUnitMap.entrySet()) {
            result.put(entry.getKey(), convert(entry.getValue()));
        }
        return result;
    }


}
