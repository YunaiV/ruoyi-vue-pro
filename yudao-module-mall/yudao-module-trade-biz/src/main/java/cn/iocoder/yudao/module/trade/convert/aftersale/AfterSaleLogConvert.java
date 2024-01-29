package cn.iocoder.yudao.module.trade.convert.aftersale;

import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.AfterSaleLogDO;
import cn.iocoder.yudao.module.trade.service.aftersale.bo.AfterSaleLogCreateReqBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AfterSaleLogConvert {

    AfterSaleLogConvert INSTANCE = Mappers.getMapper(AfterSaleLogConvert.class);

    AfterSaleLogDO convert(AfterSaleLogCreateReqBO bean);

}
