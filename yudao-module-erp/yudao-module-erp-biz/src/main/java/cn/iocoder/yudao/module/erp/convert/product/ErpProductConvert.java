package cn.iocoder.yudao.module.erp.convert.product;

import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ErpProductConvert {

    ErpProductConvert INSTANCE = Mappers.getMapper(ErpProductConvert.class);


    ErpProductDTO convert(ErpProductDO erpProductDO);

    default List<ErpProductDTO> convert(List<ErpProductDO> erpProductDOs) {
        if (erpProductDOs != null) {
            return erpProductDOs.stream().map(this::convert).toList();
        }
        return null;
    }

}
