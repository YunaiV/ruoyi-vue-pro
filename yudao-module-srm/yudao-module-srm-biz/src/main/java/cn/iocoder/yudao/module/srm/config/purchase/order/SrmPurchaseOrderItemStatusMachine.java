package cn.iocoder.yudao.module.srm.config.purchase.order;

import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilder;
import cn.iocoder.yudao.framework.cola.statemachine.builder.StateMachineBuilderFactory;
import cn.iocoder.yudao.module.srm.config.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.srm.config.machine.SrmOrderInCountContext;
import cn.iocoder.yudao.module.srm.config.machine.SrmPayCountContext;
import cn.iocoder.yudao.module.srm.config.machine.order.SrmOrderItemOffContext;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmExecutionStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmOffStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmPaymentStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmStorageStatus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.*;
import static cn.iocoder.yudao.module.srm.enums.status.SrmExecutionStatus.*;
import static cn.iocoder.yudao.module.srm.enums.status.SrmOffStatus.*;
import static cn.iocoder.yudao.module.srm.enums.status.SrmPaymentStatus.*;
import static cn.iocoder.yudao.module.srm.enums.status.SrmStorageStatus.*;

@Slf4j
@Configuration
@SuppressWarnings({"rawtypes", "unchecked"})
public class SrmPurchaseOrderItemStatusMachine {

    @Autowired
    private BaseFailCallbackImpl baseFailCallbackImpl;
    @Autowired
    Action<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderItemDO> orderItemExecuteAction;
    @Autowired
    Action<SrmStorageStatus, SrmEventEnum, SrmOrderInCountContext> ItemStorageActionImpl;
    @Autowired
    Action<SrmPaymentStatus, SrmEventEnum, SrmPayCountContext> orderItemPayAction;


    @Bean(PURCHASE_ORDER_ITEM_EXECUTION_STATE_MACHINE_NAME)
    public StateMachine<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderItemDO> getPurchaseOrderItemExecutionStateMachine() {
        StateMachineBuilder<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderItemDO> builder = StateMachineBuilderFactory.create();

        // 初始化待执行状态
        builder.internalTransition().within(PENDING).on(SrmEventEnum.EXECUTION_INIT).perform(orderItemExecuteAction);

        // 开始执行
        builder.externalTransitions().fromAmong(PENDING, IN_PROGRESS).to(IN_PROGRESS).on(SrmEventEnum.START_EXECUTION).perform(orderItemExecuteAction);

        // 执行完成
        builder.externalTransitions().fromAmong(PENDING, IN_PROGRESS, COMPLETED).to(COMPLETED).on(SrmEventEnum.COMPLETE_EXECUTION).perform(orderItemExecuteAction);

        // 到货数量调整->执行状态(根据到货数量动态变化)
        builder.externalTransitions().fromAmong(PENDING, IN_PROGRESS, COMPLETED, CANCELLED).to(COMPLETED).on(SrmEventEnum.EXECUTION_ADJUSTMENT).perform(orderItemExecuteAction);

        // 暂停执行
        builder.externalTransition().from(IN_PROGRESS).to(PAUSED).on(SrmEventEnum.PAUSE_EXECUTION).perform(orderItemExecuteAction);

        // 恢复执行
        builder.externalTransition().from(PAUSED).to(IN_PROGRESS).on(SrmEventEnum.RESUME_EXECUTION).perform(orderItemExecuteAction);

        // 取消执行
        builder.externalTransitions().fromAmong(PENDING, IN_PROGRESS, PAUSED).to(CANCELLED).on(SrmEventEnum.CANCEL_EXECUTION).perform(orderItemExecuteAction);

        // 执行失败
        builder.externalTransition().from(IN_PROGRESS).to(FAILED).on(SrmEventEnum.EXECUTION_FAILED).perform(orderItemExecuteAction);

        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(PURCHASE_ORDER_ITEM_EXECUTION_STATE_MACHINE_NAME);
    }
    @Resource
    Action<SrmOffStatus, SrmEventEnum, SrmOrderItemOffContext> OrderItemOffActionImpl;

    @Bean(PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME)
    public StateMachine<SrmStorageStatus, SrmEventEnum, SrmOrderInCountContext> buildPurchaseOrderItemStorageStateMachine() {
        StateMachineBuilder<SrmStorageStatus, SrmEventEnum, SrmOrderInCountContext> builder = StateMachineBuilderFactory.create();

        // 初始化入库
        builder.externalTransition().from(NONE_IN_STORAGE).to(NONE_IN_STORAGE).on(SrmEventEnum.STORAGE_INIT).perform(ItemStorageActionImpl);

        // 取消入库
        builder.externalTransitions().fromAmong(NONE_IN_STORAGE, PARTIALLY_IN_STORAGE).to(NONE_IN_STORAGE).on(SrmEventEnum.CANCEL_STORAGE).perform(ItemStorageActionImpl);

        // 库存调整
        builder.externalTransitions().fromAmong(NONE_IN_STORAGE, PARTIALLY_IN_STORAGE, ALL_IN_STORAGE).to(NONE_IN_STORAGE).on(SrmEventEnum.STOCK_ADJUSTMENT).perform(ItemStorageActionImpl);

        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME);
    }

    //采购订单子项状态机
    @Bean(PURCHASE_ORDER_ITEM_OFF_STATE_MACHINE_NAME)
    public StateMachine<SrmOffStatus, SrmEventEnum, SrmOrderItemOffContext> getPurchaseOrderStateMachine() {
        StateMachineBuilder<SrmOffStatus, SrmEventEnum, SrmOrderItemOffContext> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition().within(OPEN).on(SrmEventEnum.OFF_INIT).perform(OrderItemOffActionImpl);
        // 开启
        builder.externalTransitions().fromAmong(MANUAL_CLOSED).to(OPEN).on(SrmEventEnum.ACTIVATE).perform(OrderItemOffActionImpl);
        // 手动关闭
        builder.externalTransition().from(OPEN).to(MANUAL_CLOSED).on(SrmEventEnum.MANUAL_CLOSE).perform(OrderItemOffActionImpl);
        //自动关闭
        builder.externalTransitions().fromAmong(OPEN, CLOSED).to(CLOSED).on(SrmEventEnum.AUTO_CLOSE).perform(OrderItemOffActionImpl);
        //撤销关闭
        builder.externalTransitions().fromAmong(MANUAL_CLOSED, CLOSED, OPEN).to(OPEN).on(SrmEventEnum.CANCEL_DELETE).perform(OrderItemOffActionImpl);
        //错误回调函数
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(PURCHASE_ORDER_ITEM_OFF_STATE_MACHINE_NAME);
    }

    @Bean(PURCHASE_ORDER_ITEM_PAYMENT_STATE_MACHINE_NAME)
    public StateMachine<SrmPaymentStatus, SrmEventEnum, SrmPayCountContext> getPurchaseOrderItemPaymentStateMachine() {
        StateMachineBuilder<SrmPaymentStatus, SrmEventEnum, SrmPayCountContext> builder = StateMachineBuilderFactory.create();
        // 初始化付款状态
        builder.internalTransition().within(NONE_PAYMENT).on(SrmEventEnum.PAYMENT_INIT).perform(orderItemPayAction);
        // 取消付款
        builder.externalTransitions().fromAmong(NONE_PAYMENT, PARTIALLY_PAYMENT).to(NONE_PAYMENT).on(SrmEventEnum.CANCEL_PAYMENT).perform(orderItemPayAction);
        // 付款异常
        builder.externalTransitions().fromAmong(NONE_PAYMENT, PARTIALLY_PAYMENT, ALL_PAYMENT).to(NONE_PAYMENT).on(SrmEventEnum.PAYMENT_EXCEPTION).perform(orderItemPayAction);
        // 付款调整
        builder.externalTransitions().fromAmong(NONE_PAYMENT, PARTIALLY_PAYMENT, ALL_PAYMENT).to(NONE_PAYMENT).on(SrmEventEnum.PAYMENT_ADJUSTMENT).perform(orderItemPayAction);
        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(PURCHASE_ORDER_ITEM_PAYMENT_STATE_MACHINE_NAME);
    }

}
