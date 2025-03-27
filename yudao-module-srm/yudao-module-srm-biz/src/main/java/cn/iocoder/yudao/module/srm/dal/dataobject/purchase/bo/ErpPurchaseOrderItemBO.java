package cn.iocoder.yudao.module.srm.dal.dataobject.purchase.bo;

import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import lombok.Data;

@Data
public class ErpPurchaseOrderItemBO extends ErpPurchaseOrderItemDO {


    /**
     * 一个订单项：一个订单
     */
    ErpPurchaseOrderDO erpPurchaseOrderDO;
}
