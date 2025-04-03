package cn.iocoder.yudao.module.srm.config.purchase.order.impl.action.item;

import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.module.srm.api.purchase.SrmPayCountDTO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmOffStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmPaymentStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmStorageStatus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_ORDER_ITEM_OFF_STATE_MACHINE_NAME;
import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.PURCHASE_ORDER_PAYMENT_STATE_MACHINE_NAME;

@Component
@Slf4j
public class OrderItemPayActionImpl implements Action<SrmPaymentStatus, SrmEventEnum, SrmPayCountDTO> {
    @Resource
    private SrmPurchaseOrderItemMapper itemMapper;
    @Resource
    private SrmPurchaseOrderMapper mapper;
    @Resource(name = PURCHASE_ORDER_PAYMENT_STATE_MACHINE_NAME)
    private StateMachine<SrmPaymentStatus, SrmEventEnum, SrmPurchaseOrderDO> paymentStateMachine;
    @Resource(name = PURCHASE_ORDER_ITEM_OFF_STATE_MACHINE_NAME)
    private StateMachine<SrmOffStatus, SrmEventEnum, SrmPurchaseOrderItemDO> purchaseOrderItemOffStateMachine;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(SrmPaymentStatus f, SrmPaymentStatus t, SrmEventEnum event, SrmPayCountDTO context) {
        // 支付状态机执行
        SrmPurchaseOrderItemDO orderItemDO = itemMapper.selectById(context.getOrderItemId());
        // 1. 校验参数
        if (orderItemDO == null) {
            return;
        }

        // 2. 更新支付状态
        if (event == SrmEventEnum.PAYMENT_ADJUSTMENT) {
            // 2.1 更新支付金额
            if (context.getPayCountDiff() != null) {
                BigDecimal oldMoney = orderItemDO.getPayPrice() == null ? BigDecimal.ZERO : orderItemDO.getPayPrice();
                BigDecimal newMoney = context.getPayCountDiff();
                //最终金额 = 原金额 + 调整金额
                orderItemDO.setPayPrice(oldMoney.add(newMoney));
            }
            //根据支付金额 动态判断当前支付状态, 如果支付金额大于等于订单金额，则支付状态为已支付。如果支付金额为0，则支付状态为未支付。其余是部分支付。
            if (orderItemDO.getPayPrice().compareTo(orderItemDO.getTotalPrice()) >= 0) {
                t = SrmPaymentStatus.ALL_PAYMENT;
            } else if (orderItemDO.getPayPrice().compareTo(BigDecimal.ZERO) == 0) {
                t = SrmPaymentStatus.NONE_PAYMENT;
            } else {
                t = SrmPaymentStatus.PARTIALLY_PAYMENT;
            }
        }
        orderItemDO.setPayStatus(t.getCode());
        itemMapper.updateById(orderItemDO);
        // 3. 记录日志
        log.debug("订单子项支付状态机触发({})事件：对象ID={}，状态 {} -> {}", event.getDesc(), orderItemDO.getId(), f.getDesc(), t.getDesc());

        // 根据订单项ID查询订单信息
        SrmPurchaseOrderDO orderDO = mapper.selectById(orderItemDO.getOrderId());
        if (orderDO == null) {
            log.error("未找到对应的采购订单,订单ID={}", orderItemDO.getOrderId());
            return;
        }
        //不等于初始化
        if (event != SrmEventEnum.PAYMENT_INIT) {
            paymentStateMachine.fireEvent(SrmPaymentStatus.fromCode(orderDO.getPayStatus()), SrmEventEnum.PAYMENT_ADJUSTMENT, orderDO);
        }
        //当前订单项，完全入库 + 完全付款 -> 关闭订单项
        checkStatusAndClose(orderItemDO.getId());
    }

    private void checkStatusAndClose(Long orderItemId) {
        SrmPurchaseOrderItemDO orderItemDO = itemMapper.selectById(orderItemId);
        if (Objects.equals(orderItemDO.getInStatus(), SrmStorageStatus.ALL_IN_STORAGE.getCode()) && Objects.equals(orderItemDO.getPayStatus(),
            SrmPaymentStatus.ALL_PAYMENT.getCode())) {
            // 当前订单项，完全入库 + 完全付款 -> 关闭订单项
            purchaseOrderItemOffStateMachine.fireEvent(SrmOffStatus.fromCode(orderItemDO.getOffStatus()), SrmEventEnum.AUTO_CLOSE, orderItemDO);
        }
    }
}
