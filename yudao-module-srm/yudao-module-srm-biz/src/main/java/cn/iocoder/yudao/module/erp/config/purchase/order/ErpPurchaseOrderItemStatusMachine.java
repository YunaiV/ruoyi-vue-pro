package cn.iocoder.yudao.module.erp.config.purchase.order;

import cn.iocoder.yudao.module.erp.api.purchase.SrmInCountDTO;
import cn.iocoder.yudao.module.erp.config.BaseFailCallbackImpl;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.erp.enums.SrmEventEnum;
import cn.iocoder.yudao.module.erp.enums.SrmStateMachines;
import cn.iocoder.yudao.module.erp.enums.status.ErpExecutionStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpOffStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpPaymentStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpStorageStatus;
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
public class ErpPurchaseOrderItemStatusMachine {


    @Resource
    private BaseFailCallbackImpl baseFailCallbackImpl;

    



    @Resource
    private Action actionOrderItemOffImpl;
    //采购订单子项状态机
    @Bean(SrmStateMachines.PURCHASE_ORDER_ITEM_OFF_STATE_MACHINE_NAME)
    public StateMachine<ErpOffStatus, SrmEventEnum, ErpPurchaseOrderItemDO> getPurchaseOrderItemStateMachine() {
        StateMachineBuilder<ErpOffStatus, SrmEventEnum, ErpPurchaseOrderItemDO> builder = StateMachineBuilderFactory.create();
        // 初始化状态
        builder.internalTransition()
            .within(ErpOffStatus.OPEN)
            .on(SrmEventEnum.OFF_INIT)
            .perform(actionOrderItemOffImpl);
        // 开启
        builder.externalTransitions()
            .fromAmong(ErpOffStatus.MANUAL_CLOSED)
            .to(ErpOffStatus.OPEN)
            .on(SrmEventEnum.ACTIVATE)
            .perform(actionOrderItemOffImpl);
        // 手动关闭
        builder.externalTransition()
            .from(ErpOffStatus.OPEN)
            .to(ErpOffStatus.MANUAL_CLOSED)
            .on(SrmEventEnum.MANUAL_CLOSE)
            .perform(actionOrderItemOffImpl);
        //自动关闭
        builder.externalTransition()
            .from(ErpOffStatus.OPEN)
            .to(ErpOffStatus.CLOSED)
            .on(SrmEventEnum.AUTO_CLOSE)
            .perform(actionOrderItemOffImpl);
        //错误回调函数
        builder.setFailCallback(baseFailCallbackImpl);
        return builder.build(SrmStateMachines.PURCHASE_ORDER_ITEM_OFF_STATE_MACHINE_NAME);
    }


    @Resource
    private Action actionOrderItemExecuteImpl;
    // 采购订单子项执行状态机
    @Bean(SrmStateMachines.PURCHASE_ORDER_ITEM_EXECUTION_STATE_MACHINE_NAME)
    public StateMachine<ErpExecutionStatus, SrmEventEnum, ErpPurchaseOrderItemDO> getPurchaseOrderItemExecutionStateMachine() {
        StateMachineBuilder<ErpExecutionStatus, SrmEventEnum, ErpPurchaseOrderItemDO> builder = StateMachineBuilderFactory.create();

        // 初始化待执行状态
        builder.internalTransition()
            .within(ErpExecutionStatus.PENDING)
            .on(SrmEventEnum.EXECUTION_INIT)
            .perform(actionOrderItemExecuteImpl);

        // 开始执行
        builder.externalTransition()
            .from(ErpExecutionStatus.PENDING)
            .to(ErpExecutionStatus.IN_PROGRESS)
            .on(SrmEventEnum.START_EXECUTION)
            .perform(actionOrderItemExecuteImpl);

        // 执行完成
        builder.externalTransition()
            .from(ErpExecutionStatus.IN_PROGRESS)
            .to(ErpExecutionStatus.COMPLETED)
            .on(SrmEventEnum.COMPLETE_EXECUTION)
            .perform(actionOrderItemExecuteImpl);

        // 暂停执行
        builder.externalTransition()
            .from(ErpExecutionStatus.IN_PROGRESS)
            .to(ErpExecutionStatus.PAUSED)
            .on(SrmEventEnum.PAUSE_EXECUTION)
            .perform(actionOrderItemExecuteImpl);

        // 恢复执行
        builder.externalTransition()
            .from(ErpExecutionStatus.PAUSED)
            .to(ErpExecutionStatus.IN_PROGRESS)
            .on(SrmEventEnum.RESUME_EXECUTION)
            .perform(actionOrderItemExecuteImpl);

        // 取消执行
        builder.externalTransitions()
            .fromAmong(ErpExecutionStatus.PENDING, ErpExecutionStatus.IN_PROGRESS, ErpExecutionStatus.PAUSED)
            .to(ErpExecutionStatus.CANCELLED)
            .on(SrmEventEnum.CANCEL_EXECUTION)
            .perform(actionOrderItemExecuteImpl);

        // 执行失败
        builder.externalTransition()
            .from(ErpExecutionStatus.IN_PROGRESS)
            .to(ErpExecutionStatus.FAILED)
            .on(SrmEventEnum.EXECUTION_FAILED)
            .perform(actionOrderItemExecuteImpl);

        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(SrmStateMachines.PURCHASE_ORDER_ITEM_EXECUTION_STATE_MACHINE_NAME);
    }

    @Resource
    private Action actionOrderItemInImpl;
    @Resource
    private Action actionOrderItemPayImpl;

    @Bean(SrmStateMachines.PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME)
    public StateMachine<ErpStorageStatus, SrmEventEnum, SrmInCountDTO> buildPurchaseOrderItemStorageStateMachine() {
        StateMachineBuilder<ErpStorageStatus, SrmEventEnum, SrmInCountDTO> builder = StateMachineBuilderFactory.create();

        // 初始化入库
        builder.externalTransition()
            .from(ErpStorageStatus.NONE_IN_STORAGE)
            .to(ErpStorageStatus.NONE_IN_STORAGE)
            .on(SrmEventEnum.STORAGE_INIT)
            .perform(actionOrderItemInImpl);

        // 部分入库
//        builder.externalTransition()
//            .from(ErpStorageStatus.NONE_IN_STORAGE)
//            .to(ErpStorageStatus.PARTIALLY_IN_STORAGE)
//            .on(SrmEventEnum.PARTIAL_STORAGE)
//            .perform(actionOrderItemInImpl);
//
//        // 完成入库
//        builder.externalTransitions()
//            .fromAmong(ErpStorageStatus.NONE_IN_STORAGE, ErpStorageStatus.PARTIALLY_IN_STORAGE)
//            .to(ErpStorageStatus.ALL_IN_STORAGE)
//            .on(SrmEventEnum.COMPLETE_STORAGE)
//            .perform(actionOrderItemInImpl);

        // 取消入库
        builder.externalTransitions()
            .fromAmong(ErpStorageStatus.NONE_IN_STORAGE, ErpStorageStatus.PARTIALLY_IN_STORAGE)
            .to(ErpStorageStatus.NONE_IN_STORAGE)
            .on(SrmEventEnum.CANCEL_STORAGE)
            .perform(actionOrderItemInImpl);

//        // 入库异常
//        builder.externalTransitions()
//            .fromAmong(ErpStorageStatus.NONE_IN_STORAGE, ErpStorageStatus.PARTIALLY_IN_STORAGE, ErpStorageStatus.ALL_IN_STORAGE)
//            .to(ErpStorageStatus.NONE_IN_STORAGE)
//            .on(SrmEventEnum.STORAGE_EXCEPTION)
//            .perform(actionOrderItemInImpl);

        // 库存调整
        builder.externalTransitions()
            .fromAmong(ErpStorageStatus.NONE_IN_STORAGE, ErpStorageStatus.PARTIALLY_IN_STORAGE, ErpStorageStatus.ALL_IN_STORAGE)
            .to(ErpStorageStatus.NONE_IN_STORAGE)
            .on(SrmEventEnum.STOCK_ADJUSTMENT)
            .perform(actionOrderItemInImpl);

        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(SrmStateMachines.PURCHASE_ORDER_ITEM_STORAGE_STATE_MACHINE_NAME);
    }

    @Bean(SrmStateMachines.PURCHASE_ORDER_ITEM_PAYMENT_STATE_MACHINE_NAME)
    public StateMachine<ErpPaymentStatus, SrmEventEnum, ErpPurchaseOrderItemDO> getPurchaseOrderItemPaymentStateMachine() {
        StateMachineBuilder<ErpPaymentStatus, SrmEventEnum, ErpPurchaseOrderItemDO> builder = StateMachineBuilderFactory.create();

        // 初始化付款状态
        builder.internalTransition()
            .within(ErpPaymentStatus.NONE_PAYMENT)
            .on(SrmEventEnum.PAYMENT_INIT)
            .perform(actionOrderItemPayImpl);

//        // 部分付款
//        builder.externalTransition()
//            .from(ErpPaymentStatus.NONE_PAYMENT)
//            .to(ErpPaymentStatus.PARTIALLY_PAYMENT)
//            .on(SrmEventEnum.PARTIAL_PAYMENT)
//            .perform(actionOrderItemPayImpl);
//
//        // 完成付款
//        builder.externalTransitions()
//            .fromAmong(ErpPaymentStatus.NONE_PAYMENT, ErpPaymentStatus.PARTIALLY_PAYMENT)
//            .to(ErpPaymentStatus.ALL_PAYMENT)
//            .on(SrmEventEnum.COMPLETE_PAYMENT)
//            .perform(actionOrderItemPayImpl);

        // 取消付款
        builder.externalTransitions()
            .fromAmong(ErpPaymentStatus.NONE_PAYMENT, ErpPaymentStatus.PARTIALLY_PAYMENT)
            .to(ErpPaymentStatus.NONE_PAYMENT)
            .on(SrmEventEnum.CANCEL_PAYMENT)
            .perform(actionOrderItemPayImpl);

        // 付款异常
        builder.externalTransitions()
            .fromAmong(ErpPaymentStatus.NONE_PAYMENT, ErpPaymentStatus.PARTIALLY_PAYMENT, ErpPaymentStatus.ALL_PAYMENT)
            .to(ErpPaymentStatus.NONE_PAYMENT)
            .on(SrmEventEnum.PAYMENT_EXCEPTION)
            .perform(actionOrderItemPayImpl);

        // 付款调整
        builder.externalTransitions()
            .fromAmong(ErpPaymentStatus.NONE_PAYMENT, ErpPaymentStatus.PARTIALLY_PAYMENT, ErpPaymentStatus.ALL_PAYMENT)
            .to(ErpPaymentStatus.NONE_PAYMENT)
            .on(SrmEventEnum.PAYMENT_ADJUSTMENT)
            .perform(actionOrderItemPayImpl);

        // 设置错误回调
        builder.setFailCallback(baseFailCallbackImpl);

        return builder.build(SrmStateMachines.PURCHASE_ORDER_ITEM_PAYMENT_STATE_MACHINE_NAME);
    }

}
