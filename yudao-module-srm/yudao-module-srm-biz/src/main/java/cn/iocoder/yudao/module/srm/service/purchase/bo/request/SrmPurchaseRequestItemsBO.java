package cn.iocoder.yudao.module.srm.service.purchase.bo.request;

import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestItemsDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 采购申请项BO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SrmPurchaseRequestItemsBO extends SrmPurchaseRequestItemsDO {

    /**
     * 采购申请主表
     */
    private SrmPurchaseRequestDO purchaseRequest;
}
