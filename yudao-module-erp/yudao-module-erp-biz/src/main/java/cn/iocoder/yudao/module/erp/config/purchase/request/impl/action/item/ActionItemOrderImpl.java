package cn.iocoder.yudao.module.erp.config.purchase.request.impl.action.item;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.ErpStateMachines;
import cn.iocoder.yudao.module.erp.enums.status.ErpOrderStatus;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.module.erp.enums.ErpStateMachines.PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME;

//订购子表状态机
@Component
@Slf4j
public class ActionItemOrderImpl implements Action<ErpOrderStatus, ErpEventEnum, ErpPurchaseRequestItemsDO> {
    @Autowired
    private ErpPurchaseRequestItemsMapper itemsMapper;
    @Autowired
    ErpPurchaseRequestMapper requestMapper;
    @Resource(name = PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME)
    StateMachine<ErpOrderStatus, ErpEventEnum, ErpPurchaseRequestDO> stateMachine;
    @Resource(name = ErpStateMachines.PURCHASE_REQUEST_ITEM_STATE_MACHINE_NAME)
    StateMachine<ErpOrderStatus, ErpEventEnum, ErpPurchaseRequestItemsDO> itemsDOStateMachine;
    @Resource(name = ErpStateMachines.PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME)
    StateMachine<ErpOrderStatus, ErpEventEnum, ErpPurchaseRequestDO> requestStateMachine;

    @Override
    @Transactional
    public void execute(ErpOrderStatus from, ErpOrderStatus to, ErpEventEnum event, ErpPurchaseRequestItemsDO context) {
        //更新采购申请项的下单数量
        //更新采购申请项的采购状态(暂无)
        Long purchaseOrderItemId = context.getPurchaseOrderItemId();
        ErpPurchaseRequestItemsDO itemsDO = itemsMapper.selectById(purchaseOrderItemId);
        //下单数量变更
        int i = (itemsDO.getOrderedQuantity() == null ? 0 : itemsDO.getOrderedQuantity());
        itemsDO.setOrderedQuantity(i);
        //已订购数量 == 申请数量，传递事件
        if (Objects.equals(itemsDO.getCount(), itemsDO.getOrderedQuantity())) {
            //子订单已完全采购
            itemsDOStateMachine.fireEvent(ErpOrderStatus.fromCode(itemsDO.getOffStatus()), ErpEventEnum.PURCHASE_ADJUSTMENT, itemsDO);
            //检验申请单下的所有子表是否符合传递事件的需求
            checkRequest(event, itemsDO);
        }
        itemsDO.setOrderStatus(to.getCode());
        itemsMapper.updateById(itemsDO);
    }

    //检验申请单下的所有子表是否符合传递事件的需求
    private void checkRequest(ErpEventEnum event, ErpPurchaseRequestItemsDO itemsDO) {
        //子采购完成
        List<ErpPurchaseRequestItemsDO> itemList = itemsMapper.selectListByRequestId(itemsDO.getRequestId());
        boolean allClosed = itemList.stream().noneMatch(item -> item.getOrderStatus().equals(ErpOrderStatus.ORDERED.getCode()));
        ErpPurchaseRequestDO requestDO = requestMapper.selectById(itemsDO.getRequestId());
        if (allClosed) {
            requestStateMachine.fireEvent(ErpOrderStatus.fromCode(requestDO.getOrderStatus()), ErpEventEnum.PURCHASE_COMPLETE, requestDO);
        }

    }


}
