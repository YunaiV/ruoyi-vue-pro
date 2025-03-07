package cn.iocoder.yudao.module.erp.config.purchase.request.impl.action.item;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpOrderStatus;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.StateMachine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static cn.iocoder.yudao.module.erp.enums.ErpStateMachines.PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME;

//订购子表状态机
@Component
@Slf4j
public class ActionItemOrderImpl implements Action<ErpOrderStatus, ErpEventEnum, ErpPurchaseRequestItemsDO> {
    @Autowired
    private ErpPurchaseRequestItemsMapper itemsMapper;
    @Autowired
    ErpPurchaseRequestMapper requestMapper;
    @Autowired
    @Qualifier(PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME)
    StateMachine<ErpOrderStatus, ErpEventEnum, ErpPurchaseRequestDO> stateMachine;

    @Override
    @Transactional
    public void execute(ErpOrderStatus from, ErpOrderStatus to, ErpEventEnum event, ErpPurchaseRequestItemsDO context) {
        //更新采购申请项的下单数量
        //更新采购申请项的采购状态(暂无)
        Long purchaseOrderItemId = context.getPurchaseOrderItemId();
        ErpPurchaseRequestItemsDO itemsDO = itemsMapper.selectById(purchaseOrderItemId);
        //新增订单下单数量
        int i = (itemsDO.getOrderedQuantity() == null ? 0 : itemsDO.getOrderedQuantity()) + context.getOrderedQuantity();
        itemsDO.setOrderedQuantity(i);
        itemsMapper.updateById(itemsDO);
    }


}
