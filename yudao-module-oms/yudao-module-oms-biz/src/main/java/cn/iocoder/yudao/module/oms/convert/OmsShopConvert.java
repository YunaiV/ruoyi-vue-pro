package cn.iocoder.yudao.module.oms.convert;


import cn.iocoder.yudao.module.oms.api.dto.OmsShopSaveReqDTO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/4/1$
 */
@Mapper
public interface OmsShopConvert {

    OmsShopConvert INSTANCE = Mappers.getMapper(OmsShopConvert.class);

    OmsShopDO convert(OmsShopSaveReqDTO saveReqDTO);
}
