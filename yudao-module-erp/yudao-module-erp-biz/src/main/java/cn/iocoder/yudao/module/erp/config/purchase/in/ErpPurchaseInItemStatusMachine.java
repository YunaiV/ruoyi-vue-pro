package cn.iocoder.yudao.module.erp.config.purchase.in;

import cn.iocoder.yudao.module.erp.config.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.erp.config.purchase.in.impl.action.item.ActionInPayItemImpl;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.ErpStateMachines;
import cn.iocoder.yudao.module.erp.enums.status.ErpPaymentStatus;
import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//采购入库单主表状态机
@Slf4j
@Configuration
@SuppressWarnings({"rawtypes", "unchecked"})
public class ErpPurchaseInItemStatusMachine {
    @Resource
    ActionInPayItemImpl actionInPayItemImpl;
    @Resource
    private BaseFailCallbackImpl baseFailCallbackImpl;

    @Bean(ErpStateMachines.PURCHASE_IN_ITEM_PAYMENT_STATE_MACHINE)
    public StateMachine<ErpPaymentStatus, ErpEventEnum, ErpPurchaseInItemDO> getPurchaseRequestPaymentStateMachine() {
        StateMachineBuilder<ErpPaymentStatus, ErpEventEnum, ErpPurchaseInItemDO> builder = StateMachineBuilderFactory.create();
        //初始化
        builder.internalTransition()
            .within(ErpPaymentStatus.NONE_PAYMENT)
            .on(ErpEventEnum.PAYMENT_INIT)
            .perform(actionInPayItemImpl);
        //付款金额调整
        builder.externalTransition()
            .from(ErpPaymentStatus.NONE_PAYMENT)
            .to(ErpPaymentStatus.PARTIALLY_PAYMENT)
            .on(ErpEventEnum.PAYMENT_ADJUSTMENT)
            .perform(actionInPayItemImpl);
        builder.externalTransition()
            .from(ErpPaymentStatus.ALL_PAYMENT)
            .to(ErpPaymentStatus.ALL_PAYMENT)
            .on(ErpEventEnum.PAYMENT_ADJUSTMENT)
            .perform(actionInPayItemImpl);
        builder.externalTransition()
            .from(ErpPaymentStatus.PARTIALLY_PAYMENT)
            .to(ErpPaymentStatus.ALL_PAYMENT)
            .on(ErpEventEnum.PAYMENT_ADJUSTMENT)
            .perform(actionInPayItemImpl);
        //付款失败事件
        builder.externalTransition()
            .from(ErpPaymentStatus.NONE_PAYMENT)
            .to(ErpPaymentStatus.PAYMENT_EXCEPTION)
            .on(ErpEventEnum.PAYMENT_EXCEPTION)
            .perform(actionInPayItemImpl);
        builder.externalTransition()
            .from(ErpPaymentStatus.PARTIALLY_PAYMENT)
            .to(ErpPaymentStatus.PAYMENT_EXCEPTION)
            .on(ErpEventEnum.PAYMENT_EXCEPTION)
            .perform(actionInPayItemImpl);
        //完成付款
        builder.externalTransition()
            .from(ErpPaymentStatus.NONE_PAYMENT)
            .to(ErpPaymentStatus.ALL_PAYMENT)
            .on(ErpEventEnum.COMPLETE_PAYMENT)
            .perform(actionInPayItemImpl);

        //取消付款
        builder.externalTransition()
            .from(ErpPaymentStatus.ALL_PAYMENT)
            .to(ErpPaymentStatus.NONE_PAYMENT)
            .on(ErpEventEnum.CANCEL_PAYMENT)
            .perform(actionInPayItemImpl);
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(ErpStateMachines.PURCHASE_IN_ITEM_PAYMENT_STATE_MACHINE);
    }
}
