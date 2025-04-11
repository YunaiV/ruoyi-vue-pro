package cn.iocoder.yudao.module.srm.dal.dataobject.purchase.bo;

import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestItemsDO;
import lombok.Data;

import java.util.List;

@Data
public class SrmPurchaseRequestBO extends SrmPurchaseRequestDO {

    private List<SrmPurchaseRequestItemsDO> items;
}
