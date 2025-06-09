package cn.iocoder.yudao.module.srm.config.purchase.order.impl.action.item;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseInItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmExecutionStatus;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_ORDER_EXECUTION_STATE_MACHINE_NAME;

@Component
@Slf4j
public class OrderItemExecuteActionImpl implements Action<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderItemDO> {

    @Autowired
    SrmPurchaseOrderItemMapper orderItemMapper;
    @Autowired
    SrmPurchaseInItemMapper inItemMapper;

    @Resource(name = PURCHASE_ORDER_EXECUTION_STATE_MACHINE_NAME)
    StateMachine<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderDO> stateMachine;
    @Autowired
    @Lazy
    SrmPurchaseOrderService srmPurchaseOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(SrmExecutionStatus from, SrmExecutionStatus to, SrmEventEnum event, SrmPurchaseOrderItemDO context) {
        SrmPurchaseOrderItemDO orderItemDO = orderItemMapper.selectById(context.getId());
        orderItemDO.setExecuteStatus(to.getCode());
        orderItemMapper.updateById(orderItemDO);
        //log
        log.debug("订单项执行状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
        if (event == SrmEventEnum.EXECUTION_ADJUSTMENT) {
            //1.0 查询关联到货项
            List<SrmPurchaseInItemDO> srmPurchaseInItemDOS = inItemMapper.selectListByOrderItemId(orderItemDO.getId());
            //2.0 汇总订单行到货数量 -> 根据到货数量变更执行状态
            BigDecimal totalInQty = srmPurchaseInItemDOS.stream()
                .map(SrmPurchaseInItemDO::getQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 根据到货数量判断执行状态
            if (totalInQty.compareTo(orderItemDO.getQty()) >= 0) {
                // 到货数量大于等于订单数量，设置为已完成
                to = SrmExecutionStatus.COMPLETED;
            } else if (totalInQty.compareTo(BigDecimal.ZERO) > 0) {
                // 到货数量大于0但小于订单数量，设置为执行中
                to = SrmExecutionStatus.IN_PROGRESS;
            } else {
                // 到货数量为0，设置为未开始
                to = SrmExecutionStatus.PENDING;
            }
        }

        orderItemDO.setExecuteStatus(to.getCode());
        orderItemMapper.updateById(orderItemDO);
        //执行状态调整 -> 给主单
        Optional.ofNullable(orderItemDO.getOrderId()).ifPresent(orderId -> {
            if (event != SrmEventEnum.EXECUTION_INIT) {
                SrmPurchaseOrderDO purchaseOrder = srmPurchaseOrderService.getPurchaseOrder(orderId);
                stateMachine.fireEvent(SrmExecutionStatus.fromCode(purchaseOrder.getExecuteStatus()), SrmEventEnum.EXECUTION_ADJUSTMENT, purchaseOrder);
            }
        });
    }
}
