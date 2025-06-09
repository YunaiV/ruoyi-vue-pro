package cn.iocoder.yudao.module.srm.service.purchase.bo.in;

import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import lombok.Data;

@Data
public class SrmPurchaseInItemBO extends SrmPurchaseInItemDO {


    //入库单
    private SrmPurchaseInDO srmPurchaseInDO;
}
