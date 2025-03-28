package cn.iocoder.yudao.module.srm.config.purchase.in;

import cn.iocoder.yudao.module.srm.config.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.srm.config.purchase.in.impl.action.item.InPayItemActionImpl;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.SrmStateMachines;
import cn.iocoder.yudao.module.srm.enums.status.SrmPaymentStatus;
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
public class SrmPurchaseInItemStatusMachine {
    @Resource
    InPayItemActionImpl inPayItemActionImpl;
    @Resource
    private BaseFailCallbackImpl baseFailCallbackImpl;

    @Bean(SrmStateMachines.PURCHASE_IN_ITEM_PAYMENT_STATE_MACHINE)
    public StateMachine<SrmPaymentStatus, SrmEventEnum, SrmPurchaseInItemDO> getPurchaseRequestPaymentStateMachine() {
        StateMachineBuilder<SrmPaymentStatus, SrmEventEnum, SrmPurchaseInItemDO> builder = StateMachineBuilderFactory.create();
        //初始化
        builder.internalTransition()
            .within(SrmPaymentStatus.NONE_PAYMENT)
            .on(SrmEventEnum.PAYMENT_INIT)
            .perform(inPayItemActionImpl);
        //付款金额调整
        builder.externalTransition()
            .from(SrmPaymentStatus.NONE_PAYMENT)
            .to(SrmPaymentStatus.PARTIALLY_PAYMENT)
            .on(SrmEventEnum.PAYMENT_ADJUSTMENT)
            .perform(inPayItemActionImpl);
        builder.externalTransition()
            .from(SrmPaymentStatus.ALL_PAYMENT)
            .to(SrmPaymentStatus.ALL_PAYMENT)
            .on(SrmEventEnum.PAYMENT_ADJUSTMENT)
            .perform(inPayItemActionImpl);
        builder.externalTransition()
            .from(SrmPaymentStatus.PARTIALLY_PAYMENT)
            .to(SrmPaymentStatus.ALL_PAYMENT)
            .on(SrmEventEnum.PAYMENT_ADJUSTMENT)
            .perform(inPayItemActionImpl);
        //付款失败事件
        builder.externalTransition()
            .from(SrmPaymentStatus.NONE_PAYMENT)
            .to(SrmPaymentStatus.PAYMENT_EXCEPTION)
            .on(SrmEventEnum.PAYMENT_EXCEPTION)
            .perform(inPayItemActionImpl);
        builder.externalTransition()
            .from(SrmPaymentStatus.PARTIALLY_PAYMENT)
            .to(SrmPaymentStatus.PAYMENT_EXCEPTION)
            .on(SrmEventEnum.PAYMENT_EXCEPTION)
            .perform(inPayItemActionImpl);
        //完成付款
        builder.externalTransition()
            .from(SrmPaymentStatus.NONE_PAYMENT)
            .to(SrmPaymentStatus.ALL_PAYMENT)
            .on(SrmEventEnum.COMPLETE_PAYMENT)
            .perform(inPayItemActionImpl);

        //取消付款
        builder.externalTransition()
            .from(SrmPaymentStatus.ALL_PAYMENT)
            .to(SrmPaymentStatus.NONE_PAYMENT)
            .on(SrmEventEnum.CANCEL_PAYMENT)
            .perform(inPayItemActionImpl);
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_IN_ITEM_PAYMENT_STATE_MACHINE);
    }
}
