package cn.iocoder.yudao.module.srm.service.purchase.bo.order;

import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import lombok.Data;

import java.util.List;

/**
 * SrmPurchaseOrderDO + SrmPurchaseOrderItemDO 构建对象
 */
@Data
public class SrmPurchaseOrderBO extends SrmPurchaseOrderDO {

    /**
     * 1订单：N订单项
     */
    private List<SrmPurchaseOrderItemDO> srmPurchaseOrderItemDOS;
}
