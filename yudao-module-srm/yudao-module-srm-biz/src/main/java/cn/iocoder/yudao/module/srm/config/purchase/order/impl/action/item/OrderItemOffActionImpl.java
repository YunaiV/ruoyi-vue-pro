package cn.iocoder.yudao.module.srm.config.purchase.order.impl.action.item;

import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.srm.config.machine.order.SrmOrderItemOffContext;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderMapper;
import cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.SrmStateMachines;
import cn.iocoder.yudao.module.srm.enums.status.SrmOffStatus;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class OrderItemOffActionImpl implements Action<SrmOffStatus, SrmEventEnum, SrmOrderItemOffContext> {

    @Resource(name = SrmStateMachines.PURCHASE_ORDER_OFF_STATE_MACHINE_NAME)
    @Lazy
    StateMachine<SrmOffStatus, SrmEventEnum, SrmPurchaseOrderDO> orderOffStateMachine;
    @Autowired
    private SrmPurchaseOrderItemMapper itemMapper;
    @Autowired
    @Lazy
    private SrmPurchaseOrderService purchaseOrderService;
    @Autowired
    private SrmPurchaseOrderMapper orderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(SrmOffStatus from, SrmOffStatus to, SrmEventEnum event, SrmOrderItemOffContext context) {
        // 查询采购订单子项
        SrmPurchaseOrderItemDO itemDO = purchaseOrderService.validatePurchaseOrderItemExists(context.getItemId());
        if(itemDO == null) {
            log.warn("采购订单子项不存在，ID: {}", context.getItemId());
            return;
        }
        // 更新子项的状态
        itemDO.setOffStatus(to.getCode());
        itemMapper.updateById(itemDO);
        log.debug("子项开关状态机触发({})事件：对象ID={}，状态 {} -> {}", event.getDesc(), itemDO.getId(), from.getDesc(), to.getDesc());

        SrmPurchaseOrderDO orderDO = orderMapper.selectById(itemDO.getOrderId());
        if(orderDO == null) {
            log.warn("订单子项ID={} 不存在，无法更新主表状态", context.getItemId());
            return;
        }
        updateOrderStatusIfNeeded(orderDO, event, to);
    }

    /**
     * 根据所有子项的状态，决定是否传递状态事件
     */
    @Transactional()
    public void updateOrderStatusIfNeeded(SrmPurchaseOrderDO orderDO, SrmEventEnum event, SrmOffStatus to) {
        Long orderId = orderDO.getId();
        List<SrmPurchaseOrderItemDO> itemList = itemMapper.selectListByOrderId(orderId);

        if(itemList.isEmpty()) {
            log.warn("采购订单ID={} 没有子项，无法更新主表状态", orderId);
            return;
        }

        // **如果所有子项都关闭了，则传递事件给主表**
        boolean allClosed = itemList.stream().noneMatch(item -> item.getOffStatus().equals(SrmOffStatus.OPEN.getCode()));
        boolean hasOpen = itemList.stream().anyMatch(item -> item.getOffStatus().equals(SrmOffStatus.OPEN.getCode()));

        SrmPurchaseOrderDO aDo = orderMapper.selectById(orderDO.getId());
        if(aDo == null) {
            log.error("采购申请单不存在，ID: {}", orderDO.getId());
            throw ServiceExceptionUtil.exception(SrmErrorCodeConstants.PURCHASE_ORDER_NOT_EXISTS, orderDO.getId());
        }

        if(allClosed) {
            //主表是开启状态
            if(aDo.getOffStatus().equals(SrmOffStatus.OPEN.getCode())) {
                orderOffStateMachine.fireEvent(SrmOffStatus.fromCode(aDo.getOffStatus()), event, aDo);
            }
        } else if(hasOpen) {
            //主表不是开启状态
            if(!aDo.getOffStatus().equals(SrmOffStatus.OPEN.getCode())) {
                orderOffStateMachine.fireEvent(SrmOffStatus.fromCode(aDo.getOffStatus()), event, aDo);
            }
        }
    }
}
