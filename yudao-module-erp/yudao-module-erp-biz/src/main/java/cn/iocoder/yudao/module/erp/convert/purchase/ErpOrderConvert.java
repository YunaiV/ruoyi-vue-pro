package cn.iocoder.yudao.module.erp.convert.purchase;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ErpOrderConvert {
    ErpOrderConvert INSTANCE = Mappers.getMapper(ErpOrderConvert.class);


    default ErpPurchaseOrderDO convertByPurchaseRequestDO(ErpPurchaseRequestDO requestDO) {
        if (requestDO == null) {
            return null;
        }

        ErpPurchaseOrderDO.ErpPurchaseOrderDOBuilder erpPurchaseOrderDO = ErpPurchaseOrderDO.builder();

        erpPurchaseOrderDO.no(requestDO.getNo());
        erpPurchaseOrderDO.status(requestDO.getStatus());
        erpPurchaseOrderDO.supplierId(requestDO.getSupplierId());
        erpPurchaseOrderDO.auditorId(requestDO.getAuditorId());
        erpPurchaseOrderDO.auditTime(requestDO.getAuditTime());

        return erpPurchaseOrderDO.build();
    }
}
