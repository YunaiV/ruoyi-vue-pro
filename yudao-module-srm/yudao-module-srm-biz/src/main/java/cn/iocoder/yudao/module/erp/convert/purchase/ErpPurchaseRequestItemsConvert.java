package cn.iocoder.yudao.module.erp.convert.purchase;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 采购申请项->采购订单的订单项
 */
@Mapper
public interface ErpPurchaseRequestItemsConvert {
    ErpPurchaseRequestItemsConvert INSTANCE = Mappers.getMapper(ErpPurchaseRequestItemsConvert.class);


}
