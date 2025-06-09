package cn.iocoder.yudao.module.srm.service.purchase.refund;

import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnItemDO;
import lombok.Data;

import java.util.List;

@Data
public class SrmPurchaseReturnBO extends SrmPurchaseReturnDO {


    private List<SrmPurchaseReturnItemDO> SrmPurchaseReturnItemDOs;
}
