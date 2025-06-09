package cn.iocoder.yudao.module.srm.service.purchase.refund;

import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnItemDO;
import lombok.Data;

@Data
public class SrmPurchaseReturnItemBO extends SrmPurchaseReturnItemDO {

    private SrmPurchaseReturnDO srmPurchaseReturnDO;
}
