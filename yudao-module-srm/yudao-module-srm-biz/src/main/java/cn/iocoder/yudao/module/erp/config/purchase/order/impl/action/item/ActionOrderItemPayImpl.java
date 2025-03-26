package cn.iocoder.yudao.module.erp.config.purchase.order.impl.action.item;

import cn.iocoder.yudao.module.erp.api.purchase.SrmPayCountDTO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.iocoder.yudao.module.erp.enums.SrmEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpPaymentStatus;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static cn.iocoder.yudao.module.erp.enums.SrmStateMachines.PURCHASE_ORDER_PAYMENT_STATE_MACHINE_NAME;

@Component
@Slf4j
public class ActionOrderItemPayImpl implements Action<ErpPaymentStatus, SrmEventEnum, SrmPayCountDTO> {
    @Resource
    private ErpPurchaseOrderItemMapper itemMapper;
    @Resource
    private ErpPurchaseOrderMapper mapper;
    @Resource(name = PURCHASE_ORDER_PAYMENT_STATE_MACHINE_NAME)
    private StateMachine<ErpPaymentStatus, SrmEventEnum, ErpPurchaseOrderDO> paymentStateMachine;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(ErpPaymentStatus f, ErpPaymentStatus t, SrmEventEnum event, SrmPayCountDTO context) {
        // 支付状态机执行
        ErpPurchaseOrderItemDO orderItemDO = itemMapper.selectById(context.getOrderItemId());
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
                t = ErpPaymentStatus.ALL_PAYMENT;
            } else if (orderItemDO.getPayPrice().compareTo(BigDecimal.ZERO) == 0) {
                t = ErpPaymentStatus.NONE_PAYMENT;
            } else {
                t = ErpPaymentStatus.PARTIALLY_PAYMENT;
            }
        }
        orderItemDO.setPayStatus(t.getCode());
        itemMapper.updateById(orderItemDO);
        // 3. 记录日志
        log.debug("订单子项支付状态机触发({})事件：对象ID={}，状态 {} -> {}", event.getDesc(), orderItemDO.getId(), f.getDesc(), t.getDesc());

        // 根据订单项ID查询订单信息
        ErpPurchaseOrderDO orderDO = mapper.selectById(orderItemDO.getOrderId());
        if (orderDO == null) {
            log.error("未找到对应的采购订单,订单ID={}", orderItemDO.getOrderId());
            return;
        }
        paymentStateMachine.fireEvent(ErpPaymentStatus.fromCode(orderDO.getPayStatus()), event, orderDO);
    }
}
