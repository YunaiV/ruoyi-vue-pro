package cn.iocoder.yudao.module.erp.config.purchase.request.impl.action.item;

import cn.iocoder.yudao.module.erp.api.purchase.ErpOrderCountDTO;
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

import static cn.iocoder.yudao.module.erp.enums.ErpStateMachines.PURCHASE_ORDER_STATE_MACHINE_NAME;

//订购子表状态机
@Component
@Slf4j
public class ActionItemOrderImpl implements Action<ErpOrderStatus, ErpEventEnum, ErpOrderCountDTO> {
    @Autowired
    private ErpPurchaseRequestItemsMapper mapper;
    @Autowired
    ErpPurchaseRequestMapper requestMapper;
    @Autowired
    @Qualifier(PURCHASE_ORDER_STATE_MACHINE_NAME)
    StateMachine<ErpOrderStatus, ErpEventEnum, ErpPurchaseRequestDO> stateMachine;

    @Override
    @Transactional
    public void execute(ErpOrderStatus from, ErpOrderStatus to, ErpEventEnum event, ErpOrderCountDTO context) {
        //更新采购申请项的下单数量
        //更新采购申请项的采购状态(暂无)
        Long purchaseOrderItemId = context.getPurchaseOrderItemId();
        ErpPurchaseRequestItemsDO itemsDO = mapper.selectById(purchaseOrderItemId);
        mapper.updateById(itemsDO);
    }


}
