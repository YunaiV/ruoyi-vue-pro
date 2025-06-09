package cn.iocoder.yudao.module.srm.config.purchase.returned.impl.action.item;


import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.module.srm.config.machine.outItem.SrmPurchaseOutItemCountContext;
import cn.iocoder.yudao.module.srm.config.machine.outItem.SrmPurchaseOutMachineContext;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnItemDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseInItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseReturnItemMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmOutboundStatus;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_RETURN_OUT_STORAGE_STATE_MACHINE_NAME;

@Slf4j
@Component
public class OutboundItemActionImpl implements Action<SrmOutboundStatus, SrmEventEnum, SrmPurchaseOutItemCountContext> {

    @Resource(name = PURCHASE_RETURN_OUT_STORAGE_STATE_MACHINE_NAME)
    StateMachine<SrmOutboundStatus, SrmEventEnum, SrmPurchaseOutMachineContext> outnoundStateMachine;
    @Autowired
    private SrmPurchaseReturnItemMapper srmPurchaseReturnItemMapper;
    @Autowired
    @Lazy
    private SrmPurchaseOrderService srmPurchaseOrderService;
    @Autowired
    private SrmPurchaseInItemMapper srmPurchaseInItemMapper;
    @Autowired
    private SrmPurchaseOrderItemMapper srmPurchaseOrderItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(SrmOutboundStatus from, SrmOutboundStatus to, SrmEventEnum event, SrmPurchaseOutItemCountContext context) {
        SrmPurchaseReturnItemDO returnItemDO = srmPurchaseReturnItemMapper.selectById(context.getOutItemId());

        if (event == SrmEventEnum.OUT_STORAGE_ADJUSTMENT) {
            if (context.getOutCount() == null) {
                throw new IllegalArgumentException("退货数量调整事件下，dto退货数量不能为空");
            }
            //实际退货数量
            BigDecimal outboundQty = returnItemDO.getOutboundQty();
            if (outboundQty == null) {
                outboundQty = BigDecimal.ZERO;
            }
            BigDecimal changedOutboundQty = outboundQty.add(context.getOutCount());
            //计划退货数量
            BigDecimal qty = returnItemDO.getQty();

            // 根据实际退货数量确定状态
            if (changedOutboundQty.compareTo(BigDecimal.ZERO) <= 0) {
                // 实际退货数量为0或负数，设置为未出库状态
                to = SrmOutboundStatus.NONE_OUTBOUND;
            } else if (changedOutboundQty.compareTo(qty) >= 0) {
                // 实际退货数量大于等于计划数量，设置为已出库状态
                to = SrmOutboundStatus.ALL_OUTBOUND;
            } else {
                // 实际退货数量小于计划数量，设置为部分出库状态
                to = SrmOutboundStatus.PARTIALLY_OUTBOUND;
            }
            //出库数量
            returnItemDO.setOutboundQty(changedOutboundQty);
            // 1.0 传递给主单
            outnoundStateMachine.fireEvent(SrmOutboundStatus.NONE_OUTBOUND, SrmEventEnum.OUT_STORAGE_ADJUSTMENT, SrmPurchaseOutMachineContext.builder().returnId(returnItemDO.getReturnId()).build());
            // 2.0 传给订单项，同步当前的退货数量给订单项(暂不使用状态机)
            toOrderReturnCount(returnItemDO);
        }
        returnItemDO.setOutboundStatus(to.getCode());
        srmPurchaseReturnItemMapper.updateById(returnItemDO);//更新状态
        //log
        log.debug("子项退货状态机触发({})事件：对象outItemId={}，状态 {} -> {}", event.getDesc(), returnItemDO.getId(), from.getDesc(), to.getDesc());
    }

    /**
     * 1.改变订单项退货数量
     */
    private void toOrderReturnCount(SrmPurchaseReturnItemDO returnItemDO) {
        //到货项 -> 订单项
        SrmPurchaseInItemDO inItemDO = srmPurchaseInItemMapper.selectById(returnItemDO.getArriveItemId());
        SrmPurchaseOrderItemDO srmPurchaseOrderItemDO = srmPurchaseOrderItemMapper.selectById(inItemDO.getOrderItemId());

        // 获取已有的退货数量
        BigDecimal existingReturnCount = srmPurchaseOrderItemDO.getReturnCount();
        if (existingReturnCount == null) {
            existingReturnCount = BigDecimal.ZERO;
        }

        // 计算新的退货数量 = 已有退货数量 + 当前出库数量
        BigDecimal newReturnCount = existingReturnCount.add(returnItemDO.getOutboundQty());

        // 更新订单退货数量
        srmPurchaseOrderService.updatePurchaseOrderReturnCount(srmPurchaseOrderItemDO.getOrderId(), Map.of(returnItemDO.getArriveItemId(), newReturnCount));
        log.debug("采购退货状态机,订单退货数量更新：订单项ID={}，退货数量 {} -> {}", srmPurchaseOrderItemDO.getId(), existingReturnCount, newReturnCount);
    }
}
