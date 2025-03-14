package cn.iocoder.yudao.module.erp.config.purchase.order.impl.action.item;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpPaymentStatus;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static cn.iocoder.yudao.module.erp.enums.ErpStateMachines.PURCHASE_ORDER_PAYMENT_STATE_MACHINE_NAME;

@Component
@Slf4j
public class ActionOrderItemPayImpl implements Action<ErpPaymentStatus, ErpEventEnum, ErpPurchaseOrderItemDO> {
    @Resource
    private ErpPurchaseOrderItemMapper itemMapper;
    @Resource
    private ErpPurchaseOrderMapper mapper;
    @Resource(name = PURCHASE_ORDER_PAYMENT_STATE_MACHINE_NAME)
    private StateMachine paymentStateMachine;


    @Override
    @Transactional( rollbackFor = Exception.class)
    public void execute(ErpPaymentStatus f, ErpPaymentStatus t, ErpEventEnum erpEventEnum,
                        ErpPurchaseOrderItemDO context) {
        // 支付状态机执行
        // 1. 校验参数
        if (context == null) {
            return;
        }

        // 2. 更新支付状态
        context.setPayStatus(t.getCode());
        itemMapper.updateById(context);
        // 3. 记录日志
        log.debug("子项支付状态机触发({})事件：对象ID={}，状态 {} -> {}",
            erpEventEnum.getDesc(),
            context.getId(),
            f.getDesc(),
            t.getDesc());

        // 4. 触发订单支付状态变更
        // 根据订单项ID查询订单信息
        ErpPurchaseOrderDO orderDO = mapper.selectById(context.getOrderId());
        if (orderDO == null) {
            log.error("未找到对应的采购订单,订单ID={}", context.getOrderId());
            return;
        }
//        paymentStateMachine.fireEvent(ErpPaymentStatus.fromCode(orderDO.getPayStatus()), erpEventEnum, orderDO);
    }
}
