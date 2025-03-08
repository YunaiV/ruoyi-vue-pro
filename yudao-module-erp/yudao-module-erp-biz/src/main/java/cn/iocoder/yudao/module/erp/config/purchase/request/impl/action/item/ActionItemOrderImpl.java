package cn.iocoder.yudao.module.erp.config.purchase.request.impl.action.item;

import cn.hutool.json.JSONUtil;
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

import static cn.iocoder.yudao.module.erp.enums.ErpStateMachines.PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME;

//订购子表状态机
@Component
@Slf4j
public class ActionItemOrderImpl implements Action<ErpOrderStatus, ErpEventEnum, ErpPurchaseRequestItemsDO> {
    @Autowired
    ErpPurchaseRequestMapper requestMapper;
    @Resource(name = PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME)
    StateMachine<ErpOrderStatus, ErpEventEnum, ErpPurchaseRequestDO> stateMachine;
    @Resource(name = ErpStateMachines.PURCHASE_REQUEST_ITEM_STATE_MACHINE_NAME)
    StateMachine<ErpOrderStatus, ErpEventEnum, ErpPurchaseRequestItemsDO> itemsDOStateMachine;
    @Resource(name = ErpStateMachines.PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME)
    StateMachine<ErpOrderStatus, ErpEventEnum, ErpPurchaseRequestDO> requestStateMachine;
    @Autowired
    private ErpPurchaseRequestItemsMapper itemsMapper;

    @Override
    @Transactional
    public void execute(ErpOrderStatus from, ErpOrderStatus to, ErpEventEnum event, ErpPurchaseRequestItemsDO context) {
        //更新采购申请项的下单数量
        //更新采购申请项的采购状态(暂无)
        ErpPurchaseRequestItemsDO itemsDO = itemsMapper.selectById(context.getId());
        if (event == ErpEventEnum.ORDER_ADJUSTMENT) {
            //采购数量变更->采购状态
            int oldCount = (itemsDO.getOrderedQuantity() == null ? 0 : itemsDO.getOrderedQuantity());
            int newCount = oldCount + context.getCount();//最终订购数量
            itemsDO.setOrderedQuantity(newCount);
            //根据订购数量来判断是否完整订购,批准数量,状态
            if (newCount == 0) {
                itemsDO.setOrderStatus(ErpOrderStatus.OT_ORDERED.getCode());// 状态设为未订购
//                //一个未订购->订单未订购?部分订购
//                List<ErpPurchaseRequestItemsDO> requestItemsDOS = itemsMapper.selectListByRequestId(itemsDO.getRequestId());
//                boolean hasOpen = requestItemsDOS.stream().anyMatch(item -> item.getOrderStatus().equals(ErpOrderStatus.PARTIALLY_ORDERED.getCode()));
//                if (hasOpen) {//部分订购
//                    ErpPurchaseRequestDO requestDO = requestMapper.selectById(itemsDO.getRequestId());
//                    requestMapper.updateById(requestDO.setOrderStatus(ErpOrderStatus.PARTIALLY_ORDERED.getCode()));
//                }
            } else if (newCount == itemsDO.getApproveCount()) {
                itemsDO.setOrderStatus(ErpOrderStatus.ORDERED.getCode());// 状态设为全部订购
            } else if (newCount < itemsDO.getApproveCount()) {
                itemsDO.setOrderStatus(ErpOrderStatus.PARTIALLY_ORDERED.getCode());// 状态设为部分订购
            }
        } else {
            //初始化+失败，事件
            itemsDO.setOrderStatus(to.getCode());
        }
        itemsMapper.updateById(itemsDO);
        //传递事件
        ErpPurchaseRequestDO requestDO = requestMapper.selectById(itemsDO.getRequestId());
        requestStateMachine.fireEvent(ErpOrderStatus.fromCode(requestDO.getOrderStatus()), ErpEventEnum.ORDER_ADJUSTMENT, requestDO);
        log.info("item订购状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), ErpOrderStatus.fromCode(itemsDO.getOrderStatus()).getDesc());
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
