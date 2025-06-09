package cn.iocoder.yudao.module.srm.service.purchase.bo.in;

import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import lombok.Data;

import java.util.List;

@Data
public class SrmPurchaseInBO extends SrmPurchaseInDO {

    // 采购单子表
    private List<SrmPurchaseInItemDO> srmPurchaseInItemDOS;
}
