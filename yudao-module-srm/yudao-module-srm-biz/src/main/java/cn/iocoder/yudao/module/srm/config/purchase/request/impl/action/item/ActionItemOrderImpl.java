package cn.iocoder.yudao.module.srm.config.purchase.request.impl.action.item;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.srm.api.purchase.TmsOrderCountDTO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.SrmStateMachines;
import cn.iocoder.yudao.module.srm.enums.status.ErpOrderStatus;
import cn.iocoder.yudao.module.srm.service.purchase.ErpPurchaseRequestService;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

//订购子表状态机
@Component
@Slf4j
public class ActionItemOrderImpl implements Action<ErpOrderStatus, SrmEventEnum, TmsOrderCountDTO> {
    @Autowired
    @Lazy
    ErpPurchaseRequestMapper requestMapper;
    @Resource(name = SrmStateMachines.PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME)
    @Lazy
    StateMachine<ErpOrderStatus, SrmEventEnum, ErpPurchaseRequestDO> requestStateMachine;
    
    @Autowired
    private ErpPurchaseRequestItemsMapper itemsMapper;
    @Autowired
    @Lazy
    ErpPurchaseRequestService erpPurchaseRequestService;

    @Override
    @Transactional
    public void execute(ErpOrderStatus from, ErpOrderStatus to, SrmEventEnum event, TmsOrderCountDTO context) {
        //更新采购申请项的下单数量
        //更新采购申请项的采购状态(暂无)
        ErpPurchaseRequestItemsDO itemDO = null;
        if (Optional.ofNullable(context.getPurchaseRequestItemId()).isPresent()) {
            itemDO = erpPurchaseRequestService.validItemIdExist(context.getPurchaseRequestItemId());
        }
        if (event == SrmEventEnum.ORDER_ADJUSTMENT) {
            //采购数量变更->采购状态
            int oldCount = (itemDO.getOrderedQuantity() == null ? 0 : itemDO.getOrderedQuantity());
            int newCount = 0;
            if (context.getQuantity() > 0) {
                //新增订购数量
                newCount = oldCount + context.getQuantity();
            } else if (context.getQuantity() < 0) {
                newCount = oldCount + context.getQuantity();//最终订购数量
            } else {
                newCount = oldCount;
            }
            itemDO.setOrderedQuantity(newCount);
            //根据订购数量来判断是否完整订购,批准数量,状态
            if (newCount == 0) {
                itemDO.setOrderStatus(ErpOrderStatus.OT_ORDERED.getCode());// 状态设为未订购
            } else if (newCount > itemDO.getApproveCount()) {
                itemDO.setOrderStatus(ErpOrderStatus.ORDERED.getCode());// 状态设为全部订购
            } else if (newCount < itemDO.getApproveCount()) {
                itemDO.setOrderStatus(ErpOrderStatus.PARTIALLY_ORDERED.getCode());// 状态设为部分订购
            }
        } else {
            //初始化+失败，事件
            itemDO.setOrderStatus(to.getCode());
        }
        itemsMapper.updateById(itemDO);
        //传递事件
        if (event == SrmEventEnum.ORDER_ADJUSTMENT) {
            ErpPurchaseRequestDO requestDO = requestMapper.selectById(itemDO.getRequestId());
            requestStateMachine.fireEvent(ErpOrderStatus.fromCode(requestDO.getOrderStatus()), SrmEventEnum.ORDER_ADJUSTMENT, requestDO);
        }

        log.debug("item订购状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), ErpOrderStatus.fromCode(itemDO.getOrderStatus()).getDesc());
    }
}
