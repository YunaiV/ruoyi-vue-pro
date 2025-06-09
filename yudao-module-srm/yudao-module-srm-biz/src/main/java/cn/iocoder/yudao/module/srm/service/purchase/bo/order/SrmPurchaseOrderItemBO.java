package cn.iocoder.yudao.module.srm.service.purchase.bo.order;

import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import lombok.Data;

@Data
public class SrmPurchaseOrderItemBO extends SrmPurchaseOrderItemDO {


    /**
     * 一个订单项：一个订单
     */
    SrmPurchaseOrderDO srmPurchaseOrderDO;
}
