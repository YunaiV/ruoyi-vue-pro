package cn.iocoder.yudao.module.srm.config.purchase.order;

import cn.iocoder.yudao.module.srm.api.purchase.SrmInCountDTO;
import cn.iocoder.yudao.module.srm.api.purchase.SrmPayCountDTO;
import cn.iocoder.yudao.module.srm.config.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.SrmStateMachines;
import cn.iocoder.yudao.module.srm.enums.status.SrmExecutionStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmOffStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmPaymentStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmStorageStatus;
import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@SuppressWarnings({"rawtypes", "unchecked"})
public class SrmPurchaseOrderItemStatusMachine {


    @Resource
    private BaseFailCallbackImpl baseFailCallbackImpl;


    @Resource
    private Action actionOrderItemOffImpl;

    //采购订单子项状态机
    @Bean(SrmStateMachines.PURCHASE_ORDER_ITEM_OFF_STATE_MACHINE_NAME)
    public StateMachine<SrmOffStatus, SrmEventEnum, SrmPurchaseOrderItemDO> getPurchaseOrderItemStateMachine() {
        StateMachineBuilder<SrmOffStatus, SrmEventEnum, SrmPurchaseOrderItemDO> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition()
            .within(SrmOffStatus.OPEN)
            .on(SrmEventEnum.OFF_INIT)
            .perform(actionOrderItemOffImpl);
        // 开启
        builder.externalTransitions()
            .fromAmong(SrmOffStatus.MANUAL_CLOSED)
            .to(SrmOffStatus.OPEN)
            .on(SrmEventEnum.ACTIVATE)
            .perform(actionOrderItemOffImpl);
        // 手动关闭
        builder.externalTransition()
            .from(SrmOffStatus.OPEN)
            .to(SrmOffStatus.MANUAL_CLOSED)
            .on(SrmEventEnum.MANUAL_CLOSE)
            .perform(actionOrderItemOffImpl);
        //自动关闭
        builder.externalTransition()
            .from(SrmOffStatus.OPEN)
            .to(SrmOffStatus.CLOSED)
            .on(SrmEventEnum.AUTO_CLOSE)
            .perform(actionOrderItemOffImpl);
        //关闭撤销
        builder.externalTransitions()
            .fromAmong(SrmOffStatus.MANUAL_CLOSED, SrmOffStatus.CLOSED, SrmOffStatus.OPEN)
            .to(SrmOffStatus.OPEN)
            .on(SrmEventEnum.CANCEL_DELETE)
            .perform(actionOrderItemOffImpl);
        //错误回调函数
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_ORDER_ITEM_OFF_STATE_MACHINE_NAME);
    }


    @Resource
    private Action actionOrderItemExecuteImpl;

    // 采购订单子项执行状态机
    @Bean(SrmStateMachines.PURCHASE_ORDER_ITEM_EXECUTION_STATE_MACHINE_NAME)
    public StateMachine<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderItemDO> getPurchaseOrderItemExecutionStateMachine() {
        StateMachineBuilder<SrmExecutionStatus, SrmEventEnum, SrmPurchaseOrderItemDO> builder = StateMachineBuilderFactory.create();

        // 初始化待执行状态
        builder.internalTransition()
            .within(SrmExecutionStatus.PENDING)
            .on(SrmEventEnum.EXECUTION_INIT)
            .perform(actionOrderItemExecuteImpl);

        // 开始执行
        builder.externalTransition()
            .from(SrmExecutionStatus.PENDING)
            .to(SrmExecutionStatus.IN_PROGRESS)
            .on(SrmEventEnum.START_EXECUTION)
            .perform(actionOrderItemExecuteImpl);

        // 执行完成
        builder.externalTransition()
            .from(SrmExecutionStatus.IN_PROGRESS)
            .to(SrmExecutionStatus.COMPLETED)
            .on(SrmEventEnum.COMPLETE_EXECUTION)
            .perform(actionOrderItemExecuteImpl);

        // 暂停执行
        builder.externalTransition()
            .from(SrmExecutionStatus.IN_PROGRESS)
            .to(SrmExecutionStatus.PAUSED)
            .on(SrmEventEnum.PAUSE_EXECUTION)
            .perform(actionOrderItemExecuteImpl);

        // 恢复执行
        builder.externalTransition()
            .from(SrmExecutionStatus.PAUSED)
            .to(SrmExecutionStatus.IN_PROGRESS)
            .on(SrmEventEnum.RESUME_EXECUTION)
            .perform(actionOrderItemExecuteImpl);

        // 取消执行
        builder.externalTransitions()
            .fromAmong(SrmExecutionStatus.PENDING, SrmExecutionStatus.IN_PROGRESS, SrmExecutionStatus.PAUSED)
            .to(SrmExecutionStatus.CANCELLED)
            .on(SrmEventEnum.CANCEL_EXECUTION)
            .perform(actionOrderItemExecuteImpl);

        // 执行失败
        builder.externalTransition()
            .from(SrmExecutionStatus.IN_PROGRESS)
            .to(SrmExecutionStatus.FAILED)
            .on(SrmEventEnum.EXECUTION_FAILED)
            .perform(actionOrderItemExecuteImpl);

        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(SrmStateMachines.PURCHASE_ORDER_ITEM_EXECUTION_STATE_MACHINE_NAME);
    }

    @Resource
    private Action actionOrderItemInImpl;


    @Bean(SrmStateMachines.PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME)
    public StateMachine<SrmStorageStatus, SrmEventEnum, SrmInCountDTO> buildPurchaseOrderItemStorageStateMachine() {
        StateMachineBuilder<SrmStorageStatus, SrmEventEnum, SrmInCountDTO> builder = StateMachineBuilderFactory.create();

        // 初始化入库
        builder.externalTransition()
            .from(SrmStorageStatus.NONE_IN_STORAGE)
            .to(SrmStorageStatus.NONE_IN_STORAGE)
            .on(SrmEventEnum.STORAGE_INIT)
            .perform(actionOrderItemInImpl);

        // 部分入库
//        builder.externalTransition()
//            .from(SrmStorageStatus.NONE_IN_STORAGE)
//            .to(SrmStorageStatus.PARTIALLY_IN_STORAGE)
//            .on(SrmEventEnum.PARTIAL_STORAGE)
//            .perform(actionOrderItemInImpl);
//
//        // 完成入库
//        builder.externalTransitions()
//            .fromAmong(SrmStorageStatus.NONE_IN_STORAGE, SrmStorageStatus.PARTIALLY_IN_STORAGE)
//            .to(SrmStorageStatus.ALL_IN_STORAGE)
//            .on(SrmEventEnum.COMPLETE_STORAGE)
//            .perform(actionOrderItemInImpl);

        // 取消入库
        builder.externalTransitions()
            .fromAmong(SrmStorageStatus.NONE_IN_STORAGE, SrmStorageStatus.PARTIALLY_IN_STORAGE)
            .to(SrmStorageStatus.NONE_IN_STORAGE)
            .on(SrmEventEnum.CANCEL_STORAGE)
            .perform(actionOrderItemInImpl);

//        // 入库异常
//        builder.externalTransitions()
//            .fromAmong(SrmStorageStatus.NONE_IN_STORAGE, SrmStorageStatus.PARTIALLY_IN_STORAGE, SrmStorageStatus.ALL_IN_STORAGE)
//            .to(SrmStorageStatus.NONE_IN_STORAGE)
//            .on(SrmEventEnum.STORAGE_EXCEPTION)
//            .perform(actionOrderItemInImpl);

        // 库存调整
        builder.externalTransitions()
            .fromAmong(SrmStorageStatus.NONE_IN_STORAGE, SrmStorageStatus.PARTIALLY_IN_STORAGE, SrmStorageStatus.ALL_IN_STORAGE)
            .to(SrmStorageStatus.NONE_IN_STORAGE)
            .on(SrmEventEnum.STOCK_ADJUSTMENT)
            .perform(actionOrderItemInImpl);

        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(SrmStateMachines.PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME);
    }

    @Resource
    private Action actionOrderItemPayImpl;

    @Bean(SrmStateMachines.PURCHASE_ORDER_ITEM_PAYMENT_STATE_MACHINE_NAME)
    public StateMachine<SrmPaymentStatus, SrmEventEnum, SrmPayCountDTO> getPurchaseOrderItemPaymentStateMachine() {
        StateMachineBuilder<SrmPaymentStatus, SrmEventEnum, SrmPayCountDTO> builder = StateMachineBuilderFactory.create();

        // 初始化付款状态
        builder.internalTransition()
            .within(SrmPaymentStatus.NONE_PAYMENT)
            .on(SrmEventEnum.PAYMENT_INIT)
            .perform(actionOrderItemPayImpl);

//        // 部分付款
//        builder.externalTransition()
//            .from(SrmPaymentStatus.NONE_PAYMENT)
//            .to(SrmPaymentStatus.PARTIALLY_PAYMENT)
//            .on(SrmEventEnum.PARTIAL_PAYMENT)
//            .perform(actionOrderItemPayImpl);
//
//        // 完成付款
//        builder.externalTransitions()
//            .fromAmong(SrmPaymentStatus.NONE_PAYMENT, SrmPaymentStatus.PARTIALLY_PAYMENT)
//            .to(SrmPaymentStatus.ALL_PAYMENT)
//            .on(SrmEventEnum.COMPLETE_PAYMENT)
//            .perform(actionOrderItemPayImpl);

        // 取消付款
        builder.externalTransitions()
            .fromAmong(SrmPaymentStatus.NONE_PAYMENT, SrmPaymentStatus.PARTIALLY_PAYMENT)
            .to(SrmPaymentStatus.NONE_PAYMENT)
            .on(SrmEventEnum.CANCEL_PAYMENT)
            .perform(actionOrderItemPayImpl);

        // 付款异常
        builder.externalTransitions()
            .fromAmong(SrmPaymentStatus.NONE_PAYMENT, SrmPaymentStatus.PARTIALLY_PAYMENT, SrmPaymentStatus.ALL_PAYMENT)
            .to(SrmPaymentStatus.NONE_PAYMENT)
            .on(SrmEventEnum.PAYMENT_EXCEPTION)
            .perform(actionOrderItemPayImpl);

        // 付款调整
        builder.externalTransitions()
            .fromAmong(SrmPaymentStatus.NONE_PAYMENT, SrmPaymentStatus.PARTIALLY_PAYMENT, SrmPaymentStatus.ALL_PAYMENT)
            .to(SrmPaymentStatus.NONE_PAYMENT)
            .on(SrmEventEnum.PAYMENT_ADJUSTMENT)
            .perform(actionOrderItemPayImpl);

        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(SrmStateMachines.PURCHASE_ORDER_ITEM_PAYMENT_STATE_MACHINE_NAME);
    }

}
