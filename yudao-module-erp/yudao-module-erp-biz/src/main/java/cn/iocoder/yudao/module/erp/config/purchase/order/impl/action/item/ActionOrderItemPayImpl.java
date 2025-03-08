package cn.iocoder.yudao.module.erp.config.purchase.order.impl.action.item;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpPaymentStatus;
import com.alibaba.cola.statemachine.Action;

public class ActionOrderItemPayImpl implements Action<ErpPaymentStatus, ErpEventEnum, ErpPurchaseRequestItemsDO> {
    @Override
    public void execute(ErpPaymentStatus erpPaymentStatus, ErpPaymentStatus s1, ErpEventEnum erpEventEnum, ErpPurchaseRequestItemsDO requestItemsDO) {

    }
}
