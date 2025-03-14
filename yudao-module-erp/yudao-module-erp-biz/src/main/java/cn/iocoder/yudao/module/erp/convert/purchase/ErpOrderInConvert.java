package cn.iocoder.yudao.module.erp.convert.purchase;

import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInSaveReqVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ErpOrderInConvert {

    ErpOrderInConvert INSTANCE = Mappers.getMapper(ErpOrderInConvert.class);


    default ErpPurchaseInSaveReqVO convertToErpPurchaseInSaveReqVO() {

        return null;
    }


    default ErpPurchaseInSaveReqVO.Item convertToErpPurchaseInSaveReqVOItem() {

        return null;
    }
}
