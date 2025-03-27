package cn.iocoder.yudao.module.srm.dal.dataobject.purchase.bo;

import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderDO;
import lombok.Data;

import java.util.List;

/**
 * ErpPurchaseOrderDO + ErpPurchaseOrderItemDO 构建对象
 */
@Data
public class ErpPurchaseOrderBO extends ErpPurchaseOrderDO {

    /**
     * 1订单：N订单项
     */
    private List<ErpPurchaseOrderItemBO> erpPurchaseOrderItemBO;
}
