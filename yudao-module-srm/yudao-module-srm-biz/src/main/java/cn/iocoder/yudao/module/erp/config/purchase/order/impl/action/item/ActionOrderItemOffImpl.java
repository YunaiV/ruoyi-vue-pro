package cn.iocoder.yudao.module.erp.config.purchase.order.impl.action.item;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.iocoder.yudao.module.erp.enums.SrmErrorCodeConstants;
import cn.iocoder.yudao.module.erp.enums.SrmEventEnum;
import cn.iocoder.yudao.module.erp.enums.SrmStateMachines;
import cn.iocoder.yudao.module.erp.enums.status.ErpOffStatus;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class ActionOrderItemOffImpl implements Action<ErpOffStatus, SrmEventEnum, ErpPurchaseOrderItemDO> {

    @Resource(name = SrmStateMachines.PURCHASE_ORDER_OFF_STATE_MACHINE_NAME)
    StateMachine<ErpOffStatus, SrmEventEnum, ErpPurchaseOrderDO> orderOffStateMachine;
    @Autowired
    private ErpPurchaseOrderItemMapper itemMapper;
    @Autowired
    private ErpPurchaseOrderMapper orderMapper;

    @Override
    @Transactional( rollbackFor = Exception.class)
    public void execute(ErpOffStatus from, ErpOffStatus to, SrmEventEnum event, ErpPurchaseOrderItemDO context) {
        // 查询采购订单子项
        ErpPurchaseOrderItemDO itemDO = itemMapper.selectById(context.getId());
        if (itemDO == null) {
            log.warn("采购订单子项不存在，ID: {}", context.getId());
            return;
        }

        // 更新子项的状态
        itemDO.setOffStatus(to.getCode());
        itemMapper.updateById(itemDO);
        log.info("子项开关状态机触发({})事件：对象ID={}，状态 {} -> {}", event.getDesc(), itemDO.getId(), from.getDesc(), to.getDesc());

        ErpPurchaseOrderDO orderDO = orderMapper.selectById(context.getOrderId());
        if (orderDO == null) {
            log.warn("子项ID={} 关联的订单ID={} 不存在，无法更新主表状态", context.getId(), context.getOrderId());
            return;
        }

        updateOrderStatusIfNeeded(orderDO, event, to);
    }

    /**
     * 根据所有子项的状态，决定是否更新主表状态
     */
    @Transactional()
    public void updateOrderStatusIfNeeded(ErpPurchaseOrderDO orderDO, SrmEventEnum event, ErpOffStatus to) {
        Long orderId = orderDO.getId();
        List<ErpPurchaseOrderItemDO> itemList = itemMapper.selectListByOrderId(orderId);

        if (itemList.isEmpty()) {
            log.warn("采购订单ID={} 没有子项，无法更新主表状态", orderId);
            return;
        }


        // **如果所有子项都关闭了，则传递事件给主表**
        boolean allClosed = itemList.stream().noneMatch(item -> item.getOffStatus().equals(ErpOffStatus.OPEN.getCode()));
        boolean hasOpen = itemList.stream().anyMatch(item -> item.getOffStatus().equals(ErpOffStatus.OPEN.getCode()));

        ErpPurchaseOrderDO aDo = orderMapper.selectById(orderDO.getId());
        if (aDo == null) {
            log.error("采购申请单不存在，ID: {}", orderDO.getId());
            throw ServiceExceptionUtil.exception(SrmErrorCodeConstants.PURCHASE_ORDER_NOT_EXISTS, orderDO.getId());
        }

        if (allClosed) {
            //主表是开启状态
            if (aDo.getOffStatus().equals(ErpOffStatus.OPEN.getCode())) {
                orderOffStateMachine.fireEvent(ErpOffStatus.fromCode(aDo.getOffStatus()), event, aDo);
            }
        } else if (hasOpen) {
            //主表不是开启状态
            if (!aDo.getOffStatus().equals(ErpOffStatus.OPEN.getCode())) {
                orderOffStateMachine.fireEvent(ErpOffStatus.fromCode(aDo.getOffStatus()), event, aDo);
            }
        }
    }
}
