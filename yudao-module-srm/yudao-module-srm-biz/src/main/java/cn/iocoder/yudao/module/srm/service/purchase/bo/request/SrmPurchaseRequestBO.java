package cn.iocoder.yudao.module.srm.service.purchase.bo.request;

import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestItemsDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 采购申请BO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SrmPurchaseRequestBO extends SrmPurchaseRequestDO {

    /**
     * 采购申请项列表
     */
    private List<SrmPurchaseRequestItemsDO> items;
}
