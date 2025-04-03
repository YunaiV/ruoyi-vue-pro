package cn.iocoder.yudao.module.srm.config.purchase.order.impl.action.item;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
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

import java.util.Optional;

import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_ORDER_EXECUTION_STATE_MACHINE_NAME;

@Component
@Slf4j
public class OrderItemExecuteActionImpl implements Action<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderItemDO> {

    @Resource
    SrmPurchaseOrderItemMapper mapper;
    @Resource(name = PURCHASE_ORDER_EXECUTION_STATE_MACHINE_NAME)
    StateMachine<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderDO> stateMachine;
    @Autowired
    @Lazy
    SrmPurchaseOrderService srmPurchaseOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(SrmExecutionStatus from, SrmExecutionStatus to, SrmEventEnum event, SrmPurchaseOrderItemDO context) {
        SrmPurchaseOrderItemDO aDo = mapper.selectById(context.getId());
        aDo.setExecuteStatus(to.getCode());
        mapper.updateById(aDo);
        //log
        log.debug("执行状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());

        //
        Optional.ofNullable(aDo.getOrderId()).ifPresent(orderId -> {
            if (event != SrmEventEnum.EXECUTION_INIT) {
                SrmPurchaseOrderDO purchaseOrder = srmPurchaseOrderService.getPurchaseOrder(orderId);
                stateMachine.fireEvent(SrmExecutionStatus.fromCode(purchaseOrder.getExecuteStatus()), event, purchaseOrder);
            }
        });
    }
}
