package cn.iocoder.yudao.module.srm.convert.purchase;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 采购申请项->采购订单的订单项
 */
@Mapper
public interface SrmPurchaseRequestItemsConvert {
    SrmPurchaseRequestItemsConvert INSTANCE = Mappers.getMapper(SrmPurchaseRequestItemsConvert.class);


}
